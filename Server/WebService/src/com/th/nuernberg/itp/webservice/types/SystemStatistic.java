package com.th.nuernberg.itp.webservice.types;

import com.th.nuernberg.itp.webservice.interfaces.ISystemStatistic;

public class SystemStatistic implements ISystemStatistic {
	private int connectedDevices;
	
	public void setConnectedDevices(int connectedDevices) {
		this.connectedDevices = connectedDevices;
	}
	
	public int getConnectedDevices() {
		return this.connectedDevices;
	}
}
