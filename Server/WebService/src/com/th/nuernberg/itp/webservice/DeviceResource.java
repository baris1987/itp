package com.th.nuernberg.itp.webservice;

import com.th.nuernberg.itp.webservice.interfaces.*;
import com.th.nuernberg.itp.webservice.types.Device;
import com.th.nuernberg.itp.webservice.types.DevicePersistence;

import javax.ws.rs.*;

@Path("device")
public class DeviceResource extends BaseResource implements IWebServiceDevice {

	private final IConfiguration config;
	private final IDatabase persister;
	
	public DeviceResource() {
		this.config = this.createConfigurationInstance(Constants.Configuration);
		this.persister = this.createDatabaseInstance(this.config);
	}

	@GET
	@Path("register/{identifier}/{latitude}/{longitude}")
	public String register(@PathParam("identifier") String identifier, @PathParam("latitude") double latitude, @PathParam("longitude") double longitude) {
		
		if (!identifier.matches("^([0-9A-F]{2}[:-]){5}([0-9A-F]{2})$")) {
			return "{success: false, message: \"Not a valid MAC address.\"}";
		}
		
		
		// Create Device instance
		IDevice device = new Device();
		device.setIdentifier(identifier);
		device.setLatitude(latitude);
		device.setLongitude(longitude);
		
		// Create Device Persister instance
		IDevicePersistence devicePersister = new DevicePersistence();
		devicePersister.setPersister(this.persister);
		devicePersister.setDevice(device);
		
		// Persist Device instance
		boolean stored = devicePersister.persist();
		
		return "{success: "+stored+", message: \"\"}";
	}

	@GET
	@Path("receive/{id}/{data}")
	public String receive(@PathParam("id") String id,
			@PathParam("data") byte data) {
		return "{receive: 0}";
	}

	@GET
	@Path("push/{id}/{enabled}")
	public String push(@PathParam("id") String id,
			@PathParam("enabled") Boolean enabled) {
		return "{push: 0}";
	}
}