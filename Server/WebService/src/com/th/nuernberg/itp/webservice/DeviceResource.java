package com.th.nuernberg.itp.webservice;

import java.util.List;
import com.th.nuernberg.itp.webservice.interfaces.*;
import com.th.nuernberg.itp.webservice.types.Device;
import com.th.nuernberg.itp.webservice.types.DeviceRepository;

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
			return JsonWebResponse.build(false, "Not a valid MAC-48 address. Only lower case and hex is allowed, e.g. 1c5d0386bbf7");
		}
		
		// Create Device instance
		IDevice device = new Device();
		device.setIdentifier(identifier);
		device.setLatitude(latitude);
		device.setLongitude(longitude);
		
		// Persist device with repository
		DeviceRepository repository = new DeviceRepository();
		repository.setPersister(this.persister);
		boolean success = repository.persist(device);
		repository.destroy();

		return JsonWebResponse.build(success);
	}
	
	@GET
	@Path("list")
	public String list() {

		DeviceRepository repository = new DeviceRepository();
		repository.setPersister(this.persister);
		List<IDevice> deviceList = repository.getAllDevices();
		repository.destroy();		
		
		return JsonWebResponse.build(true, deviceList);
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