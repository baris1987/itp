package com.th.nuernberg.itp.webservice.types;
import com.th.nuernberg.itp.webservice.interfaces.IDevice;

public class Device implements IDevice {

	private String identifier;
	private double longitude;
	private double latitude;
	
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	
	public void setLongitude(double longitude) {
		this.longitude = longitude;
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
}
