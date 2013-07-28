package com.th.nuernberg.itp.webservice;

import com.th.nuernberg.itp.webservice.interfaces.IWebResponse;

public class WebResponse implements IWebResponse {

	private String message;
	private Object data;
	private boolean success;
	
	public String getMessage() {
		return this.message;
	}

	public Object getData() {
		return this.data;
	}

	public boolean getSuccess() {
		return this.success;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

}
