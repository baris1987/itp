package com.th.nuernberg.itp.webservice.interfaces;

public interface IWebServiceDevice {
	String register(String identifier, double latitude, double longitude);
	String receive(String id, byte data);
	String push(String id, Boolean enabled);
}