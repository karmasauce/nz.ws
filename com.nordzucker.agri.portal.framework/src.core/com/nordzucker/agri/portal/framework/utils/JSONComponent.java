package com.nordzucker.agri.portal.framework.utils;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSON;
import net.sf.json.JSONObject;

import com.sapportals.portal.prt.component.AbstractPortalComponent;
import com.sapportals.portal.prt.component.IPortalComponentRequest;
import com.sapportals.portal.prt.component.IPortalComponentResponse;
import com.sapportals.portal.prt.util.StringUtils;

public abstract class JSONComponent extends AbstractPortalComponent {

	public void doJson(IPortalComponentRequest request, IPortalComponentResponse response) {
		String json = doJSON(request).toString();
		PrintWriter writer;
		try {
			HttpServletResponse servletResponse = request.getServletResponse(true);
			servletResponse.addHeader("Content-Type", "text/x-json");
			writer = servletResponse.getWriter();
			writer.write(json);
		} catch (IOException e) {
		}
	}

	public void doContent(IPortalComponentRequest request, IPortalComponentResponse response) {
		String json = doJSON(request).toString(4);
		response.write("<pre>");
		response.write(StringUtils.escapeToHTML(json));
		response.write("</pre>");
	}

	public abstract JSON doJSON(IPortalComponentRequest request);
	
	public JSON exceptionAsJSON(Throwable t) {
		return JSONUtils.exceptionAsJSON(t);
	}

	public JSON errorAsJSON(Object value) {
		return JSONUtils.errorAsJSON(value);
	}
}