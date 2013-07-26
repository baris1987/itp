package com.th.nuernberg.itp.webservice.interfaces;

public interface IDevice {
	public void setIdentifier(String identifier);
	public void setLongitude(double longitude);
	public void setLatitude(double latitude);
	public String getIdentifier();
	public double getLongitude();
	public double getLatitude();
}
