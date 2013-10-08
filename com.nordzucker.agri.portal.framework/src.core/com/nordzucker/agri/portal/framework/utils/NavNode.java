/*
 * Created on 25.07.2008 by D037963
 */
package com.nordzucker.agri.portal.framework.utils;

import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.naming.NamingException;
import javax.naming.directory.NoSuchAttributeException;

import com.sapportals.portal.navigation.INavigationConstants;
import com.sapportals.portal.navigation.INavigationNode;
import com.sapportals.portal.navigation.NavigationEventsHelperService;
import com.sapportals.portal.navigation.NavigationNodes;
import com.sapportals.portal.prt.component.IPortalComponentContext;
import com.sapportals.portal.prt.component.IPortalComponentProfile;
import com.sapportals.portal.prt.component.IPortalComponentRequest;
import com.sapportals.portal.prt.runtime.PortalRuntime;
import com.sapportals.portal.prt.util.StringUtils;

/**
 * @author D037963
 */
public class NavNode {
//
//	public static String anchorAttributes(INavigationNode node, String portalPath, boolean initialNodeFirstLevel) {
//		return anchorAttributes(node, portalPath, null, initialNodeFirstLevel);
//	}
//
//	public static String anchorAttributes(INavigationNode node, String portalPath) {
//		return anchorAttributes(node, portalPath, null, false);
//	}
//
//	public static String anchorAttributes(INavigationNode node, String portalPath, String navigationContext, boolean initialNodeFirstLevel) {
//		return anchorAttributes(node, portalPath, navigationContext, initialNodeFirstLevel, null);
//	}
//
//	public static String anchorAttributes(INavigationNode node, String portalPath, String navigationContext, boolean initialNodeFirstLevel, String loadFolderOrDrillDown) {
//		StringBuffer buf = new StringBuffer(128);
//		int showType = node.getShowType();
//		if (showType != INavigationConstants.SHOW_INPLACE) {
//			String winName = node.getWindowName();
//			if (winName == null || winName.length() == 0)
//				winName = "_blank";
//
//			String winFeatures = (String) getAttributeValue(node, "com.sapportals.portal.navigation.WinFeatures");
//			if (winFeatures.equals("")) {
//				winFeatures = "height=" + node.getExtWindowHeight() + ",width=" + node.getExtWindowWidth() + ",toolbar,location,status,scrollbars,directories,menubar,resizable";
//			} else {
//				if (winFeatures.indexOf("height") == -1) {
//					winFeatures = "height=" + node.getExtWindowHeight() + "," + winFeatures;
//				}
//				if (winFeatures.indexOf("width") == -1) {
//					winFeatures = "width=" + node.getExtWindowWidth() + "," + winFeatures;
//				}
//			}
//			switch (showType) {
//			case INavigationConstants.SHOW_EXTERNAL:
//				// IPortalComponentURI contentAreaURI =
//				// request.createPortalComponentURI();
//				// contentAreaURI.setContextName("com.sap.portal.navigation.contentarea.default");
//				buf.append("href='");
//				// buf.append(contentAreaURI.toString());
//				// buf.append("/irj/servlet/prt/portal/prtroot/fwk_light.content");
//				buf.append("/irj/servlet/prt/portal/prtroot/com.sap.portal.navigation.contentarea.default");
//				buf.append("?NavigationTarget=");
//				buf.append(node.getHashedName());
//				if (navigationContext != null) {
//					buf.append("&NavigationContext=");
//					buf.append(navigationContext);
//				}
//				if (initialNodeFirstLevel) {
//					buf.append("&InitialNodeFirstLevel=true");
//				}
//				buf.append("&ExecuteLocally=true&NavPathUpdate=false' target='");
//				buf.append(winName);
//				buf.append("' onclick='self.open(this.href,this.target,\"");
//				buf.append(winFeatures);
//				buf.append("\");return false'");
//				break;
//			case INavigationConstants.SHOW_EXTERNAL_PORTAL:
//				buf.append("href='");
//				buf.append(portalPath);
//				buf.append("?NavigationTarget=");
//				buf.append(node.getHashedName());
//				if (navigationContext != null) {
//					buf.append("&NavigationContext=");
//					buf.append(navigationContext);
//				}
//				if (initialNodeFirstLevel) {
//					buf.append("&InitialNodeFirstLevel=true");
//				}
//				buf.append("&ExecuteLocally=true&NavPathUpdate=false' target='");
//				buf.append(winName);
//				buf.append("' onclick='self.open(this.href,this.target,\"");
//				buf.append(winFeatures);
//				buf.append("\");return false'");
//				break;
//			case INavigationConstants.SHOW_EXTERNAL_HEADERLESS:
//				buf.append("href='");
//				buf.append(portalPath);
//				buf.append("?NavigationTarget=");
//				buf.append(node.getHashedName());
//				if (navigationContext != null) {
//					buf.append("&NavigationContext=");
//					buf.append(navigationContext);
//				}
//				if (initialNodeFirstLevel) {
//					buf.append("&InitialNodeFirstLevel=true");
//				}
//				buf.append("&NavMode=" + INavigationConstants.SHOW_EXTERNAL_HEADERLESS);
//				buf.append("&ExecuteLocally=true&NavPathUpdate=false' target='");
//				buf.append(winName);
//				buf.append("' onclick='self.open(this.href,this.target,\"");
//				buf.append(winFeatures);
//				buf.append("\");return false'");
//				break;
//			case INavigationConstants.SHOW_EMBEDDED_EXTERNAL_HEADERLESS:
//				buf.append("href='");
//				buf.append(portalPath);
//				buf.append("?NavigationTarget=");
//				buf.append(node.getHashedName());
//				if (navigationContext != null) {
//					buf.append("&NavigationContext=");
//					buf.append(navigationContext);
//				}
//				if (initialNodeFirstLevel) {
//					buf.append("&InitialNodeFirstLevel=true");
//				}
//				buf.append("&ExecuteLocally=true&NavPathUpdate=false&NavMode=" + INavigationConstants.SHOW_EMBEDDED_EXTERNAL_HEADERLESS);
//				buf.append("' target='");
//				buf.append(winName);
//				buf.append("' onclick='self.open(this.href,this.target,\"");
//				buf.append(winFeatures);
//				buf.append("\");return false'");
//				break;
//			default:
//				buf.append("href='");
//				buf.append(portalPath);
//				buf.append("?NavigationTarget=");
//				buf.append(node.getHashedName());
//				if (navigationContext != null) {
//					buf.append("&NavigationContext=");
//					buf.append(navigationContext);
//				}
//				if (initialNodeFirstLevel) {
//					buf.append("&InitialNodeFirstLevel=true");
//				}
//				buf.append("&ExecuteLocally=true&NavPathUpdate=false' target='");
//				buf.append(winName);
//				buf.append("' onclick='self.open(this.href,this.target,\"");
//				buf.append(winFeatures);
//				buf.append("\");return false'");
//				break;
//			}
//		} else {
//			if (node.isLaunchable()) {
//				buf.append("rel='nav' href='");
//			} else {
//				if (loadFolderOrDrillDown != null) {
//					buf.append("rel='").append(loadFolderOrDrillDown).append("' href='");
//				} else {
//					buf.append("rel='nav' href='");
//				}
//			}
//			buf.append(portalPath);
//			buf.append("?NavigationTarget=");
//			buf.append(node.getHashedName());
//			if (navigationContext != null) {
//				buf.append("&NavigationContext=");
//				buf.append(navigationContext);
//			}
//			if (initialNodeFirstLevel) {
//				buf.append("&InitialNodeFirstLevel=true");
//			}
//			buf.append("&ExecuteLocally=true&NavPathUpdate=false'");
//		}
//		// if (node.getVisualizationType() == INavigationConstants.TYPE_FOLDER)
//		// {
//		// buf.append(" class='folder'");
//		// }
//		return buf.toString();
//	}

	public static String getAttributeValue(IPortalComponentRequest request, String pcdUri, String key) {
		String attr = null;
		if (pcdUri != null && pcdUri.startsWith("pcd:")) {
			IPortalComponentContext componentContext = request.getComponentContext(pcdUri);
			if (componentContext != null) {
				IPortalComponentProfile profile = componentContext.getProfile();
				if (profile != null) {
					attr = profile.getProperty(key);	
				}	
			}
		}
		return attr;
	}
	
	public static Object getAttributeValue(INavigationNode node, String key) {
		Object attr = null;
		try {
			attr = node.getAttributeValue(key);
		} catch (NoSuchAttributeException e) {
			// attr = new String("NO SUCH ATTRIBUTE: " + key);
		}
		return attr;
	}

	public static Object getAttributeValue(INavigationNode node, String key, Locale locale) {
		Object attr = null;
		try {
			attr = node.getAttributeValue(key, locale);
		} catch (NoSuchAttributeException e) {
			// attr = new String("NO SUCH ATTRIBUTE: " + key);
		}
		return attr;
	}
	
	public static String getQuickLink(INavigationNode node) {
		String quickLink = (String) getAttributeValue(node, INavigationConstants.NAVIGATION_QUICKLINK);
		if (quickLink != null && quickLink.trim().equals("")) {
			return null;
		}
		return quickLink;
	}
	
	public static String getTitle(INavigationNode node, Locale locale) {
		String title = node.getTitle(locale);
		if (title == null) {
			return "";
		}
		return StringUtils.escapeToHTML(title);
	}
	
	public static String getDescription(INavigationNode node, Locale locale) {
		String description = node.getDescription(locale);
		if (description == null) {
			return "";
		}
		return StringUtils.escapeToHTML(description);
	}
	
	public static NavigationNodes getChildren(INavigationNode node, IPortalComponentRequest request, final Locale locale) {
		NavigationEventsHelperService navHelperService = (NavigationEventsHelperService) PortalRuntime.getRuntimeResources().getService(NavigationEventsHelperService.KEY);
		Hashtable environment = navHelperService.getEnvironment(request);
		return getChildren(node, environment, locale);
	}
	
	public static NavigationNodes getChildren(INavigationNode node, Hashtable environment, final Locale locale) {
		NavigationNodes nodes = null;
		try {
			nodes = node.getChildren(environment);
			Object sortAlphaAttr = getAttributeValue(node, "comBoschNavigationSortAlphabetically");
			if (nodes != null && nodes.size() > 0 && sortAlphaAttr != null && new Boolean(sortAlphaAttr.toString()).booleanValue()) {
				Collections.sort(nodes, new Comparator() {
					public int compare(Object o1, Object o2) {
						if (o1 != null && o2 != null) {
							String t1 = ((INavigationNode) o1).getTitle(locale);
							String t2 = ((INavigationNode) o2).getTitle(locale);
							return t1.compareTo(t2);
						}
						return 0;
					}
				});
			}
		} catch (NamingException e) {
		}
		return nodes;
	}

	public static NavigationNodes getChildren(INavigationNode node, Hashtable environment) {
		NavigationNodes nodes = null;
		try {
			nodes = node.getChildren(environment);
		} catch (NamingException e) {
		}
		return nodes;
	}

	public static boolean hasChildren(INavigationNode node, IPortalComponentRequest request) {
		NavigationEventsHelperService navHelperService = (NavigationEventsHelperService) PortalRuntime.getRuntimeResources().getService(NavigationEventsHelperService.KEY);
		Hashtable environment = navHelperService.getEnvironment(request);
		return hasChildren(node, environment);
	}

	public static boolean hasChildren(INavigationNode node, Hashtable environment) {
		NavigationNodes nodes = getChildren(node, environment);
		return nodes != null && !nodes.isEmpty();
	}

	public static boolean isInPath(INavigationNode node, IPortalComponentRequest request) {
		return isInPath(node, getPathNodes(request));		
	}
	
	public static boolean isInPath(INavigationNode node, NavigationNodes pathNodes) {
		boolean isInPath = false;
		if (node != null && pathNodes != null && !pathNodes.isEmpty()) {
			String nodeName = node.getHashedName();
			for (Iterator it = pathNodes.iterator(); it.hasNext();) {
				INavigationNode pathNode = (INavigationNode) it.next();
				String pathNodeName = pathNode.getHashedName();
				if (pathNodeName.equals(nodeName)) {
					return true;
				}
			}
		}
		return isInPath;
	}

	public static boolean isInvisible(INavigationNode node) {
		Object invObj = getAttributeValue(node, "com.sap.portal.navigation.Invisible");
		return invObj != null && Boolean.valueOf(invObj.toString()).booleanValue();
	}

	public static boolean isLaunchableFolder(INavigationNode node, Hashtable environment) {
		return node.getVisualizationType() == INavigationConstants.TYPE_FOLDER && node.isLaunchable(environment);
	}

	public static boolean equals(INavigationNode node1, INavigationNode node2) {
		return node1 != null && node2 != null && node1.getHashedName().equals(node2.getHashedName());
	}

	public static NavigationNodes getRelatedNodes(INavigationNode node, Hashtable environment) {
		if (node != null && (node.getVisualizationType() == INavigationConstants.TYPE_IVIEW || node.getVisualizationType() == INavigationConstants.TYPE_PAGE)) {
			try {
				return node.getRelatedSeeAlsoNodes(environment);
			} catch (NamingException e) {
			}
		}
		return null;
	}

	public static NavigationNodes getDynNavNodes(INavigationNode node, Hashtable environment) {
		if (node != null && (node.getVisualizationType() == INavigationConstants.TYPE_IVIEW || node.getVisualizationType() == INavigationConstants.TYPE_PAGE)) {
			try {
				return node.getRelatedNavigationEntitiesNodes(environment);
			} catch (NamingException ne) {
			}
		}
		return null;
	}

	public static NavigationNodes getPathNodes(IPortalComponentRequest request) {
		NavigationEventsHelperService helperService = (NavigationEventsHelperService) PortalRuntime.getRuntimeResources().getService(NavigationEventsHelperService.KEY);

		List list = helperService.getNavNodesListForPath(request, INavigationConstants.NAVIGATION_CONTEXT_ATTR);
		NavigationNodes nodes = new NavigationNodes();
		for (Iterator it = list.iterator(); it.hasNext();) {
			nodes.add(it.next());
		}

		return nodes;
	}

	public static INavigationNode getCollRoomRootNode(IPortalComponentRequest request, INavigationNode launchedNode) {
		NavigationEventsHelperService helperService = (NavigationEventsHelperService) PortalRuntime.getRuntimeResources().getService(NavigationEventsHelperService.KEY);
		INavigationNode node = launchedNode;
		node = helperService.getParentNode(node, request);

		while (node != null && hasMoreFolders(node.getName())) {
			node = helperService.getParentNode(node, request);
		}
		return node;
	}
	
	public static String getId(INavigationNode node) {
		return node.getHashedName().replaceFirst("^navurl://", "");
	}

	private static boolean hasMoreFolders(String nodeName) {
		char[] c = nodeName.toCharArray();
		int counter = 0;
		for (int i = 0; i < c.length; i++) {
			if (c[i] == '/')
				counter++;
		}
		return counter > 6;
	}
}
