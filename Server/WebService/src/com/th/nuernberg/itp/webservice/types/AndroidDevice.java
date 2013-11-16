package com.th.nuernberg.itp.webservice.types;

import com.th.nuernberg.itp.webservice.interfaces.IAndroidDevice;

public class AndroidDevice implements IAndroidDevice {
	private String deviceIdentifier;
	
	public void setDeviceIdentifier(String deviceIdentifier) {
		this.deviceIdentifier = deviceIdentifier;
	}
	
	public String getDeviceIdentifier() {
		return this.deviceIdentifier;
	}
}
