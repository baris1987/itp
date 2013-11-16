package com.th.nuernberg.itp.webservice.interfaces;

public interface IGoogleCloudMessagingNotification {
	void setAndroidDevices(IAndroidDevice[] androidDevices);
	IAndroidDevice[] getAndroidDevices();
	void setMessage(String message);
	String getMessage();
}
