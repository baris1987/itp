package com.th.nuernberg.itp.webservice;

import java.sql.ResultSet;
import java.sql.SQLException;

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
		
		// Validate MAC address 
		if (!identifier.matches("^([0-9a-f]{12})$")) {
			return "{success: false, message: \"Not a valid MAC-48 address. Only lower case and hex is allowed.\"}";
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
		
		try {
			this.persister.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return "{success: "+stored+", message: \"\"}";
	}

	@Override
	public String alarm() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String detect() {
		// TODO Auto-generated method stub
		return null;
	}

	@GET
	@Path("list")
	public String list() {
		
		
		/* ONLY FOR TESTING - REFACTORING NEEDED */
		try {
			ResultSet rs = this.persister.get("SELECT IDENTIFIER, ACTIVITY, LATITUDE, LONGITUDE FROM ITP.T_DEVICE");
			
			rs.next();
			String s = rs.getString(1) + " | " + rs.getString(2) + " | " + rs.getString(3) + " | " + rs.getString(4);
			
			try {
				this.persister.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return s;
		
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}

	@Override
	public String warn() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String meta() {
		// TODO Auto-generated method stub
		return null;
	}
}