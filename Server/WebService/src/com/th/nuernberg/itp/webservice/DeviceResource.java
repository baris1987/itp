package com.th.nuernberg.itp.webservice;

import com.th.nuernberg.itp.webservice.interfaces.*;

import javax.ws.rs.*;

@Path("device")
public class DeviceResource extends BaseResource implements IWebServiceDevice {

	private final IConfiguration config;
	private final IDatabase database;
	
	public DeviceResource() {
		this.config = this.createConfigurationInstance(Constants.Configuration);
		this.database = this.createDatabaseInstance(this.config);
	}

	@GET
	@Path("register/{id}")
	public String register(@PathParam("id") String id) {
		return "{register: \"" + id + "\"}";
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