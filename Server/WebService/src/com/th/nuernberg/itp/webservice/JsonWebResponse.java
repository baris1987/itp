package com.th.nuernberg.itp.webservice;

import com.th.nuernberg.itp.webservice.interfaces.IWebResponse;

public final class JsonWebResponse {
	public static String build(boolean success, Object data, String message) {
		Json json = new Json();
		
		IWebResponse response = new WebResponse();
		response.setData(data);
		response.setMessage(message);
		response.setSuccess(success);
		
		return json.build(response);
	}
	
	public static String build(boolean success, Object data) {
		return JsonWebResponse.build(success, data, "");
	}
		
	public static String build(boolean success, String message) {
		return JsonWebResponse.build(success, new Object(), message);
	}
	
	public static String build(boolean success) {
		return JsonWebResponse.build(success, new Object(), "");
	}
}
