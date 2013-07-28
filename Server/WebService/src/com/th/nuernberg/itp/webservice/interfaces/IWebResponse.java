package com.th.nuernberg.itp.webservice.interfaces;

public interface IWebResponse {
	public String getMessage();
	public Object getData();
	public boolean getSuccess();
	
	public void setMessage(String message);
	public void setData(Object data);
	public void setSuccess(boolean success);
}
