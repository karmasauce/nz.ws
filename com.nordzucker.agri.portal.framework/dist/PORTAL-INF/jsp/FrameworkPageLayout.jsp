<%
/** 
 * The framework page component is the central component that handles the rendering of all framework related HTML (and Javascript).
 * That is: 
 * - header with logo, search input, and toolbar links
 * - top level navigation
 * - navigation panel with detail navigation, related links, and dynamic navigation
 * - content area
 * 
 * Note the 'funny looking' usage of scriptlet begin and end tags that open in one line and close in (one of) the following line(s).
 * The reason for this is that the HTML output is much more compact and produces less 'empty' lines and whitespace. 
 */
%><%@ taglib prefix="nav" uri="NavigationTagLibrary" %><%
%><%@ taglib prefix="fwk" uri="FrameworkTagLibrary" %><%
%><%@ taglib prefix="lyt" uri="LayoutTagLibrary"%><%


/*
 * import statements
 */
%><%@ page import="java.util.*"
%><%@ page import="com.sap.portal.navigation.*"
%><%@ page import="com.sapportals.portal.navigation.*"
%><%@ page import="com.sapportals.portal.prt.component.*"
%><%@ page import="com.sapportals.portal.prt.pom.*"
%><%@ page import="com.sapportals.portal.prt.runtime.*"
%><%@ page import="com.nordzucker.agri.portal.framework.*"
%><%@ page import="com.nordzucker.agri.portal.framework.utils.*"

%><jsp:useBean id="cfg" scope="request" class="com.nordzucker.agri.portal.framework.Config" /><%

%><%!

String str(ResourceBundle rb, String key) {
	String string = rb.getString(key);
	if (string != null) {
		return string;
	}
	return key;
}

%><%
Locale locale = cfg.getLocale();
String lang = locale.getLanguage();
String userId = componentRequest.getUser().getUniqueName();

ResourceBundle rb = ResourceBundle.getBundle("framework", locale);
String portalPath = cfg.getPortalPath().replaceFirst("/login$", "");
INavigationNode storedNode0 = null;

NavigationNodes pathNodes = cfg.getPathNodes();
Hashtable environment = cfg.getEnvironment();

IPortalComponentProfile profile = componentRequest.getComponentContext().getProfile();
String baseRID = profile.getProperty("com.nordzucker.agri.portal.framework.BaseRID");
if (baseRID == null) baseRID = "";

String dynamic = profile.getProperty("com.nordzucker.agri.portal.framework.search.DynamicParameter");
if (dynamic == null) dynamic = "paraurl%3Dhttp%3A%2F%2Fgut205142%3A8080%2Fcps%2Frde%2Fxchg%2Fagriportal%2Fhs.xsl%2F#HTML#%26paraconfig%3DGUT205142_agriportal";

String httpHost = request.getServerPort() == 443 ? ("http://" + request.getServerName() + ":80") : "";
String httpsHost = request.getServerPort() == 80 ? ("https://" + request.getServerName() + ":443") : "";

String rssUrl = profile.getProperty("com.nordzucker.agri.portal.framework.RSSURL");
rssUrl = httpHost + rssUrl.replaceAll("#LANG#", lang);

long startTime = System.currentTimeMillis();
long stopTime = startTime;

//get ip from user
String ip = request.getHeader("X-Forwarded-For");  
if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
  ip = request.getHeader("Proxy-Client-IP");  
}  
if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
  ip = request.getHeader("WL-Proxy-Client-IP");  
}  
if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
  ip = request.getHeader("HTTP_CLIENT_IP");  
}  
if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
  ip = request.getHeader("HTTP_X_FORWARDED_FOR");  
}  
if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
  ip = request.getRemoteAddr();  
}  
%>
<div id="wrap"><div class="inner">

	<nav class="hidden">
		<h2><%=str(rb,"title.quick_links")%></h2>
		<ul>
			<li><a href="#content" accesskey="2" tabindex="1"><%=str(rb,"link.to_content")%></a></li>
			<li><a href="#searchForm" tabindex="1"><%=str(rb,"link.to_search")%></a></li>
			<li><a href="#main" tabindex="1"><%=str(rb,"link.to_main_navigation")%></a></li>
			<li><a href="#footer" tabindex="1"><%=str(rb,"link.to_footer")%></a></li>
		</ul>
	</nav><%
if (!cfg.isAnonymousUser()) {%>
	<div id="printableUser"><%=str(rb,"text.you_are_logged_in_as")%> <%=userId%><br><label id="printableBPLabel"><%=str(rb,"text.businesspartner")%></label> <span id="printableBP"></span></div><%
}%>
	<header id="header">
		<nav id="meta" role="banner"><div class="inner"><%
if (!cfg.isAnonymousUser()) {%>
			<ul id="welcome">
				<li><%
					%><span class="welcome_box"><%
						%><label class="welcome_label"><%=str(rb,"text.you_are_logged_in_as")%> </label><%
						%><span class="user_id"><%=userId%></span><%
					%></span><%
					%><label class="bpselect_label"><%=str(rb,"text.businesspartner")%></label><div class="bpselect_container"></div></li>
				<li><a id="logOut" title="<%=str(rb,"title.log_off_from_portal")%>" href="/irj/go/portal/prtmode/json/prtroot/nz.logout?logout_submit=true&url=<%=portalPath.replaceFirst("/login$", "")%>" data-external-url="<%=cfg.getExternalUrl()%>" data-silent="<%=cfg.isSilent()%>"><span class="hidden"><%=str(rb,"text.logoff")%></span></a></li>
			</ul><%
}
%>
			<ul id="fontSize">
				<li><a href="#" class="small" title="<%=str(rb,"title.small_font")%>">A-</a></li>
				<li class="sel"><a href="#" class="normal" title="<%=str(rb,"title.normal_font")%>">A</a></li>
				<li><a href="#" class="big" title="<%=str(rb,"title.big_font")%>">A+</a></li>
			</ul>
			<ul>
				<li><form id="searchForm" class="searchForm" role="search" data-dynamic="<%=dynamic%>" title="<%=str(rb,"title.search")%>"><input id="searchTerm" name="term" class="term"><button title="<%=str(rb,"title.search")%>"><%=str(rb,"button.search")%></button></form></li>
				<!--[if gt IE 8]--><li><button id="print" title="<%=str(rb,"title.print")%>"><%=str(rb,"button.print")%></button></li><!--[endif]-->
				<%
if (rssUrl != null && !rssUrl.equals("")) {
				%><li><a href="<%=rssUrl%>" id="rss" title="<%=str(rb,"title.rss_feed")%>"><span class="hidden"><%=str(rb,"button.rss_feed")%></span></a></li><%
}
%>
			</ul>
		</div></nav>
		<a id="logo" href="<%=portalPath%>"><img src="<%=componentRequest.getWebResourcePath()%>/mimes/logo.png"></a>
		<nav id="main" role="navigation">
			<ul class="menu-ul"><%
				%><nav:iterateInitialNavNodes currentNavNode="node0"><%
					boolean isInPath0 = NavNode.isInPath(node0, pathNodes);
				
					if (isInPath0) {
						%><nav:storeNavNode/><%
						storedNode0 = node0;
					}
				
					String filterID0 = (String) NavNode.getAttributeValue(node0, "com.sap.portal.navigation.view");
					if (filterID0 == null || !filterID0.equals("miscpages") || cfg.isDebug()) {
						
						boolean hasChildren0 = NavNode.hasChildren(node0, environment);
						
						if (isInPath0) {
							%><li class="menu-li sel"><%
						} else {
							%><li class="menu-li"><%
						}
						
						String quickLink0 = NavNode.getQuickLink(node0);
						String href0 = portalPath + (quickLink0 != null ? "/" + quickLink0 : "?NavigationTarget=" + node0.getHashedName());
						
						%><a class="menu-a" href="<%=href0%>" title="<%=NavNode.getDescription(node0, locale)%>"><%=NavNode.getTitle(node0, locale)%></a><%
						
						if (hasChildren0) {
							%><ul class="submenu-ul"><nav:iterateNavNodeChildren currentNavNode="node1"><%
							String mergeID1 = node1.getMergeID();
							boolean filteredByLang1 = false;
							if (mergeID1 != null // the node does have a Merge ID 
								&& mergeID1.lastIndexOf("_") == mergeID1.length() - 3 // the antepenultimate (=last but two) character is an underscore  
								&& !mergeID1.endsWith(lang)) // the language code doesn't equal the current request lang code 
							{
								filteredByLang1 = true; // exclude from rendering
							}
							if (!filteredByLang1) {
								boolean isInPath1 = NavNode.isInPath(node1, pathNodes);
								
								if (isInPath1) {
									%><li class="submenu-li sel"><%
								} else {
									%><li class="submenu-li"><%
								}
	
								String quickLink1 = NavNode.getQuickLink(node1);
								String href1 = portalPath + (quickLink0 != null && quickLink1 != null ? "/" + quickLink0 + "/" + quickLink1: "?NavigationTarget=" + node1.getHashedName());
				
								%><a class="submenu-a" href="<%=href1 %>" title="<%=NavNode.getDescription(node1, locale)%>"><%=NavNode.getTitle(node1, locale)%></a><%
								%></li><%
							}
							%></nav:iterateNavNodeChildren></ul><%
						}
						%></li>
						<%
					}
					%></nav:iterateInitialNavNodes><%
					if (cfg.isAnonymousUser()) {
						%><li class="menu-li logIn" data-href="<%=httpsHost%>/irj/go/portal/prtroot/nz.login_html5"><a class="menu-a" href="<%=portalPath%>/login"><%=str(rb,"link.my_pages")%></a>
							<ul class="submenu-ul">
								<li class="submenu-li"><em><%=str(rb,"text.my_pages")%></em></li>
								<li class="submenu-li"><button class="button small"><%=str(rb,"button.login")%></button></li>
							</ul>
						</li><%
					}
				%>
			</ul>
		</nav>
		<nav id="path"><%
			String filterID0 = (String) NavNode.getAttributeValue(storedNode0, "com.sap.portal.navigation.view");
			if (filterID0 == null || !filterID0.equals("miscpages") || cfg.isDebug()) {
			%>
			<ol class="path-ol">
				<nav:iterateNavNodesInSelectedPath currentNavNode="node"><%
					%><nav:ifHasMoreIterations><%
						%><li class="path-li"><a class="path-a" href="<%=portalPath%>?NavigationTarget=<%=node.getHashedName()%>&ExecuteLocally=true&NavPathUpdate=true"><nav:navNodeTitle/></a></li><%
					%></nav:ifHasMoreIterations><%
					%><nav:ifNotHasMoreIterations><%
						%><li class="path-li"><span class="path-span"><nav:navNodeTitle/></span></li><%
					%></nav:ifNotHasMoreIterations><%
				%></nav:iterateNavNodesInSelectedPath>
			</ol><%
			}
			%>
		</nav>
	</header>
	<div id="body" role="main">
		<nav:recallNavNode><%
		String quickLink0 = NavNode.getQuickLink(storedNode0);
		
		if ((filterID0 == null || !filterID0.equals("miscpages") || cfg.isDebug()) && NavNode.hasChildren(storedNode0, environment)) {
			%><nav id="sub"><div class="inner"><ul class="menu-ul"><%
			%><nav:iterateNavNodeChildren currentNavNode="node1"><%
			String mergeID1 = node1.getMergeID();
			boolean filteredByLang1 = false;
			if (mergeID1 != null // the node does have a Merge ID 
				&& mergeID1.lastIndexOf("_") == mergeID1.length() - 3 // the antepenultimate (=last but two) character is an underscore  
				&& !mergeID1.endsWith(lang)) // the language code doesn't equal the current request lang code 
			{
				filteredByLang1 = true; // exclude from rendering
			}
			if (!filteredByLang1) {

				if (NavNode.isInPath(node1, pathNodes)) {
					%><li class="menu-li sel"><%
				
						String quickLink1 = NavNode.getQuickLink(node1);
						String href1 = portalPath + (quickLink0 != null && quickLink1 != null ? "/" + quickLink0 + "/" + quickLink1 : "?NavigationTarget=" + node1.getHashedName());
						
						%><a class="menu-a" href="<%=href1%>" title="<%=NavNode.getDescription(node1, locale)%>"><%=NavNode.getTitle(node1, locale)%></a><%

						if (NavNode.hasChildren(node1, environment)) {
							%><ul class="submenu-ul"><nav:iterateNavNodeChildren currentNavNode="node2"><%
							String mergeID2 = node2.getMergeID();
							boolean filteredByLang2 = false;
							if (mergeID2 != null // the node does have a Merge ID 
								&& mergeID2.lastIndexOf("_") == mergeID2.length() - 3 // the antepenultimate (=last but two) character is an underscore  
								&& !mergeID2.endsWith(lang)) // the language code doesn't equal the current request lang code 
							{
								filteredByLang2 = true; // exclude from rendering
							}
							if (!filteredByLang2) {
								boolean isInPath2 = NavNode.isInPath(node2, pathNodes);
								if (isInPath2) {
									%><li class="submenu-li sel"><%
								} else {
									%><li class="submenu-li"><%
								}

								String quickLink2 = NavNode.getQuickLink(node2);
								String href2 = portalPath + (quickLink0 != null && quickLink1 != null && quickLink2 != null ? "/" + quickLink0 + "/" + quickLink1 + "/" + quickLink2: "?NavigationTarget=" + node2.getHashedName());
				
								%><a class="submenu-a" href="<%=href2%>" title="<%=NavNode.getDescription(node2, locale)%>"><%=NavNode.getTitle(node2, locale)%></a><%
								
								if (isInPath2 && NavNode.hasChildren(node2, environment)) {
									%><ul class="submenu-ul"><nav:iterateNavNodeChildren currentNavNode="node3"><%
										boolean isInPath3 = NavNode.isInPath(node3, pathNodes);
										if (isInPath3) {
											%><li class="submenu-li sel"><%
										} else {
											%><li class="submenu-li"><%
										}

										String quickLink3 = NavNode.getQuickLink(node3);
										String href3 = portalPath + (quickLink0 != null && quickLink1 != null && quickLink2 != null && quickLink3 != null ? "/" + quickLink0 + "/" + quickLink1 + "/" + quickLink2 + "/" + quickLink3: "?NavigationTarget=" + node3.getHashedName());
						
										%><a class="submenu-a" href="<%=href3%>" title="<%=NavNode.getDescription(node3, locale)%>"><%=NavNode.getTitle(node3, locale)%></a><%
									
										if (isInPath3 && NavNode.hasChildren(node3, environment)) {
											%><ul class="submenu-ul"><nav:iterateNavNodeChildren currentNavNode="node4"><%
												if (NavNode.isInPath(node4, pathNodes)) {
													%><li class="submenu-li sel"><%
												} else {
													%><li class="submenu-li"><%
												}

												String quickLink4 = NavNode.getQuickLink(node4);
												String href4 = portalPath + (quickLink0 != null && quickLink1 != null && quickLink2 != null && quickLink3 != null && quickLink4 != null ? "/" + quickLink0 + "/" + quickLink1 + "/" + quickLink2 + "/" + quickLink3 + "/" + quickLink4: "?NavigationTarget=" + node4.getHashedName());
								
												%><a class="submenu-a" href="<%=href4%>" title="<%=NavNode.getDescription(node4, locale)%>"><%=NavNode.getTitle(node4, locale)%></a><%
														
												%></li><%
											%></nav:iterateNavNodeChildren></ul><%
										}
										%></li><%
									%></nav:iterateNavNodeChildren></ul><%
								}		
								%></li><%
							}
							%></nav:iterateNavNodeChildren></ul><%
						}
					%></li><%
				} else {
					%><li class="menu-li"><%
							
						String quickLink1 = NavNode.getQuickLink(node1);
						String href1 = portalPath + (quickLink0 != null && quickLink1 != null ? "/" + quickLink0 + "/" + quickLink1 : "?NavigationTarget=" + node1.getHashedName());
						
						%><a class="menu-a" href="<%=href1%>" title="<%=NavNode.getDescription(node1, locale)%>"><%=NavNode.getTitle(node1, locale)%></a><%

					%></li><%
				}
			}
			%></nav:iterateNavNodeChildren><%
			%></ul></div></nav><%
		}
		%></nav:recallNavNode>
		<lyt:template eliminatemessagebar="true"><lyt:container id="content" wrappingMethod="none"/></lyt:template>
	</div>

</div></div>

<footer id="footer"><div class="inner">
	<nav id="footerContact">
<%= HTMLFragmentFromKM.getHTMLFragment(baseRID + "/footerContact.html", locale, portalPath) %>
	</nav>
	<nav id="footerSites">
		<h3><%=str(rb,"headline.subsidiaries")%></h3>
<%= HTMLFragmentFromKM.getHTMLFragment(baseRID + "/footerSites.html", locale, portalPath) %>
	</nav>
	<nav id="footerNav">
		<h3><%=str(rb,"headline.rubrics")%></h3>
		<ul><%
			%><nav:iterateInitialNavNodes currentNavNode="node0"><%
			filterID0 = (String) NavNode.getAttributeValue(node0, "com.sap.portal.navigation.view");
			if (filterID0 == null || !filterID0.equals("miscpages") || cfg.isDebug()) {%>
			<li><%
				
				String quickLink0 = NavNode.getQuickLink(node0);
				String href0 = portalPath + (quickLink0 != null ? "/" + quickLink0 : "?NavigationTarget=" + node0.getHashedName());
				
				%><a href="<%=href0%>" title="<%=NavNode.getDescription(node0, locale)%>"><%=NavNode.getTitle(node0, locale)%></a><%
				%></li><%
			}
			%></nav:iterateInitialNavNodes><%
			if (cfg.isAnonymousUser()) {
				%><li class="logIn" data-href="<%=httpsHost%>/irj/go/portal/prtroot/nz.login_html5"><a href="<%=portalPath%>/login"><%=str(rb,"link.my_pages")%></a></li><%
			}
			%>
		</ul>
	</nav>
	<nav id="footerInfo">
		<h3>Browserinfo</h3>
		<ul>
			<!--Content from NZ-->
			<li><a href="#" id="browserData">"<%=str(rb,"info.whatbrowserdoihave")%>"</a></li>
		</ul>
	</nav>
	<!-- 
	<form id="footerSearch" class="searchForm" role="search" data-dynamic="<%=dynamic%>">
		<h3><%=str(rb,"headline.search")%></h3>
		<input id="footerSearchTerm" name="term" class="term" placeholder="<%=str(rb,"placeholder.search")%>">
		<button class="button"><%=str(rb,"button.search")%></button>
	</form> -->
	<section id="footerCopyright">
<%= HTMLFragmentFromKM.getHTMLFragment(baseRID + "/footerCopyright.html", locale, portalPath) %>
	</section>
</div></footer>
<%
stopTime = System.currentTimeMillis();

if (cfg.isDebug()) {%>
<table align=center>
	<tr><th align=right>time</th><td>&nbsp;=&nbsp;</td><td><%=(stopTime - startTime)%></td></tr>
	<tr><th colspan=3 align=center><hr></th></tr>
	<tr><th align=right>IPortalComponentRequest.getLocale()</th><td>&nbsp;=&nbsp;</td><td><%=componentRequest.getLocale()%></td></tr>
	<tr><th align=right>HttpServletRequest.getLocale()</th><td>&nbsp;=&nbsp;</td><td><%=request.getLocale()%></td></tr>
	<tr><th align=right>IUserContext.getLocale()</th><td>&nbsp;=&nbsp;</td><td><%=componentRequest.getUser().getLocale()%></td></tr>
	<tr><th align=right>IUserContext.getCountry()</th><td>&nbsp;=&nbsp;</td><td><%=componentRequest.getUser().getCountry()%></td></tr>
	<tr><th colspan=3 align=center><hr></th></tr>
	<tr><th align=right rowspan=3>selected node</th><td>&nbsp;=&nbsp;</td><td><%=cfg.getSelectedNode().getName()%></td></tr>
	<tr><td>&nbsp;=&nbsp;</td><td><%=cfg.getSelectedNode().getHashedName()%></td></tr>
	<tr><td>&nbsp;=&nbsp;</td><td><%=cfg.getSelectedNode().getTitle(locale)%></td></tr>
	<tr><th align=right rowspan=3>launched node</th><td>&nbsp;=&nbsp;</td><td><%=cfg.getLaunchedNode().getName()%></td></tr>
	<tr><td>&nbsp;=&nbsp;</td><td><%=cfg.getLaunchedNode().getHashedName()%></td></tr>
	<tr><td>&nbsp;=&nbsp;</td><td><%=cfg.getLaunchedNode().getTitle(locale)%></td></tr>
	<tr><th align=right rowspan=3>context node</th><td>&nbsp;=&nbsp;</td><td><%=cfg.getContextNode().getName()%></td></tr>
	<tr><td>&nbsp;=&nbsp;</td><td><%=cfg.getContextNode().getHashedName()%></td></tr>
	<tr><td>&nbsp;=&nbsp;</td><td><%=cfg.getContextNode().getTitle(locale)%></td></tr>
	<tr><th colspan=3 align=center><hr></th></tr>
	<tr><th align=right>launchURL</th><td>&nbsp;=&nbsp;</td><td><%=cfg.getLaunchURL()%></td></tr>
	<tr><th align=right>isOTPage</th><td>&nbsp;=&nbsp;</td><td><%=cfg.isOTPage()%></td></tr>
	<tr><th align=right>isOTConnectorPage</th><td>&nbsp;=&nbsp;</td><td><%=cfg.isOTConnectorPage()%></td></tr>
	<tr><th align=right>isEmbeddablePage</th><td>&nbsp;=&nbsp;</td><td><%=cfg.isEmbeddablePage()%></td></tr>
	<tr><th colspan=3 align=center><hr></th></tr>
	<tr><th align=right>parameter: pageTitle</th><td>&nbsp;=&nbsp;</td><td><%=componentRequest.getParameter("pageTitle")%></td></tr>
	<tr><th align=right>parameter: paraurl</th><td>&nbsp;=&nbsp;</td><td><%=componentRequest.getParameter("paraurl")%></td></tr>
</table><%
}%>
<div class="overlay large content" id="searchResultsOvrl" data-src="/irj/go/portal/prtroot/nz.search"></div>
<div class="overlay content" id="popupBlockerDetectedOvrl">
<%= HTMLFragmentFromKM.getHTMLFragment(baseRID + "/popupBlockerDetected.html", locale, portalPath) %>
</div>
<script>
var PBD = {
	mimesPath: '<%=componentRequest.getWebResourcePath()%>/'
}
</script>
