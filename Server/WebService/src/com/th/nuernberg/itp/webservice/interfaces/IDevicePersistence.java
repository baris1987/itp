package com.th.nuernberg.itp.webservice.interfaces;

public interface IDevicePersistence extends IPersistence {
	public void setDevice(IDevice device);
	public IDevice getDevice();
}
