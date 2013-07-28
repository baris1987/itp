package com.th.nuernberg.itp.webservice.interfaces;

public interface IWebServiceDevice {
	// Register device (also usable as heart beat)
	String register(String identifier, double latitude, double longitude);
	
	// Device sends an alarm
	String alarm();
	
	// Detect all devices
	String detect();
	
	// List all connected devices
	String list();
	
	// Notify all devices
	String warn();
	
	// Get device meta inforamtion
	String meta();
}