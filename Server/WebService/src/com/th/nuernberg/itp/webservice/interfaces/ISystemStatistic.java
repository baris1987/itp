package com.th.nuernberg.itp.webservice.interfaces;

import java.util.List;

public interface ISystemStatistic {
	void setConnectedDevices(int connectedDevices);
	int getConnectedDevices();
	void setEarthquakes(List<IEarthquake> earthquakes);
	List<IEarthquake> getEarthquakes();
	public void setVersion(String version);
	public String getVersion();
}
