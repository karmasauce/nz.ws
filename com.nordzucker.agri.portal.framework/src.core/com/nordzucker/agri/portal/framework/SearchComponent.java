package com.nordzucker.agri.portal.framework;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.TimeZone;

import com.sap.portal.navigation.IAliasHelper;
import com.sap.portal.navigation.IAliasService;
import com.sapportals.portal.navigation.NavigationEventsHelperService;
import com.sapportals.portal.prt.component.AbstractPortalComponent;
import com.sapportals.portal.prt.component.IPortalComponentRequest;
import com.sapportals.portal.prt.component.IPortalComponentResponse;
import com.sapportals.portal.prt.runtime.PortalRuntime;
import com.sapportals.portal.prt.session.IUserContext;
import com.sapportals.portal.prt.util.StringUtils;
import com.sapportals.wcm.WcmException;
import com.sapportals.wcm.control.released.search.SearchQueryListBuilder;
import com.sapportals.wcm.repository.IProperty;
import com.sapportals.wcm.repository.IPropertyIterator;
import com.sapportals.wcm.repository.IPropertyMap;
import com.sapportals.wcm.repository.IPropertyName;
import com.sapportals.wcm.repository.IResource;
import com.sapportals.wcm.repository.IResourceContext;
import com.sapportals.wcm.repository.IResourceFactory;
import com.sapportals.wcm.repository.PropertyName;
import com.sapportals.wcm.repository.ResourceException;
import com.sapportals.wcm.repository.ResourceFactory;
import com.sapportals.wcm.service.IServiceTypesConst;
import com.sapportals.wcm.service.indexmanagement.IIndexService;
import com.sapportals.wcm.service.indexmanagement.IWcmIndexConst;
import com.sapportals.wcm.service.indexmanagement.retrieval.search.IFederatedSearch;
import com.sapportals.wcm.service.indexmanagement.retrieval.search.IQueryEntryList;
import com.sapportals.wcm.service.indexmanagement.retrieval.search.ISearchResult;
import com.sapportals.wcm.service.indexmanagement.retrieval.search.ISearchResultList;
import com.sapportals.wcm.service.indexmanagement.retrieval.search.ISearchResultListIterator;
import com.sapportals.wcm.service.indexmanagement.retrieval.search.ISearchSession;
import com.sapportals.wcm.service.mimehandler.IMime;
import com.sapportals.wcm.service.mimehandler.MimeHandlerServiceFactory;
import com.sapportals.wcm.util.content.IContent;


public class SearchComponent extends AbstractPortalComponent {
	static final int RESULTS_PER_PAGE = 20;
    private static final String KM_NAMESPACE = "http://sapportals.com/xmlns/cm";
    private static final String SEARCH_PARAM_ACTION_LINGUISTIC = "LINGUISTIC";      

	private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
	private static final SimpleDateFormat LAST_MODIFIED = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	public void doContent(IPortalComponentRequest request, IPortalComponentResponse response) {
		ResourceBundle rb = ResourceBundle.getBundle("search", request.getLocale());
		
		NavigationEventsHelperService navHelperService = (NavigationEventsHelperService) PortalRuntime.getRuntimeResources().getService(NavigationEventsHelperService.KEY);
		boolean isAnonymousUser = navHelperService.isAnonymousUser(request);
		
		IAliasHelper aliasHelper = (IAliasHelper) PortalRuntime.getRuntimeResources().getService(IAliasService.KEY);
		String portalPath = aliasHelper.getPath(request);
		
		Locale locale = request.getLocale();
		DateFormat dateTimeInstance = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, locale);
		
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
			dynamic = 
				"/generic_smartview" +
				"?paraconfig=OT-DS-Server_agriportal" +
				"&paraurl=http%3A%2F%2Fgut210142.nordzucker.lan%3A8080%2Fcps%2Frde%2Fxchg%2Fagriportal%2Fhs.xsl%2F";
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
//			com.sap.security.api.IUser nwUser = request.getUser(); 
//			IUser user = null;        
//			
//			if (nwUser != null) {
//				try {
//					user = WPUMFactory.getUserFactory().getEP5User(nwUser); 
//				} catch (UserManagementException e1) {
//				}
//				user = UMFactory.getUserFactory().getUser(nwUser.getUniqueID());
//			}

			IUserContext user = request.getUser();
			if (user != null) {
//				ResourceContext ctx = new ResourceContext(user);
				IResourceFactory rf = ResourceFactory.getInstance();
				IResourceContext ctx = rf.getServiceContextUME(user.getUniqueID());
							
				try {
					// search for pages
					response.write("<div id='searchInKM'>");
					response.write("<form class='ovrlControl searchForm' data-dynamic='" + dynamic + "'><input class='term' type='text' value='" + term + "'><button class='button' type='submit'>");
					response.write(str(rb, "button.search"));
					response.write("</button></form>");
					
					IIndexService indexService = (IIndexService) ResourceFactory.getInstance().getServiceFactory().getService(IServiceTypesConst.INDEX_SERVICE);
					IFederatedSearch federatedSearch = (IFederatedSearch) indexService.getObjectInstance(IWcmIndexConst.FEDERATED_SEARCH_INSTANCE);
					
					List activeIndexes = indexService.getActiveIndexes();
					
					SearchQueryListBuilder sqb = new SearchQueryListBuilder();
					sqb.setSearchTerm(term);
					
					// das wäre die Erweiterung an jede TREX Suche noch die Einschränkung nach dem Land.
					sqb.setSelectedSearchAction(SEARCH_PARAM_ACTION_LINGUISTIC);
					sqb.setSelectedCustomProps(KM_NAMESPACE +":contentlanguage(value=" + request.getLocale().getLanguage() + ")");
					
					IQueryEntryList qel = sqb.buildSearchQueryList();
					
					ISearchSession session = federatedSearch.searchWithSession(qel, activeIndexes, ctx);

					int totalNumberOfResults = session.getTotalNumberResultKeys();

					ISearchResultList results = session.getSearchResults(1 + (pg-1)*RESULTS_PER_PAGE, RESULTS_PER_PAGE + (pg-1)*RESULTS_PER_PAGE);

					response.write("<p title='");
					response.write((1 + (pg-1)*RESULTS_PER_PAGE) + " - " + (RESULTS_PER_PAGE + (pg-1)*RESULTS_PER_PAGE) + " / " + results.size());
					response.write("'>");
					response.write(str(rb, "text.results").replaceFirst("\\$1", "<strong class='term'>" + xssTerm + "</strong>").replaceFirst("\\$2", "<span>" + totalNumberOfResults + "</span>"));
					response.write("</p>");
					
					response.write("<header class='ovrlControl'>");
					if (totalNumberOfResults > RESULTS_PER_PAGE) {
						long P = (long) (Math.ceil(totalNumberOfResults / RESULTS_PER_PAGE)) + 1;
						response.write("<nav class='paginator' data-term='" + term + "'>");
						for (long p = 1; p <= P; p++) {
							if (p == pg) {
								response.write("<strong>" + p + "</strong>");
							} else {
								response.write("<a href='#' data-page='" + p + "'>" + p + "</a>");
							}
						}
						response.write("</nav>");

						response.write("<nav class='prevNext' data-term='" + term + "'>");
						if (pg > 1) {
							response.write("<a href='#' data-page='" + (pg - 1) + "' class='prev'>");
							response.write(str(rb, "button.prev_page"));
							response.write("</a>");
						} else {
							response.write("<span class='prev'>");
							response.write(str(rb, "button.prev_page"));
							response.write("</span>");;
						}
						if (pg < P) {
							response.write("<a href='#' data-page='" + (pg + 1) + "' class='next'>");
							response.write(str(rb, "button.next_page"));
							response.write("</a>");
						} else {
							response.write("<span class='next'>");
							response.write(str(rb, "button.next_page"));
							response.write("</span>");;
						}
						response.write("</nav>");
					}
					
					response.write("</header>");
					
					response.write("<ol start='" + pg + "' id='searchResultsList'>");
					
					try {
						int i=0;
						ISearchResultListIterator iter = results.listIterator();
						while (iter.hasNext()) {
							i += 1;
						    ISearchResult result = iter.next();
						    IResource resource = result.getResource();
						    IMime mime = MimeHandlerServiceFactory.getInstance().getExtensionSpecific(resource);
							String imgSrc = mime.getImagePath();
							IPropertyMap localProperties = result.getLocalProperties();
							
							boolean isPSMHTML = false; 
							String mimeType = mime.getType();
							if (mimeType != null) {
								if (mimeType.equals("text/html")) {
									imgSrc = request.getWebResourcePath() + "/mime-icons/html@2x.png";
									isPSMHTML = true;
								}
								if (mimeType.equals("application/pdf")) {
									imgSrc = request.getWebResourcePath() + "/mime-icons/pdf@2x.png";
								}
							}
							
							String displayName = resource.getDisplayName(true);
							if (isPSMHTML) {
								displayName = localProperties.get(PropertyName.getPN("http://sapportals.com/xmlns/cm", "displayname")).getValueAsString();
							}
							
							String rid = resource.getRID().getPath();
							String url = "/irj/go/km/docs" + rid;
							if (rid.startsWith("/opentext") && isPSMHTML) {
								String html = rid.substring(rid.lastIndexOf("/")+1, rid.length());
								url = portalPath.replaceFirst("/login$", "") + dynamic + html;
								if (url.indexOf("?") > -1) {
									url += "&pageTitle=" + StringUtils.escapeToURL(displayName);			
								} else {
									url += "?pageTitle=" + StringUtils.escapeToURL(displayName);
								}
							} else if (rid.startsWith("/pcd")){
								// e.g. /irj/go/km/docs/pcd/portal_content/nz_agri/cnt/de/grower/rl.grower/adm.wst.my_pages/campaign/pg.campaign_deliveredbeets
								url = portalPath.replaceFirst("/login$", "") + 
										"?NavigationTarget=pcd%3A" + rid.replaceFirst("^/pcd/", "") +
										"&NavigationContext=dummy";
							}

							response.write("<li id='hit_" + i + "' class='hit'>");
							response.write("<article>");

							response.write("<h2 class='title'><a href='");
							response.write(url);
							response.write("' class='link' target='_blank'>");
							response.write(displayName);
							response.write("</a></h2>");
							
							response.write("<p class='snippet'><a href='");
							response.write(url);
							response.write("' class='link' target='_blank'>");
							response.write(result.getContentSnippet());
							response.write("</a></p>");
							
							response.write("<footer>");
							response.write("<dl>");
							
							response.write("<dt class='info preview'>");
							response.write(str(rb, "text.preview"));
							response.write("</dt>");
							response.write("<dd class='info preview'><a href='");
							response.write(url);
							response.write("' class='link' target='_blank'><img src='");
							response.write(imgSrc);
							response.write("' title='");
							response.write(mimeType);
							response.write(", ");
							response.write(rid);
							response.write("'></a></dd>");

							response.write("<dt class='info relevance'>");
							response.write(str(rb, "text.relevance"));
							response.write("</dt>");
							float rankValue = result.getRankValue();
							int relevance = Math.round(rankValue * 100);
							response.write("<dd class='info relevance'><a href='");
							response.write(url);
							response.write("' class='link' target='_blank'><meter min='0' max='100' value='" + relevance + "' title='" + relevance + "%'></meter>");
							response.write(relevance + "%");
							response.write("</a></dd>");

//			    			response.write("<dt class='info category'>Kategorien</dt>");
//			    			response.write("<dd class='info category'><a href='first-category.html' class='link'>Erste Kategorie</a></dd>");
//			    			response.write("<dd class='info category'><a href='second-category.html' class='link'>Zweite Kategorie</a></dd>");

							IContent content = resource.getContent();
							if (content != null) {
								long fileSize = content.getContentLength();
								if (fileSize > 0) {
					    			response.write("<dt class='info fsize'>");
					    			response.write(str(rb, "text.filesize"));
					    			response.write("</dt>");
					    			response.write("<dd class='info fsize'>");
									if (fileSize > 1024 * 1024) {
										response.write(Math.round(fileSize / (1024 * 1024)) + " <abbr title='megabyte'>MB</abbr></dd>");	
									} else if (fileSize > 1024) {
										response.write(Math.round(fileSize / 1024) + " <abbr title='kilobyte'>kB</abbr></dd>");	
									} else {
										response.write(fileSize + " <abbr title='byte'>B</abbr></dd>");
									}
								}
							}

						
			    			Date lastModified = resource.getLastModified();
			    			if (lastModified == null) {
			    				try {
			    					lastModified = (Date) localProperties.get(PropertyName.getPN("http://sapportals.com/xmlns/cm", "modified")).getValue();
			    				} catch (Exception ex) {}
			    			}
			    			if (lastModified != null) {
				    			TimeZone timeZone = TimeZone.getTimeZone("GMT+1"); // CET
				    			if (locale.toString().startsWith("fi")) {
				    				timeZone = TimeZone.getTimeZone("GMT+2"); // Finland
				    			}
								Calendar cal = Calendar.getInstance(timeZone, locale);
				    			cal.setTime(lastModified);
				    			if (lastModified.getTime() > 0) {
					    			response.write("<dt class='info modified'>");
					    			response.write(str(rb, "text.modified"));
					    			response.write("</dt>");
					    			response.write("<dd class='info modified'><time datetime='"+ SDF.format(lastModified) + "' title='" + lastModified.getTime() + "'>");
									response.write(dateTimeInstance.format(lastModified));
					    			response.write("</time></dd>");
				    			}
			    			}
							
							response.write("\n<!-- resource.getProperties() = ");
							IPropertyMap properties = resource.getProperties();
					        for (IPropertyIterator it = properties.iterator(); it.hasNext(); ) {
					        	IProperty property = it.next();		
					        	Object value = property.getValue();
								IPropertyName propertyName = property.getPropertyName();
								response.write("\n{" + propertyName.getNamespace() + "}" + propertyName.getName() + ":   " + value);
					        }
					        response.write("\n-->");
					        
							response.write("\n<!-- result.getLocalProperties() = ");
					        for (IPropertyIterator it = localProperties.iterator(); it.hasNext(); ) {
					        	IProperty property = it.next();		
					        	Object value = property.getValue();
								IPropertyName propertyName = property.getPropertyName();
								response.write("\n{" + propertyName.getNamespace() + "}" + propertyName.getName() + ":   " + value);
					        }
					        response.write("\n-->");
							
							
							response.write("</dl>");
							response.write("</footer>");
							response.write("</article>");
							response.write("</li>");
						    
//					        IPropertyMap localProperties = result.getLocalProperties();
//					        for (IPropertyIterator it = localProperties.iterator(); it.hasNext(); ) {
//					        	IProperty property = it.next();		
//					        	Object value = property.getValue();
//								jsonProperties.put(property.getPropertyName().getName(), value instanceof Date ? ((Date) value).toString() : value);
//					        }
						}
					} catch (Exception e) {
						StringWriter sw = new StringWriter();
						PrintWriter pw = new PrintWriter(sw);
						e.printStackTrace(pw);
						response.write("<li><pre>");
						response.write(sw.toString());
						response.write("</pre></li>");
					}
					response.write("</ol>");
					response.write("<footer class='ovrlControl'>");
					if (totalNumberOfResults > RESULTS_PER_PAGE) {
						long P = (long) (Math.ceil(totalNumberOfResults / RESULTS_PER_PAGE)) + 1;
						response.write("<nav class='paginator' data-term='" + term + "'>");
						for (long p = 1; p <= P; p++) {
							if (p == pg) {
								response.write("<strong>" + p + "</strong>");
							} else {
								response.write("<a href='#' data-page='" + p + "'>" + p + "</a>");
							}
						}
						response.write("</nav>");
					}
					response.write("<button type='submit' id='closeSearch' class='closeOvrl'>");
					response.write(str(rb, "button.close"));
					response.write("</button>");
					response.write("</footer>");
					response.write("</div>");
					
					// search for documents
					if (!isAnonymousUser) {
						response.write("<div id='searchInPCM'>");
						response.write("<div id='searchInPCMFrame' data-src='");
						response.write("/irj/servlet/prt/portal/prtroot/pcd!3aportal_content!2fnz_agri!2fadmin!2fnavigation!2fadm.pcm_search?opentext.queryString=");
						response.write(term);
						response.write("'>");
						response.write("</div>");
						response.write("<footer class='ovrlControl'>");
						response.write("<button type='submit' id='closeSearch' class='closeOvrl'>");
						response.write(str(rb, "button.close"));
						response.write("</button>");
						response.write("</footer>");
						response.write("</div>");
					}
				} catch (ResourceException e) {
				} catch (WcmException e) {
				}
			} 
		} catch (Exception e) {
		}
	}
	
	private String str(ResourceBundle rb, String key) {
		String string = rb.getString(key);
		if (string != null) {
			return string;
		}
		return key;
	}
}