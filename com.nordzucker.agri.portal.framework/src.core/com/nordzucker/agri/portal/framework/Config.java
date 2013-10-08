/**
 * 
 */
package com.nordzucker.agri.portal.framework;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.nordzucker.agri.portal.framework.utils.NavNode;
import com.sap.engine.lib.xml.util.StringUtils;
import com.sap.portal.navigation.IAliasHelper;
import com.sap.portal.navigation.IAliasService;
import com.sapportals.portal.navigation.INavigationNode;
import com.sapportals.portal.navigation.INavigationService;
import com.sapportals.portal.navigation.NavigationEventsHelperService;
import com.sapportals.portal.navigation.NavigationNodes;
import com.sapportals.portal.prt.component.IPortalComponentRequest;
import com.sapportals.portal.prt.runtime.PortalRuntime;

/**
 * @author D037963
 *
 */
public class Config {

	private static final Map<String, String> LANGUAGES = new HashMap<String, String>();

	static {
		LANGUAGES.put("en", "English");
		LANGUAGES.put("de", "Deutsch");
		LANGUAGES.put("da", "Dansk");   // da -- language | DK -- country
		LANGUAGES.put("sv", "Svenska"); // sv -- language | SE -- country
		LANGUAGES.put("pl", "Polski");
		LANGUAGES.put("sk", "Slovenčina");
		LANGUAGES.put("fi", "Suomi");
		LANGUAGES.put("lt", "Lietuvos");
	}

	private static final Map<String, String> COUNTRIES = new HashMap<String, String>();

	static {
		COUNTRIES.put("EN", "en");
		COUNTRIES.put("DE", "de"); // Germany
		COUNTRIES.put("DK", "da"); // Danmark
		COUNTRIES.put("SE", "sv"); // Sweden
		COUNTRIES.put("PL", "pl"); // Poland
		COUNTRIES.put("SK", "sk"); // Slowakia
		COUNTRIES.put("FI", "fi"); // Finland
		COUNTRIES.put("LT", "lt"); // Lithuania
	}
	
	private boolean isHomePage = false;
	private boolean isOTPage = false;
	private boolean isEmbeddablePage = false;
	private boolean isOTConnectorPage = false;
	private boolean isAnonymousUser = false;
	@SuppressWarnings("rawtypes")
	private Hashtable environment = null;
	private INavigationNode selectedNode;
	private INavigationNode launchedNode;
	private INavigationNode contextNode;
	private INavigationNode personalizePortalNode;
	private INavigationNode homeNode;
	private NavigationNodes pathNodes;
	private String launchURL;
	private String portalPath;
	private String mimesPath;
	private String assetsPath;
	private String pageTitle;
	private String pageDescription;
	private Locale locale;
	
	boolean isDebug = false;

	public Config(IPortalComponentRequest request) {
		Boolean fwkDebug = (Boolean) request.getComponentSession().getValue("fwkDebug");
		if (fwkDebug == null) {
			fwkDebug = Boolean.FALSE;
		}
		String fwkDebugParam = request.getParameter("fwkDebug");
		if (fwkDebugParam != null) {
			if (fwkDebugParam.equals("off") || fwkDebugParam.equals("false") || fwkDebugParam.equals("0")) {
				request.getComponentSession().removeValue("fwkDebug");
				fwkDebug = Boolean.FALSE;
			} else {
				request.getComponentSession().putValue("fwkDebug", Boolean.TRUE);
				fwkDebug = Boolean.TRUE;
			}
		}
		isDebug = fwkDebug.booleanValue();
		
		NavigationEventsHelperService navHelperService = (NavigationEventsHelperService) PortalRuntime.getRuntimeResources().getService(NavigationEventsHelperService.KEY);
		INavigationService navService = (INavigationService) PortalRuntime.getRuntimeResources().getService(INavigationService.KEY);
		
		environment = navHelperService.getEnvironment(request);
		
		isAnonymousUser = navHelperService.isAnonymousUser(request);
		
		selectedNode = navHelperService.getCurrentNavNode(request);
		contextNode = navHelperService.getCurrentContextNavNode(request);
		launchedNode = navHelperService.getCurrentLaunchNavNode(request);
		personalizePortalNode = navHelperService.getPersonalizePortalNode(request);
		pathNodes = NavNode.getPathNodes(request);
		
		homeNode = navService.getNodeByQuickLink(environment, "home");
		if (homeNode == null) {
			NavigationNodes realInitialNodes = navHelperService.getRealInitialNodes(request);
			if (realInitialNodes != null && realInitialNodes.size() > 0) {
				homeNode = (INavigationNode) realInitialNodes.get(0);
			}
		}
		
		IAliasHelper aliasHelper = (IAliasHelper) PortalRuntime.getRuntimeResources().getService(IAliasService.KEY);
		portalPath = aliasHelper.getPath(request);
		
		String webResourcePath = request.getWebResourcePath();
		mimesPath = webResourcePath + "/mimes/";
		assetsPath = webResourcePath + "/assets/";
		
		locale = request.getLocale();
		
		String ntParam = request.getParameter("NavigationTarget");
		String qlParam = request.getParameter("QuickLink");
		if ((ntParam == null && qlParam == null) || ("home".equals(qlParam))) {
			isHomePage = true;
		} else {
			isHomePage = false;
		}
		
		if (launchedNode != null) {
			launchURL = launchedNode.getLaunchURL();

			if (launchURL != null) {
				if (launchURL.endsWith("com.opentext.sap.pcd.iview.smartview")) {
					isOTPage = true;
				}
				else if (launchURL.endsWith("com.opentext.pct.wsmpm.smartview")) {
					isOTPage = true;
				}
				else if (launchedNode.getNavConnectorNamePrefix().equals("OPENTEXT_EXTERNAL")) {
					isOTPage = true;
					isOTConnectorPage = true;
				}
				if (NavNode.getAttributeValue(request, launchURL, "com.nordzucker.agri.portal.framework.EmbeddablePage") != null) {
					isEmbeddablePage = true;
				}
			}
		}
		
		if (isOTPage && !isOTConnectorPage && selectedNode != null) {
			pageTitle = selectedNode.getTitle(locale);
			pageDescription = selectedNode.getDescription(locale);
		}
		else if (launchedNode != null) {
			if (NavNode.isInvisible(launchedNode)) {
				pageTitle = selectedNode.getTitle(locale);
				pageDescription = selectedNode.getDescription(locale);
			} else {
				pageTitle = launchedNode.getTitle(locale);
				pageDescription = launchedNode.getDescription(locale);
			}
		}
		// overwrite the pageTitle if a parameter "pageTitle" is present
		String pageTitleParam = request.getParameter("pageTitle");
		if (pageTitleParam != null) {
			pageTitle = StringUtils.unescapeURL(pageTitleParam);
		}
	}
	

	/**
	 * Create and hang a Config object to the request.
	 * @param request
	 * @return
	 */
	public static Config getInstance(IPortalComponentRequest request) {
		HttpServletRequest servletRequest = request.getServletRequest();
		Config cfg = (Config) servletRequest.getAttribute("cfg");
		if (cfg == null) {
			cfg = new Config(request);
			servletRequest.setAttribute("cfg", cfg);
		}
		return cfg;
	}

	
	@SuppressWarnings("rawtypes")
	public Hashtable getEnvironment() {
		return environment;
	}

	public boolean isHomePage() {
		return isHomePage;
	}

	public boolean isOTPage() {
		return isOTPage;
	}
	
	public boolean isOTConnectorPage() {
		return isOTConnectorPage;
	}

	public boolean isEmbeddablePage() {
		return isEmbeddablePage;
	}

	public boolean isAnonymousUser() {
		return isAnonymousUser;
	}

	public INavigationNode getSelectedNode() {
		return selectedNode;
	}

	public INavigationNode getLaunchedNode() {
		return launchedNode;
	}

	public INavigationNode getHomeNode() {
		return homeNode;
	}

	public INavigationNode getContextNode() {
		return contextNode;
	}

	public INavigationNode getPersonalizePortalNode() {
		return personalizePortalNode;
	}

	public NavigationNodes getPathNodes() {
		return pathNodes;
	}

	public String getLaunchURL() {
		return launchURL;
	}

	public String getPortalPath() {
		return portalPath;
	}

	public String getMimesPath() {
		return mimesPath;
	}

	public String getAssetsPath() {
		return assetsPath;
	}

	public String getPageTitle() {
		return pageTitle;
	}

	public String getPageDescription() {
		return pageDescription;
	}

	public Locale getLocale() {
		return locale;
	}
	
	public String getLanguageName() {
		return LANGUAGES.get(locale.toString());
	}
	
	public String getLanguageName(String lang) {
		return LANGUAGES.get(lang);
	}

	public static Map<String, String> getSupportedLanguages() {
		return LANGUAGES;
	}

	public static Map<String, String> getSupportedCountries() {
		return COUNTRIES;
	}
	
	public boolean isDebug() {
		return isDebug;
	}
}
