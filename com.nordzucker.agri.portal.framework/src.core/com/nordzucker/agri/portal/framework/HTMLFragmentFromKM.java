package com.nordzucker.agri.portal.framework;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Locale;

import com.nordzucker.agri.portal.framework.utils.UnicodeBOMInputStream;
import com.sap.portal.navigation.IAliasHelper;
import com.sap.portal.navigation.IAliasService;
import com.sapportals.portal.prt.component.AbstractPortalComponent;
import com.sapportals.portal.prt.component.IPortalComponentProfile;
import com.sapportals.portal.prt.component.IPortalComponentRequest;
import com.sapportals.portal.prt.component.IPortalComponentResponse;
import com.sapportals.portal.prt.runtime.PortalRuntime;
import com.sapportals.wcm.repository.AccessDeniedException;
import com.sapportals.wcm.repository.IResource;
import com.sapportals.wcm.repository.IResourceContext;
import com.sapportals.wcm.repository.IResourceFactory;
import com.sapportals.wcm.repository.ResourceException;
import com.sapportals.wcm.repository.ResourceFactory;
import com.sapportals.wcm.util.content.ContentException;
import com.sapportals.wcm.util.content.IContent;
import com.sapportals.wcm.util.uri.RID;

public class HTMLFragmentFromKM extends AbstractPortalComponent {
	private static final String ACCESS_PATH = "/irj/go/km/docs";
	private static final String CONTENT_PATH = "/irj/go/prtrw/prtroot/bestrun.html_fragment";

	public void doContent(IPortalComponentRequest request, IPortalComponentResponse response) {
		Locale locale = request.getLocale();
		
		IPortalComponentProfile profile = request.getComponentContext().getProfile();

		// Get location of carousel.html from properties
		String RID = (String) request.getNode().getValue("com.sap.bestrun.framework.RID"); // set by com.sap.bestrun.framework.FrameworkLayout.doOnNodeReady()
		if (RID == null) {
			RID = request.getParameter("RID");
		}
		if (RID == null) {
			String requestUri = request.getServletRequest().getRequestURI();
			if (requestUri.startsWith(CONTENT_PATH) && requestUri.length() > CONTENT_PATH.length()) {
				RID = requestUri.substring(CONTENT_PATH.length());
			}
		}
		if (RID == null) {
			RID = profile.getProperty("com.sap.bestrun.framework.RID");
		}

		if (RID != null && RID.startsWith(ACCESS_PATH)) {
			RID = RID.substring(ACCESS_PATH.length()); 
		}

		
		IAliasHelper aliasHelper = (IAliasHelper) PortalRuntime.getRuntimeResources().getService(IAliasService.KEY);
		String portalPath = aliasHelper.getPath(request);

		response.write(HTMLFragmentFromKM.getHTMLFragment(RID, locale, portalPath));
	}

	/**
	 * @param request
	 * @param response
	 * @param RID
	 * @return
	 */
	public static String getHTMLFragment(String RID, Locale locale, String portalPath) {
		IResource resource = getResource(RID, locale);
		if (resource == null) {
			return "<span style='background:#fff;color:#D0001D;font-size:11px;'>KM resource not found: \"" + RID + "\"</span>";
		}

		String folderPathHref = "";
		String folderPathSrc = "";
		try {
			folderPathHref = CONTENT_PATH + resource.getParentCollection().getRID().getPath();
			folderPathSrc = ACCESS_PATH + resource.getParentCollection().getRID().getPath();
		} catch (AccessDeniedException e) {
		} catch (ResourceException e) {
		}

		Writer out = new StringWriter();
		writeResourceContent(resource, out);

		String html = out.toString().trim();

		// some processing
//		if (RID.endsWith(".textile") || RID.endsWith(".text") || RID.endsWith(".txt")) {
//			Textile textile = new Textile();
//			html = textile.process(html);
//		}

		html = html.replaceAll(" (data-)?href='\\$(.*?)'", " $1href=\""+portalPath+"$2\""); // <a href='...'>
		html = html.replaceAll(" (data-)?href=\"\\$(.*?)\"", " $1href=\""+portalPath+"$2\""); // <a href="...">
		html = html.replaceAll(" href='(?!mailto:|data:|http://|https://|ftp://|/|#)(.*?)'", " href='"+folderPathHref+"/$1'"); // <a href='...'>
		html = html.replaceAll(" href=\"(?!mailto:|data:|http://|https://|ftp://|/|#)(.*?)\"", " href=\""+folderPathHref+"/$1\""); // <a href="...">
		html = html.replaceAll(" src='(?!mailto:|data:|http://|https://|ftp://|/|#)(.*?)'", " src='"+folderPathSrc+"/$1'"); // <img src='...'>, <iframe src='...'>
		html = html.replaceAll(" src=\"(?!mailto:|data:|http://|https://|ftp://|/|#)(.*?)\"", " src=\""+folderPathSrc+"/$1\""); // <img src="...">, <iframe src="...">
		html = html.replaceAll("url\\((?!mailto:|data:|http://|https://|ftp://|/)(.*?)\\)", "url("+folderPathSrc+"/$1)"); // background-image: url(...)
		html = html.replaceAll("url\\('(?!mailto:|data:|http://|https://|ftp://|/)(.*?)'\\)", "url('"+folderPathSrc+"/$1')"); // background-image: url('...')
		html = html.replaceAll("url\\(\"(?!mailto:|data:|http://|https://|ftp://|/)(.*?)\"\\)", "url(\""+folderPathSrc+"/$1\")"); // background-image: url("...")
		return html;
	}

	private static void writeResourceContent(IResource resource, Writer out) {
		try {
			IContent content = resource.getContent();

			InputStream is = content.getInputStream();
			UnicodeBOMInputStream ubis = new UnicodeBOMInputStream(is);
			String encoding = content.getEncoding();
			if (encoding == null) {
				encoding = "UTF-8";
			}
			InputStreamReader isr = new InputStreamReader(ubis, encoding);
			BufferedReader br = new BufferedReader(isr, 2^10);
			
			ubis.skipBOM();
			
			String text;
			while ((text = br.readLine()) != null) {
				out.write(text);
			}
			
			br.close();
			isr.close();
			ubis.close();
			is.close();
		} catch (ContentException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static IResource getResource(String rid, Locale locale) {
		if (rid == null) {
			return null;
		}
		
		try {
			// get the service user to access the CM			
			IResourceFactory rf = ResourceFactory.getInstance();
			IResourceContext ctx = rf.getServiceContext("cmadmin_service");
			
			String loc = '_' + locale.toString();
			
			do {
				String i8nrid = rid.replaceFirst("(\\.\\w+)$", loc+"$1");
				IResource resource = rf.getResource(RID.getRID(i8nrid), ctx);
				if (resource != null) {
					return resource;
				} else {
					loc = loc.replaceFirst("(.*?)_[^_]+$", "$1");
				}
			} while (loc.indexOf('_') > -1);
			
			IResource resource = rf.getResource(RID.getRID(rid), ctx);
			if (resource != null) {
				return resource;
			}
			
		} catch (ResourceException e1) {
			e1.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
