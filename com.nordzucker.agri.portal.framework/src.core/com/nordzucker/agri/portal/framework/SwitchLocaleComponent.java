package com.nordzucker.agri.portal.framework;
 
import java.util.Locale;

import javax.servlet.http.HttpSession;

import net.sf.json.JSON;
import net.sf.json.JSONObject;

import com.nordzucker.agri.portal.framework.utils.JSONComponent;
import com.sap.security.api.IUserMaint;
import com.sap.security.api.UMException;
import com.sap.security.api.UMFactory;
import com.sapportals.portal.navigation.NavigationEventsHelperService;
import com.sapportals.portal.prt.component.IPortalComponentRequest;
import com.sapportals.portal.prt.runtime.PortalRuntime;
import com.sapportals.portal.prt.session.IUserContext;

public class SwitchLocaleComponent extends JSONComponent {
	private static final String COMPONENT_PATH = "/irj/go/portal/prtmode/json/prtroot/nz.switch_locale";
	
    public JSON doJSON(IPortalComponentRequest request) {
		JSONObject json = new JSONObject();
		
    	json.put("success", switchLocale(request));
    	
    	return json;
    }
    
	private boolean switchLocale(IPortalComponentRequest request) {		
		Locale newLocale = null;		
		
		// read new locale from parameter "locale"
		String locale = request.getParameter("locale");
		if (locale == null) {
			String requestUri = request.getServletRequest().getRequestURI();
			if (requestUri.startsWith(COMPONENT_PATH) && requestUri.length() > COMPONENT_PATH.length()) {
				locale = requestUri.substring(COMPONENT_PATH.length() + 1);
			}
		}
		
		if (locale != null && !locale.equals("")) {			
			String language, country;
			int underscore = locale.indexOf("_");
			if(underscore > -1) {
				language = locale.substring(0, underscore);
				country = locale.substring(underscore+1);
			} else {
				language = locale;
				country = "";
			}
			newLocale = new Locale(language, country);

			NavigationEventsHelperService helperService = (NavigationEventsHelperService) PortalRuntime.getRuntimeResources().getService(NavigationEventsHelperService.KEY);
			if (helperService.isAnonymousUser(request)) {
				// guest user --> set only in session
				
				HttpSession session = request.getServletRequest().getSession();
				session.setAttribute("sessionLocale", newLocale); 
				return true;
			} else {
				// authenticated user --> set in user profile
				
				IUserContext userContext = request.getUser();
				String userID = userContext.getUniqueID();
				if (userID != null) {// && !newLocale.equals(request.getLocale())
					// currently set locale and the given locale differ, set new locale 
					IUserMaint mutUser = null;
					try {
						mutUser = UMFactory.getUserFactory().getMutableUser(userID);
						mutUser.setLocale(newLocale);
						mutUser.save();
						mutUser.commit();
						return true;
					} catch (UMException e1) {
						mutUser.rollback();
					} catch (Exception e2) {
						mutUser.rollback();
					}
				}
			}
		}
		return false;
	}
}