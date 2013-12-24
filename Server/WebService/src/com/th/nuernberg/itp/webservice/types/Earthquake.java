package com.th.nuernberg.itp.webservice.types;

import com.th.nuernberg.itp.webservice.interfaces.IEarthquake;

public class Earthquake implements IEarthquake {
	private double longitude;
	private double latitude;
	private String activity;
	private double radius;
	private int devices;
	private double ratio;
	

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLatitude() {
		return this.latitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public double getLongitude() {
		return this.longitude;
	}

	public void setActivity(String activity) {
		this.activity = activity;
	}
	
	public String getActivity() {
		return this.activity;
	}

	public void setRadius(double radius) {
		this.radius = radius;
	}

	public double getRadius() {
		return this.radius;
	}

	public void setDevices(int devices) {
		this.devices = devices;
	}

	public double getDevices() {
		return this.devices;
	}

	public void setRatio(double ratio) {
		this.ratio = ratio;
	}

	public double getRatio() {
		return this.ratio;
	}
}
