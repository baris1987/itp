package com.th.nuernberg.itp.webservice.interfaces;

public interface IWebServiceDevice {
	// Register device (also usable as heart beat)
	String register(String identifier, double latitude, double longitude);
	
	// Device sends an alarm
	String alarm(String identifier, double latitude, double longitude, int level);
	
	// List all connected devices
	String list();
	
	// Get device meta information
	String meta(String identifier);
	
	// Detect earth quake and send broadcast
	String broadcast(String message, double latitude, double longitude);
	
	// Statistics 
	String stats();
}