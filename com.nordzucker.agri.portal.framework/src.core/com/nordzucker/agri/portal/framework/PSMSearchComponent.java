package com.nordzucker.agri.portal.framework;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ResourceBundle;

import com.opentext.sap.navigation.DSUrlMapping;
import com.opentext.sap.search.SearchFacade;
import com.sap.portal.navigation.IAliasHelper;
import com.sap.portal.navigation.IAliasService;
import com.sap.security.api.IUser;
import com.sap.security.api.UMFactory;
import com.sapportals.portal.navigation.NavigationEventsHelperService;
import com.sapportals.portal.prt.component.AbstractPortalComponent;
import com.sapportals.portal.prt.component.IPortalComponentRequest;
import com.sapportals.portal.prt.component.IPortalComponentResponse;
import com.sapportals.portal.prt.runtime.PortalRuntime;
import com.sapportals.portal.prt.util.StringUtils;
import com.sapportals.wcm.repository.IProperty;
import com.sapportals.wcm.repository.IPropertyName;
import com.sapportals.wcm.service.indexmanagement.retrieval.search.ISearchResult;

public class PSMSearchComponent extends AbstractPortalComponent {
	static final int RESULTS_PER_PAGE = 20;

	public void doContent(IPortalComponentRequest request, IPortalComponentResponse response) {
		ResourceBundle rb = ResourceBundle.getBundle("search", request.getLocale());

		NavigationEventsHelperService navHelperService = (NavigationEventsHelperService) PortalRuntime.getRuntimeResources().getService(NavigationEventsHelperService.KEY);
		boolean isAnonymousUser = navHelperService.isAnonymousUser(request);

		IAliasHelper aliasHelper = (IAliasHelper) PortalRuntime.getRuntimeResources().getService(IAliasService.KEY);
		String portalPath = aliasHelper.getPath(request);

		String term = request.getParameter("term");
		if (term == null) {
			term = "";
		}
		String xssTerm = StringUtils.escapeToHTML(term);
		int pg = 1;
		String page = request.getParameter("page");
		if (page != null) {
			try {
				pg = Integer.parseInt(page);
			} catch (NumberFormatException e) {
			}
		}

		String dynamic = request.getParameter("dynamic");
		if (dynamic == null) {
			dynamic = "paraurl%3Dhttp%3A%2F%2Fgut210142%3A8080%2Fcps%2Frde%2Fxchg%2Fagriportal%2Fhs.xsl%2F#HTML#%26paraconfig%3DGUT210142_agriportal";
		}

		response.write("<button class='close closeOvrl'>");
		response.write(str(rb, "button.close"));
		response.write("</button>");
		response.write("<h1>");
		response.write(str(rb, "text.headline").replaceFirst("\\$1", "<span class='term'>" + xssTerm + "</span>"));
		if (!isAnonymousUser) {
			response.write("<a href='#' class='searchForDocs'>");
			response.write(str(rb, "text.search_for_docs"));
			response.write("</a>");
			response.write("<a href='#' class='searchForPages'>");
			response.write(str(rb, "text.search_for_pages"));
			response.write("</a>");
		}
		response.write("</h1>");

		try {
			IUser user = UMFactory.getAuthenticator().getLoggedInUser();
//
////			IIndexService indexService = (IIndexService) ResourceFactory.getInstance().getServiceFactory().getService(IServiceTypesConst.INDEX_SERVICE);
////			List activeIndexes = indexService.getActiveIndexes();
//			String[] indexes = null;
//
//			SearchFacade searchFacade = SearchFacade.getInstance();
//			IQueryEntryList searchQuery = searchFacade.createSearchQuery(term, request.getLocale());
//			ISearchSession session = searchFacade.search(user, searchQuery, indexes);
//
//			try {
//				// search for pages
//				response.write("<div id='searchInKM'>");
//				response.write("<form class='ovrlControl searchForm' data-dynamic='" + dynamic + "'><input class='term' type='text' value='" + term + "'><button class='button' type='submit'>");
//				response.write(str(rb, "button.search"));
//				response.write("</button></form>");
//
//				int totalNumberOfResults = session.getTotalNumberResultKeys();
//
//				ISearchResultList searchResults = session.getSearchResults(1 + (pg - 1) * RESULTS_PER_PAGE, RESULTS_PER_PAGE + (pg - 1) * RESULTS_PER_PAGE);
//
//				response.write("<p title='");
//				response.write((1 + (pg - 1) * RESULTS_PER_PAGE) + " - " + (RESULTS_PER_PAGE + (pg - 1) * RESULTS_PER_PAGE) + " / " + searchResults.size());
//				response.write("'>");
//				response.write(str(rb, "text.results").replaceFirst("\\$1", "<strong class='term'>" + xssTerm + "</strong>").replaceFirst("\\$2", "<span>" + totalNumberOfResults + "</span>"));
//				response.write("</p>");
//
//				response.write("<header class='ovrlControl'>");
//				if (totalNumberOfResults > RESULTS_PER_PAGE) {
//					long P = (long) (Math.ceil(totalNumberOfResults / RESULTS_PER_PAGE)) + 1;
//					response.write("<nav class='paginator' data-term='" + term + "'>");
//					for (long p = 1; p <= P; p++) {
//						if (p == pg) {
//							response.write("<strong>" + p + "</strong>");
//						} else {
//							response.write("<a href='#' data-page='" + p + "'>" + p + "</a>");
//						}
//					}
//					response.write("</nav>");
//
//					response.write("<nav class='prevNext' data-term='" + term + "'>");
//					if (pg > 1) {
//						response.write("<a href='#' data-page='" + (pg - 1) + "' class='prev'>");
//						response.write(str(rb, "button.prev_page"));
//						response.write("</a>");
//					} else {
//						response.write("<span class='prev'>");
//						response.write(str(rb, "button.prev_page"));
//						response.write("</span>");
//						;
//					}
//					if (pg < P) {
//						response.write("<a href='#' data-page='" + (pg + 1) + "' class='next'>");
//						response.write(str(rb, "button.next_page"));
//						response.write("</a>");
//					} else {
//						response.write("<span class='next'>");
//						response.write(str(rb, "button.next_page"));
//						response.write("</span>");
//						;
//					}
//					response.write("</nav>");
//				}
//
//				response.write("</header>");
//
//				response.write("<ol start='" + pg + "' id='searchResultsList'>");
//
//				try {
//					int i = 0;
//					ISearchResultListIterator iter = searchResults.listIterator();
//					while (iter.hasNext()) {
//						i += 1;
//						ISearchResult searchResult = iter.next();
//
//						response.write("<li id='hit_" + i + "' class='hit'>");
//						response.write("<article>");
//
//						IResource resource = searchResult.getResource();
//						IMime mime = MimeHandlerServiceFactory.getInstance().getExtensionSpecific(resource);
//						String imgSrc = mime.getImagePath();
//						String mimeType = mime.getType();
//						if (mimeType != null && mimeType.equals("text/html")) {
//							imgSrc = request.getWebResourcePath() + "/mime-icons/html@2x.png";
//						}
//
//						String displayName = resource.getDisplayName(true);
//
//						String linkUrl = "";
//						String linkTitle = null;
//
//						String contentLink = getContentLink(searchFacade, searchResult);
//
//						if (contentLink == null) {
//							// writeMessage("No content link property was found on index or property has no value.");
//							// writeLine("<li>" + contentSnippet + "</li>");
//						} else {
//							boolean isSearchResultPartOfNavigation = false;
//
//							// identify mapping for Management Server GUID
//							DSUrlMapping dsUrlMapping = getDsUrlMapping(user, searchFacade, contentLink);
//
//							if (dsUrlMapping != null) {
//								isSearchResultPartOfNavigation = true;
//							} else {
//								// writeMessage("No mapping was found for user " + user.getUniqueName() + " and cms GUID mapping key: " + mappingKeys.getCmsIdKey());
//							}
//
//							if (dsUrlMapping != null) {
//								// writeResultLink(dsUrlMapping,
//								// isSearchResultPartOfNavigation, contentLink,
//								// contentSnippet);
//
//								String mappedPortalUrl = dsUrlMapping.getPortalUrl();
//								if (isSearchResultPartOfNavigation) {
//									// Case 1: Search result is part of the navigation
//									linkUrl = portalPath + "?NavigationTarget=" + mappedPortalUrl;
//									
//									INavigationService navService = (INavigationService) PortalRuntime.getRuntimeResources().getService(INavigationService.KEY);
//									INavigationNode node = navService.getNode(navHelperService.getEnvironment(request), mappedPortalUrl);
//									if (node != null) {
//										linkTitle = node.getTitle(request.getLocale());
//									}
//
//									// writeLine("<li><a href=\"#\" onClick=\"return parent.EPCM.doNavigate('" + mappedPortalUrl + "');\">" + contentSnippet + "</a></li>");
//								} else {
//									// Case 2: Search result is not part of the navigation, but we can use another navigation context.
//
//									// The path should point to a SmartView-iView under the anchor node of a dynamically linked navigation tree.
//									String iviewPath = portalPath + "/generic_smartview";
//									linkUrl = iviewPath + "?DynamicParameter=paraurl%3d" + contentLink + "&NavigationContext=" + mappedPortalUrl;
//
//									// writeLine("<li><a href=\"#\" onClick=\"return parent.EPCM.doNavigate('" + targetUrl + "', 0, '', '', 2, '', '" + contextUrl + "');\">" + contentSnippet + "</a></li>");
//								}
//							} else {
//								// writeMessage("Use Delivery Server URL for the result link.");
//								// Case 3: Search result is not part of the
//								// navigation and no
//								// other navigation context exists
//								linkUrl = contentLink;
//
//								// writeLine("<li><a href=\"" + contentLink + "\">" + contentSnippet + "</a></li>");
//							}
//						}
//
//						if (linkTitle != null) {
//							response.write("<h2 class='title'><a href='");
//							response.write(linkUrl);
//							response.write("' class='link' target='_blank'>");
//							response.write(linkTitle);
//							response.write("</a></h2>");
//						}
//
//						response.write("<p class='snippet'><a href='");
//						response.write(linkUrl);
//						response.write("' target='_blank'>");
//						String contentSnippet = searchResult.getContentSnippet();
//						if (contentSnippet != null) {
//							response.write(contentSnippet);
//						} else {
//							response.write(displayName);
//						}
//						response.write("</a></p>");
//
//						response.write("<footer>");
//						response.write("<dl>");
//
//						response.write("<dt class='info preview'>");
//						response.write(str(rb, "text.preview"));
//						response.write("</dt>");
//						response.write("<dd class='info preview'><a href='");
//						response.write(linkUrl);
//						response.write("' target='_blank'><img src='");
//						response.write(imgSrc);
//						response.write("' title='");
//						response.write(mimeType);
//						response.write("'></a></dd>");
//
//						response.write("<dt class='info relevance'>");
//						response.write(str(rb, "text.relevance"));
//						response.write("</dt>");
//						float rankValue = searchResult.getRankValue();
//						int relevance = Math.round(rankValue * 100);
//						response.write("<dd class='info relevance'><a href='");
//						response.write(linkUrl);
//						response.write("' target='_blank'><meter min='0' max='100' value='" + relevance + "' title='" + relevance + "%'");
//						response.write(relevance + "%");
//						response.write("</meter></a></dd>");
//
//						// response.write("<dt class='info category'>Kategorien</dt>");
//						// response.write("<dd class='info category'><a href='first-category.html' class='link'>Erste Kategorie</a></dd>");
//						// response.write("<dd class='info category'><a href='second-category.html' class='link'>Zweite Kategorie</a></dd>");
//
//						// response.write("<dt class='info fsize'>Dateigrš§e</dt>");
//						// response.write("<dd class='info fsize'>2 <abbr title='kilobyte'>kB</abbr></dd>");
//						// response.write("<dt class='info modified'>Letzte €nderung</dt>");
//						// response.write("<dd class='info modified'><time datetime='2012-11-20T12:34:56Z'>Heute, 12:34 Uhr</time></dd>");
//
//						response.write("</dl>");
//						response.write("</footer>");
//						response.write("</article>");
//						response.write("</li>");
//
//						// IPropertyMap localProperties =
//						// result.getLocalProperties();
//						// for (IPropertyIterator it =
//						// localProperties.iterator(); it.hasNext(); ) {
//						// IProperty property = it.next();
//						// Object value = property.getValue();
//						// jsonProperties.put(property.getPropertyName().getName(),
//						// value instanceof Date ? ((Date) value).toString() :
//						// value);
//						// }
//					}
//				} catch (Exception e) {
//					response.write("<li>");
//					writeStackTrace(response, e);
//					response.write("</li>");
//				}
//				response.write("</ol>");
//				response.write("<footer class='ovrlControl'>");
//				if (totalNumberOfResults > RESULTS_PER_PAGE) {
//					long P = (long) (Math.ceil(totalNumberOfResults / RESULTS_PER_PAGE)) + 1;
//					response.write("<nav class='paginator' data-term='" + term + "'>");
//					for (long p = 1; p <= P; p++) {
//						if (p == pg) {
//							response.write("<strong>" + p + "</strong>");
//						} else {
//							response.write("<a href='#' data-page='" + p + "'>" + p + "</a>");
//						}
//					}
//					response.write("</nav>");
//				}
//				response.write("<button type='submit' id='closeSearch' class='closeOvrl'>");
//				response.write(str(rb, "button.close"));
//				response.write("</button>");
//				response.write("</footer>");
//				response.write("</div>");
//
//				// search for documents
//				if (!isAnonymousUser) {
//					response.write("<div id='searchInPCM'>");
//					response.write("<div id='searchInPCMFrame' data-src='");
//					response.write("/irj/servlet/prt/portal/prtroot/pcd!3aportal_content!2fnz_agri!2fadmin!2fnavigation!2fadm.pcm_search?opentext.queryString=");
//					response.write(term);
//					response.write("'>");
//					response.write("</div>");
//					response.write("<footer class='ovrlControl'>");
//					response.write("<button type='submit' id='closeSearch' class='closeOvrl'>");
//					response.write(str(rb, "button.close"));
//					response.write("</button>");
//					response.write("</footer>");
//					response.write("</div>");
//				}
//			} catch (ResourceException e) {
//				writeStackTrace(response, e);
//			} catch (WcmException e) {
//				writeStackTrace(response, e);
//			}
		} catch (Exception e) {
			writeStackTrace(response, e);
		}
	}

	private void writeStackTrace(IPortalComponentResponse response, Exception e) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		response.write("<pre>");
		response.write(sw.toString());
		response.write("</pre>");
	}

	private DSUrlMapping getDsUrlMapping(IUser user, SearchFacade searchFacade, String contentLink) {
		DSUrlMapping dsUrlMapping = null;

		SearchFacade.MappingKeys mappingKeys = searchFacade.getMappingKeys(user, contentLink);

		dsUrlMapping = searchFacade.getDSUrlMapping(user, mappingKeys.getCmsIdKey());
		if (dsUrlMapping == null) {
			// writeMessage("Find alternative NavigationContext for the resource.");
			dsUrlMapping = searchFacade.getDSUrlMapping(user, mappingKeys.getCmsNavigationIdKey());
			if (dsUrlMapping == null) {
				// writeMessage("No mapping was found for user " + user.getUniqueName() + " and Management Server Navigation GUID mapping key:" + mappingKeys.getCmsNavigationIdKey());
			}
		}
		return dsUrlMapping;
	}

	private String getContentLink(SearchFacade searchFacade, ISearchResult searchResult) {
		String contentLink = null;
		IPropertyName contentLinkPropertyName = searchFacade.createContentLinkPropertyName();
		IProperty contentLinkProperty = searchFacade.getPropertyFromIndex(searchResult, contentLinkPropertyName);
		if (contentLinkProperty != null) {
			contentLink = contentLinkProperty.getValueAsString();
		}
		return contentLink;
	}

	private String str(ResourceBundle rb, String key) {
		String string = rb.getString(key);
		if (string != null) {
			return string;
		}
		return key;
	}
}