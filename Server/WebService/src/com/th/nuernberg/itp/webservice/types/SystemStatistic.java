package com.th.nuernberg.itp.webservice.types;

import java.util.List;

import com.th.nuernberg.itp.webservice.interfaces.IEarthquake;
import com.th.nuernberg.itp.webservice.interfaces.ISystemStatistic;

public class SystemStatistic implements ISystemStatistic {
	private int connectedDevices;
	private List<IEarthquake> earthquakes;
	private String version;
	
	public void setConnectedDevices(int connectedDevices) {
		this.connectedDevices = connectedDevices;
	}
	
	public int getConnectedDevices() {
		return this.connectedDevices;
	}
	
	public void setEarthquakes(List<IEarthquake> earthquakes) {
		this.earthquakes = earthquakes;
	}
	
	public List<IEarthquake> getEarthquakes() {
		return this.earthquakes;
	}
	
	public void setVersion(String version) {
		this.version = version;
	}
	
	public String getVersion() {
		return this.version;
	}
}
