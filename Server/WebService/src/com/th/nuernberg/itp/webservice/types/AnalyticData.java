package com.th.nuernberg.itp.webservice.types;

import com.th.nuernberg.itp.webservice.interfaces.IAnalyticData;

public class AnalyticData implements IAnalyticData {
	private int devices;
	
	public void setDevices(int devices) {
		this.devices = devices;
	}
	
	public double getDevices() {
		return this.devices;
	}
}
