package com.th.nuernberg.itp.webservice.interfaces;

public interface IDevice extends IAndroidDevice {
	public void setLongitude(double longitude);
	public void setLatitude(double latitude);
	public void setActivity(String activity);
	public double getLongitude();
	public double getLatitude();
	public String getActivity();
}
