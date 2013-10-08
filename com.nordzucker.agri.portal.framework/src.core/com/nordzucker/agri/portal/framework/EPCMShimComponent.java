package com.nordzucker.agri.portal.framework;

import com.sapportals.portal.prt.component.AbstractPortalComponent;
import com.sapportals.portal.prt.component.IPortalComponentRequest;
import com.sapportals.portal.prt.component.IPortalComponentResponse;

public class EPCMShimComponent extends AbstractPortalComponent {

	public void doContent(IPortalComponentRequest request, IPortalComponentResponse response) {
		// no direct output
		// the needed output of the EPCMShimComponent script object happens automatically due to the property EPCFLevel = 1
	}
}