package com.th.nuernberg.itp.webservice;

import com.th.nuernberg.itp.webservice.interfaces.IAndroidDevice;
import com.th.nuernberg.itp.webservice.interfaces.IGoogleCloudMessagingNotification;

public class GoogleCloudMessagingNotification implements IGoogleCloudMessagingNotification {
	private IAndroidDevice[] androidDevices;
	private String message;
	
	public void setAndroidDevices(IAndroidDevice[] androidDevices) {
		this.androidDevices = androidDevices; 
	}
	
	public IAndroidDevice[] getAndroidDevices() {
		return this.androidDevices; 
	}
	
	public void setMessage(String message) {
		this.message = message;
	}
	
	public String getMessage() {
		return this.message;
	}
}
