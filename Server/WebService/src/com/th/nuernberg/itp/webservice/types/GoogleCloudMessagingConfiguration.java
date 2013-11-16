package com.th.nuernberg.itp.webservice.types;

import com.th.nuernberg.itp.webservice.interfaces.IGoogleCloudMessagingConfiguration;

public class GoogleCloudMessagingConfiguration implements IGoogleCloudMessagingConfiguration {
	private String apiUrl;
	private String authorizationKey;
	
	public void setApiUrl(String apiUrl) {
		this.apiUrl = apiUrl;
	}
	
	public String getApiUrl() {
		return this.apiUrl;
	}
	
	public void setAuthorizationKey(String authorizationKey) {
		this.authorizationKey = authorizationKey;
	}
	
	public String getAuthorizationKey() {
		return this.authorizationKey;
	}
}
