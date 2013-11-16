package com.th.nuernberg.itp.webservice.interfaces;

public interface IGoogleCloudMessaging {
	void setMessagingConfiguration(IGoogleCloudMessagingConfiguration configuration);	
	IGoogleCloudMessagingConfiguration getMessagingConfiguration();
	void send(IGoogleCloudMessagingNotification notification);
}
