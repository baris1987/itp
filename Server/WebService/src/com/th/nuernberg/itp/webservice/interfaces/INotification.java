package com.th.nuernberg.itp.webservice.interfaces;

public interface INotification {
	public void setIdentifier(String identifier);
	public void setLongitude(double longitude);
	public void setLatitude(double latitude);
	public void setActivity(String activity);
	public void setLevel(int level);
	public String getIdentifier();
	public double getLongitude();
	public double getLatitude();
	public String getActivity();
	public int getLevel();
}
