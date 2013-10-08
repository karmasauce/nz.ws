package com.nordzucker.agri.portal.framework;

import com.sapconsulting.portal.utils.html.EnhancedPortalResponse;
import com.sapportals.portal.prt.component.AbstractPortalComponent;
import com.sapportals.portal.prt.component.IPortalComponentContext;
import com.sapportals.portal.prt.component.IPortalComponentRequest;
import com.sapportals.portal.prt.component.IPortalComponentResponse;
import com.sapportals.portal.prt.pom.IComponentNode;
import com.sapportals.portal.prt.pom.IEvent;
import com.sapportals.portal.prt.pom.INode;
import com.sapportals.portal.prt.pom.INodeList;
import com.sapportals.portal.prt.pom.IPortalNode;
import com.sapportals.portal.prt.pom.NodeMode;

public class LoginHtml5Component extends AbstractPortalComponent {

	@Override
	protected void doOnNodeReady(IPortalComponentRequest request, IEvent event) {
		addChildNode(request, request.getComponentContext().getApplicationName() + ".login");
	}
	
	public void doContent(IPortalComponentRequest request, IPortalComponentResponse response) {
		EnhancedPortalResponse epResponse = new EnhancedPortalResponse(request, true, false);
		epResponse.setDocTypeToHtml5();
		
		includeChildNodes(request, response);
	}
	
	private void addChildNode(IPortalComponentRequest request, String launchURL) {
		INode node = request.getNode();
		if (node != null) {
			IPortalNode portalNode = node.getPortalNode();

			IPortalComponentContext componentContext = request.getComponentContext(launchURL);
			if (componentContext != null) {
				IComponentNode contentAreaNode = portalNode.createComponentNode("login", componentContext);
				node.addChildNode(contentAreaNode);
			}
		}
	}

	private void includeChildNodes(IPortalComponentRequest request, IPortalComponentResponse response) {
		INode node = request.getNode();
		INodeList contentAreaNodes = node.getChildNodesByName("login");
		if (contentAreaNodes != null) {
			for (int i = 0; i < contentAreaNodes.getLength(); i++) {
				response.include(request, contentAreaNodes.item(i));
			}
		}
	}
}