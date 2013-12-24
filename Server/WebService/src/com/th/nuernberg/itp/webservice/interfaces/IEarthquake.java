package com.th.nuernberg.itp.webservice.interfaces;

public interface IEarthquake {

	void setLatitude(double latitude);

	double getLatitude();

	void setLongitude(double longitude);

	double getLongitude();

	void setActivity(String activity);

	String getActivity();

	void setRadius(double radius);

	double getRadius();

	void setDevices(int devices);

	double getDevices();

	void setRatio(double ratio);

	double getRatio();

}