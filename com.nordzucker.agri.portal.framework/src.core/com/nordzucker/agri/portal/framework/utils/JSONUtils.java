package com.nordzucker.agri.portal.framework.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.ResourceBundle;

import net.sf.json.JSON;
import net.sf.json.JSONObject;

public class JSONUtils {
	public static JSONObject convertResourceBundle(ResourceBundle rb) {
		JSONObject json = new JSONObject();
		if (rb != null) {
			Enumeration<String> keys = rb.getKeys();
			while (keys.hasMoreElements()) {
				String key = keys.nextElement();
				json.put(key, rb.getString(key));
			}
		}
		return json;
	}

	public static JSON exceptionAsJSON(Throwable t) {
		JSONObject error = new JSONObject();
		error.put("Exception", t.getMessage());
		error.put("Type", t.getClass().getName());
		error.put("Stacktrace", t.getStackTrace());
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		t.printStackTrace(pw);
		error.put("StacktraceString",sw.toString());
		return error;
	}

	public static JSON errorAsJSON(Object value) {
		JSONObject error = new JSONObject();
		error.put("Error", value.toString());
		error.put("Value", value);
		return error;
	}
}
