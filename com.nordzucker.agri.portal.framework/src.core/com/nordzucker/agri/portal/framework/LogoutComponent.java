package com.nordzucker.agri.portal.framework;

import com.sap.portal.navigation.IAliasHelper;
import com.sap.portal.navigation.IAliasService;
import com.sapportals.portal.prt.component.AbstractPortalComponent;
import com.sapportals.portal.prt.component.IPortalComponentRequest;
import com.sapportals.portal.prt.component.IPortalComponentResponse;
import com.sapportals.portal.prt.runtime.PortalRuntime;

//public class LogoutComponent extends JSONComponent {
//
//	public JSON doJSON(IPortalComponentRequest request) {
//		JSONObject json = new JSONObject();
//		json.put("success", true);
//		return json;
//	}
//
//}

public class LogoutComponent extends AbstractPortalComponent {

	protected void doContent(IPortalComponentRequest request, IPortalComponentResponse response) {
		String portalPath = request.getParameter("url");
		
		if (portalPath == null) {
			IAliasHelper aliasHelper = (IAliasHelper) PortalRuntime.getRuntimeResources().getService(IAliasService.KEY);
			portalPath = aliasHelper.getPath(request);
		}
		
		response.write("<script>top.location.href='" + portalPath + "'</script>");
	}

}