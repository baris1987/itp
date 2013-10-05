package com.th.nuernberg.itp.webservice.types;

import com.th.nuernberg.itp.webservice.interfaces.INotification;

public class Notification implements INotification {
	private String identifier;
	private double longitude;
	private double latitude;
	private String activity;
	private int level;
	
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	
	public void setLevel(int level) {
		this.level = level;
	}
	
	public int getLevel() {
		return this.level;
	}
	
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	
	public String getIdentifier() {
		return this.identifier;
	}
	
	public double getLongitude() {
		return this.longitude;
	}
	
	public double getLatitude() {
		return this.latitude;
	}

	public void setActivity(String activity) {
		this.activity = activity;
	}

	public String getActivity() {
		return this.activity;
	}
}
