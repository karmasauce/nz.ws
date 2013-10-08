package com.nordzucker.agri.portal.framework;

import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import com.nordzucker.agri.portal.framework.utils.NavNode;
import com.sap.portal.desktop.IDesktopService;
import com.sap.security.api.IUserMaint;
import com.sap.security.api.UMException;
import com.sap.security.api.UMFactory;
import com.sapportals.portal.navigation.INavigationNode;
import com.sapportals.portal.navigation.NavigationEventsHelperService;
import com.sapportals.portal.prt.component.AbstractPortalComponent;
import com.sapportals.portal.prt.component.IPortalComponentContext;
import com.sapportals.portal.prt.component.IPortalComponentRequest;
import com.sapportals.portal.prt.component.IPortalComponentResponse;
import com.sapportals.portal.prt.component.IPortalComponentURI;
import com.sapportals.portal.prt.pom.IComponentNode;
import com.sapportals.portal.prt.pom.IEvent;
import com.sapportals.portal.prt.pom.INode;
import com.sapportals.portal.prt.pom.INodeList;
import com.sapportals.portal.prt.pom.IPortalNode;
import com.sapportals.portal.prt.resource.IResource;
import com.sapportals.portal.prt.runtime.PortalRuntime;
import com.sapportals.portal.prt.util.StringUtils;

public class FrameworkPageComponent extends AbstractPortalComponent {

	final String NZ_PORTAL_PATH = "/irj/portal/nordzucker";
	final String NZ_PORTAL_PATH_SLASH = NZ_PORTAL_PATH + '/';
	final String NZ_PORTAL_PATH_SLASH_REGEXP = '^' + NZ_PORTAL_PATH_SLASH;
	
	protected void doOnNodeReady(IPortalComponentRequest request, IEvent event) {
		Config cfg = Config.getInstance(request);

		if (checkLanguage(request, cfg)) {
			// the language check ended with a redirect, >> exit
			return;
		}
		
		if (cfg.isOTConnectorPage()) {
			// The navigation node indicates that it originates from the PSM/OT navigation connector, that means that 
			// currently (until a fix comes from OT) the node's launch URL returns an HTTP URL instead of a PCD URL.
			// This in turn means that the portal would render an iFrame and load it with that HTTP URL.
			// Since at Nordzucker we want to avoid iFrames for the content, the framework will do a servlet-forward to 
			// the very same framework page - parametrized PSM parameters - with which this time will load the SmartView iView directly
			
			// Check whether this is the first "request" (not the forwarded request). 
			if (request.getParameter("paraurl") == null && request.getParameter("pageTitle") == null) {
				String launchUrl = cfg.getLaunchURL();

				IDesktopService desktopservice = (IDesktopService) PortalRuntime.getRuntimeResources().getService(IDesktopService.KEY);
				IPortalComponentURI uri = request.createPortalComponentURI();
				uri.setContextName(desktopservice.getCurrentFrameworkPageUrl(request));
				
				String forwardUrl = 
						// take the URI of the framework page and strip the leading "/irj"
						uri.toString().replaceFirst("/irj", "") +
						
						// attach the node's title for usage as a window title
						"?pageTitle=" + StringUtils.escapeToURL(cfg.getLaunchedNode().getTitle(cfg.getLocale())) +
						
						// pass the currently selected node as NavigationTarget
						"&NavigationTarget=" + cfg.getSelectedNode().getHashedName() +
						
						// attach the PSM parameters that have been taken from the query string of the PSM launch URL
						"&" + launchUrl.substring(launchUrl.indexOf("?")+1, launchUrl.length());
				
				try {
					request.getServletRequest().getRequestDispatcher(forwardUrl).forward(request.getServletRequest(), request.getServletResponse(true));
					return;
				} catch (ServletException e) {
				} catch (IOException e) {
				}
			} else {
				// We are in the forwarded request. Now include the SmartView iView and let it handle the rest. 
				addChildNode(request, "pcd:portal_content/other_vendors/specialist/com.opentext.pct.wsmpm/iviews/com.opentext.pct.wsmpm.smartview");
			}
		}
		
		else if (cfg.isOTPage()) { // || cfg.isWPCPage() || cfg.isKMPage() ||Êcfg.isHomePage()
			// one of the 'known' content pages, embed directly
			addChildNode(request, cfg.getLaunchURL());
		} 
		
		else if (cfg.isEmbeddablePage()) {
			// for pages custom-marked as "embeddable" EPCM has to be load, and the content will be embedded (which is okay in the case of IE9 because the X-UA-Compatible will be set to EmulateIE8)
			addChildNode(request, request.getComponentContext().getApplicationName() + ".epcm_shim");
			addChildNode(request, cfg.getLaunchURL());
			request.putValue("isEmbeddablePage", Boolean.TRUE);
		}
		
		else {	
			// all other pages are loaded into an iframe and the EPCM is forced to load as well
			addChildNode(request, request.getComponentContext().getApplicationName() + ".epcm_shim");
			request.putValue("iframeURL", cfg.getLaunchURL());
		}
	}

	
	
	
	private boolean checkLanguage(IPortalComponentRequest request, Config cfg) {
		String portalPath = cfg.getPortalPath();
		
		if (!cfg.isAnonymousUser()) {
			// authenticated user --> if an authenticated user lacks a user language,
			// but has a country, then set (=initialize) the language accordingly, that is:
			// country >> lang
			// DE      >> de
			// FI      >> fi
			// SE      >> sv (Sweden > Swedish)
			// DK      >> dk (Danmark > Danish)
			// PL      >> pl
			// LT      >> lt
			// SK      >> sk
			String country = request.getUser().getCountry();
			Locale userLocale = request.getUser().getLocale();
			
			if (userLocale == null && country != null && Config.getSupportedCountries().containsKey(country)) {
				try {
					IUserMaint userMaint = UMFactory.getUserFactory().getMutableUser(request.getUser().getUniqueID());
					userMaint.setLocale(new Locale(Config.getSupportedCountries().get(country)));
					userMaint.save();
					userMaint.commit();
				} catch (UMException e) {
				}
			}
		} else {
			// guest user --> set the (temporary) locale 
			request.getNode().getPortalNode().putValue("isAnonymous", "true");
			int pos = portalPath.indexOf(NZ_PORTAL_PATH_SLASH);
			if (pos > -1) {
				String lang = portalPath.replaceFirst(NZ_PORTAL_PATH_SLASH_REGEXP, "").replaceFirst("/login$", "");
				
				if (Config.getSupportedLanguages().containsKey(lang) && !cfg.getLocale().getLanguage().equals(lang)) {
					Locale newLocale = new Locale(lang);
					request.getServletRequest().getSession().setAttribute("sessionLocale", newLocale); 
					try {
						request.getServletResponse(true).sendRedirect(NZ_PORTAL_PATH_SLASH + lang);
					} catch (IOException e) {
					}
					return true;
				}
			}
		}
		
		
		// if the portal path doesn't contain a language part
		// redirect to the portal path that reflects the current user's/browser's locale
		if (portalPath.replaceFirst("/$", "").equals(NZ_PORTAL_PATH)) {
			String lang = request.getLocale().getLanguage();
			if (Config.getSupportedLanguages().containsKey(lang)) {
				try {
					request.getServletResponse(true).sendRedirect(NZ_PORTAL_PATH_SLASH + lang);
				} catch (IOException e) {
				}
				return true;
			}
		}
		
		return false;
	}
	
	
	
	public void doContent(IPortalComponentRequest request, IPortalComponentResponse response) {
		
		if (request.getParameter("ERR") != null) {
			response.write(request.getParameter("null").indexOf("none") + "");
		}
		
		// set magic cookie (confer Thomas Schreiner)
		setMagicCookie(request);
		
		Object isEmbeddablePage = request.getValue("isEmbeddablePage");
		if (isEmbeddablePage != null) {
			response.write("<section id='content' class='content oneColumn'><div class='inner'>");
			
			// embed any child nodes...
			includeChildNodes(request, response);

			response.write("</div></section>");
		} else {
			// embed any child nodes...
			includeChildNodes(request, response);
		}
		
		// ...and if needed an iFrame as well
		Object iframeURL = request.getValue("iframeURL");
		if (iframeURL != null) {
			writeIFrame(request, response, iframeURL);
		}
	}

	
	
	
	private void writeIFrame(IPortalComponentRequest request, IPortalComponentResponse response, Object iframeURL) {
		IPortalComponentURI uri = request.createPortalComponentURI();
		if (iframeURL != null) {
			uri.setContextName(iframeURL.toString());
		}

		HttpServletRequest servletRequest = request.getServletRequest();
		String method = servletRequest.getMethod();
		if ((method != null) && (method.equals("POST"))) {
			response.write("<iframe src='");
			response.write(request.getResource(IResource.STATIC_PAGE, "mimes/relax.html").getResourceInformation().getURL(request));
			response.write("' frameborder='0' id='content' class='oneColumn' name='contentFrame'></iframe>");
			response.write("<form id='contentForm' target='contentFrame' method='POST' action='");
			response.write(uri.toString());
			response.write("' style='height:0;width:0;overflow:hidden;position:absolute;top:-999em'>");
			
			// transform all parameters into hidden input fields
			Map<String, String[]> parameterMap = servletRequest.getParameterMap();
			Iterator<?> it;
			if (parameterMap != null) {
				for (it = parameterMap.entrySet().iterator(); it.hasNext();) {
					Map.Entry<String, String[]> entry = (Map.Entry<String, String[]>) it.next();
					String param = (String) entry.getKey();
					String[] values = (String[]) entry.getValue();
					if (values == null) {
						response.write("<input type='hidden' name='");
						response.write(StringUtils.escapeToHTML(param));
						response.write("' />");
					} else {
						for (int v = 0; v < values.length; v++) {
							response.write("<input type='hidden' name='");
							response.write(StringUtils.escapeToHTML(param));
							response.write("' value='");
							response.write(StringUtils.escapeToHTML(values[v]));
							response.write("' />");
						}
					}
				}
			}
			response.write("</form>");
			response.write("<script>document.forms['contentForm'].submit();</script>");
		} else {
			response.write("<iframe src='");
			response.write(uri.toString());
			String queryString = servletRequest.getQueryString();
			if (queryString != null) {
				response.write("?" + queryString);
			}
			response.write("' frameborder='0' id='content' class='oneColumn'></iframe>");
		}
	}

	private void setMagicCookie(IPortalComponentRequest request) {
		StringBuffer sb = new StringBuffer();
		sb.append("auth");
		sb.append(request.getUser().getName());
		sb.append("EBV");
		String nz_sso = sb.toString();
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(nz_sso.getBytes(), 0, nz_sso.length());
			String nz_sso_hash = new BigInteger(1, md.digest()).toString(16);
			nz_sso_hash = "00000000000000000000000000000000".substring(0, 32-nz_sso_hash.length()) + nz_sso_hash;
			Cookie cookie = new Cookie("nz_sso_hash", nz_sso_hash);
			cookie.setPath("/");
			cookie.setDomain(request.getServletRequest().getServerName().replaceFirst("[^.]+\\.", "."));
			request.getServletResponse(false).addCookie(cookie);
			// setCookie("nz_sso_hash","<%= nz_sso_hash %>", 0); // JS?
		} catch (NoSuchAlgorithmException e) {
		}
	}

	private void addChildNode(IPortalComponentRequest request, String launchURL) {
		INode node = request.getNode();
		if (node != null) {
			IPortalNode portalNode = node.getPortalNode();

			IPortalComponentContext componentContext = request.getComponentContext(launchURL);
			if (componentContext != null) {
				IComponentNode contentAreaNode = portalNode.createComponentNode("contentArea", componentContext);
				node.addChildNode(contentAreaNode);
			}
		}
	}

	private void includeChildNodes(IPortalComponentRequest request, IPortalComponentResponse response) {
		INode node = request.getNode();
		INodeList contentAreaNodes = node.getChildNodesByName("contentArea");
		if (contentAreaNodes != null) {
			for (int i = 0; i < contentAreaNodes.getLength(); i++) {
				response.include(request, contentAreaNodes.item(i));
			}
		}
	}
}
