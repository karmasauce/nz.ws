package com.nordzucker.agri.portal.framework;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.http.HttpSession;

import net.sf.json.JSON;
import net.sf.json.JSONSerializer;

import com.sapconsulting.portal.utils.html.EnhancedPortalResponse;
import com.sapconsulting.portal.utils.html.elements.HtmlFactory;
import com.sapconsulting.portal.utils.html.elements.IHtmlElement;
import com.sapportals.portal.pb.layout.PageLayout;
import com.sapportals.portal.prt.component.IPortalComponentProfile;
import com.sapportals.portal.prt.component.IPortalComponentRequest;
import com.sapportals.portal.prt.component.IPortalComponentResponse;
import com.sapportals.portal.prt.pom.IEvent;
import com.sapportals.portal.prt.resource.IResource;
import com.sapportals.portal.prt.util.StringUtils;


public class FrameworkLayoutComponent extends PageLayout {
//	private static final String SCRIPT_DOMAIN_RELAX = "<script>(function(d,D){d.domain=/^\\d+\\.\\d+\\.\\d+\\.\\d+$/.test(D=d.domain)?D:D.replace(/^(?:[^.]+\\.)?([^.]+\\..*)$/,'$1');}(document))</script>";
	
	protected void doBeforeContent(IPortalComponentRequest request, IEvent event) {
		// remove global style sheets from request
		HttpSession session = request.getServletRequest().getSession();
		session.setAttribute("com.sap.portal.themes.lafservice.requiredThemeParts", "");
	}

	
	public void doContent(IPortalComponentRequest request, IPortalComponentResponse response) {
		Config cfg = Config.getInstance(request);
		
//		ResourceBundle rb = ResourceBundle.getBundle("framework", cfg.getLocale());
		ResourceBundle rb = request.getResourceBundle();
		
		
		
//		IPortalComponentProfile layoutProfile = request.getComponentContext().getProfile();
//		String windowTitle = layoutProfile.getProperty("com.nordzucker.agri.portal.framework.WindowTitle");
		
		String windowTitle = rb.getString("window.title");
		
		EnhancedPortalResponse epResponse = new EnhancedPortalResponse(request, true, false);
		
		epResponse.setDocTypeToHtml5();
		epResponse.addHtmlAttribute("lang", request.getLocale().getLanguage().toString());
		epResponse.setTitle(cfg.getPageTitle() + " | " + windowTitle);

		epResponse.addHtmlAttribute("id", "nz");
		epResponse.addBodyAttribute("id", "fwk");

		String userAgent = request.getServletRequest().getHeader("User-Agent");
		
		

		boolean isGTEIE10 = false;
		if (userAgent != null && userAgent.matches("^.*MSIE \\d\\d\\.\\d+.*$")) {
			isGTEIE10 = true;
		}
		
//		if (!isGTEIE10 && !cfg.isOTPage()) {
			// for legacy (=portal) content fall back to IE8 behaviour of integrating iframes with content in quirks mode (interesting only for IE9)
//			request.getServletResponse(false).addHeader("X-UA-Compatible", "IE=EmulateIE8");
//			epResponse.include(HtmlFactory.createHttpEquivMeta("X-UA-Compatible", "IE=EmulateIE8"));
//		} else {
			// for OT PSM pages use "bleeding edge"
			request.getServletResponse(false).addHeader("X-UA-Compatible", "IE=edge");
//			epResponse.include(HtmlFactory.createHttpEquivMeta("X-UA-Compatible", "IE=edge"));
//		}
		epResponse.include(HtmlFactory.createMeta("viewport", "width=device-width, initial-scale=1, user-scalable=0"));
		
		String mimesPath = cfg.getMimesPath();
		
		StringBuffer buf = new StringBuffer(2 ^ 8);
		
//		buf.append("\n<!--[if lte IE 9]>");
//		if (!isGTEIE10 && !cfg.isOTPage()) {
			// for legacy (=portal) content fall back to IE8 behaviour of integrating iframes with content in quirks mode (interesting only for IE9)
//			HtmlFactory.createHttpEquivMeta("X-UA-Compatible", "IE=EmulateIE8").getUniqueHtmlCode(buf);
//		} else {
			// for OT PSM pages use "bleeding edge"
			HtmlFactory.createHttpEquivMeta("X-UA-Compatible", "IE=edge").getUniqueHtmlCode(buf);
//		}
//		buf.append("\n<![endif]-->");
		
		buf.append("\n<!--[if IE]>"); // all IE excluding IE 10
		HtmlFactory.createLink("shortcut icon", mimesPath + "favicon.ico", null, null).getUniqueHtmlCode(buf);
		buf.append("\n<![endif]-->");
		
		buf.append("\n<!--[if !IE]><!-->"); // modern browsers: Firefox, Chrome, Opera... (including IE10!)
//		HtmlFactory.createHttpEquivMeta("X-UA-Compatible", "IE=edge").getUniqueHtmlCode(buf); // for IE10
		HtmlFactory.createLink("shortcut icon", mimesPath + "favicon.png", null, null).getUniqueHtmlCode(buf);
//		buf.append("<style>");
//		buf.append("\n@font-face{"); 
//		buf.append("\n\tfont-family:'StoneSansMedium';");
//		buf.append("\n\tsrc:url('").append(mimesPath).append("/fonts/StoneSansMedium.ttf') format('truetype'),url('").append(mimesPath).append("/fonts/StoneSansMedium.woff');");
//		buf.append("\n\tfont-weight:normal;");
//		buf.append("\n\tfont-style:normal;");
//		buf.append("\n}");
//		buf.append("\n</style>");
		buf.append("\n<![endif]-->");
		
		buf.append("\n<link rel='apple-touch-icon' href='").append(mimesPath).append("apple-touch-icon-57x57.png' />");
		buf.append("\n<link rel='apple-touch-icon' sizes='72x72' href='").append(mimesPath).append("apple-touch-icon-72x72.png' />");
		buf.append("\n<link rel='apple-touch-icon' sizes='114x114' href='").append(mimesPath).append("apple-touch-icon-114x114.png' />");
		buf.append("\n<link rel='apple-touch-icon' sizes='144x144' href='").append(mimesPath).append("apple-touch-icon-144x144.png' />");
		buf.append("\n<meta name='msapplication-TileImage' content='").append(mimesPath).append("ms-tile-image-144x144.png' />");
		buf.append("\n<meta name='msapplication-TileColor' content='#ffffff' />");
		
		IPortalComponentProfile profile = request.getComponentContext().getProfile();
		String lang = cfg.getLocale().getLanguage();
		
		String rssUrl = profile.getProperty("com.nordzucker.agri.portal.framework.RSSURL");
		if (cfg.isAnonymousUser() && rssUrl != null && !rssUrl.equals("")) {
			buf.append("\n<link rel='alternate' href='").append(rssUrl.replaceAll("#LANG#", lang)).append("' type='application/rss+xml' title='RSS' />");
		}
		
		buf.append("\n<!--[if lt IE 9]>");
		buf.append("\n<script src='").append(mimesPath).append("html5shiv.js").append("'></script>");
		buf.append("\n<![endif]-->");
		buf.append("<script>(function(D,M){M=D.documentMode;D.documentElement.className+='js'+(M?(M<9?' old-ie':'')+' lte-ie'+M:'');})(document)</script>");
		epResponse.include(new HtmlString(buf.toString()));
		
		epResponse.include(request.getResource(IResource.CSS, "mimes/nz.css"));
		
		super.doContent(request, response);

		String updateAOBUserUrl = profile.getProperty("com.nordzucker.agri.portal.framework.UpdateAOBUserURL");
		if (updateAOBUserUrl == null) {
			updateAOBUserUrl = "";
		}
		
		epResponse.defer(HtmlFactory.createInlineScript("var FWK = {" +
//				"place:'" + StringUtils.escapeToJS(place) + "'," +
//				"placeName:'" + StringUtils.escapeToJS(placeName) + "'" +

				"user:'" + StringUtils.escapeToJS(request.getUser().getUniqueName()) + "'," +
				"portalPath:'" + cfg.getPortalPath() + "'," +
				"updateAOBUserUrl:'" + updateAOBUserUrl.replaceAll("#LANG#", lang) + "'," +
				"isDebug:" + cfg.isDebug() +
				"ip:" +request.getServletRequest().getRemoteAddr().toString() +
				"nav:" +cfg.getSelectedNode().getTitle(cfg.getLocale()) +
				"navUrl:" +request.getParameter("NavigationTarget") +
				"headlineText:" + rb.getString("info.headline") + 
				"browserText:" + rb.getString("info.browser") + 
				"browserVersionText:" + rb.getString("info.browserVersion") + 
				"osText:" + rb.getString("info.os") + 
				"userText:" + rb.getString("info.user") + 
				"navText:" + rb.getString("info.nav") + 
				"ipText:" + rb.getString("info.ip") + 
				"bandwidthText:" + rb.getString("info.bandwidth") + 
				"mimesPath:" + request.getWebResourcePath() + 
				
				
			"};", null));
		
		if (cfg.isDebug()) {
			epResponse.defer(request.getResource(IResource.SCRIPT, "mimes/jquery.js"));
			epResponse.defer(request.getResource(IResource.SCRIPT, "mimes/dropkick/jquery.dropkick-1.0.0.js"));
			epResponse.defer(request.getResource(IResource.SCRIPT, "mimes/session.js"));
			epResponse.defer(request.getResource(IResource.SCRIPT, "mimes/bpselect_plain.js"));
			epResponse.defer(request.getResource(IResource.SCRIPT, "mimes/nz.js"));
		} else {
			// jQuery, dropkick and bpselect_plain.js are already contained in nz.min.js
			epResponse.defer(request.getResource(IResource.SCRIPT, "mimes/nz.min.js"));
		}
		
		epResponse.defer(HtmlFactory.createInlineScript(
			"nordzucker(window,document,jQuery,FWK," + 
				getResourceBundleAsJSON("scripts", cfg.getLocale()).toString() + 
			");", null));
		
		epResponse.defer(HtmlFactory.createComment("sven://kannengiesser"));
	}
	
	public JSON getResourceBundleAsJSON(String bundleName, Locale locale) {
		ResourceBundle resourceBundle = ResourceBundle.getBundle(bundleName, locale);
		Map<String, String> map = new TreeMap<String, String>();
		Set<String> keySet = resourceBundle.keySet();
		for (String key : keySet) {
			String value = resourceBundle.getString(key);
			map.put(key, value);
		}
		return JSONSerializer.toJSON(map);
	}
	
	public class HtmlString implements com.sapconsulting.portal.utils.html.elements.IHtmlElement {
		private String string = "";
		
		public HtmlString(String string) {
			this.string = string;
		}

		public boolean isXHTMLCompliant() {
			return false;
		}

		public IHtmlElement setNewLine(boolean bool) {
			return this;
		}

		public IHtmlElement setXHTMLCompliant(boolean bool) {
			return this;
		}

		public void destroy() {
		}

		public int getElementCount() {
			return 0;
		}

		public void output(StringBuffer buf) {
			buf.append(string);
		}

		public void output(PrintWriter writer) {
			writer.write(string);
		}

		public void output(Writer writer) {
			try {
				writer.write(string);
			} catch (IOException e) {
			}
		}
	}
}
