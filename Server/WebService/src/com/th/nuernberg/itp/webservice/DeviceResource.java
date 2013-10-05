package com.th.nuernberg.itp.webservice;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import com.th.nuernberg.itp.webservice.interfaces.*;
import com.th.nuernberg.itp.webservice.types.Device;
import com.th.nuernberg.itp.webservice.types.DeviceRepository;
import com.th.nuernberg.itp.webservice.types.Notification;
import com.th.nuernberg.itp.webservice.types.NotificationRepository;

import javax.ws.rs.*;

@Path("device")
public class DeviceResource extends BaseResource implements IWebServiceDevice {

	private final IConfiguration config;
	private final IDatabase persister;
	
	public DeviceResource() {
		this.config = this.createConfigurationInstance(Constants.Configuration);
		this.persister = this.createDatabaseInstance(this.config);
	}

	// POST
	@GET
	@Path("register/{identifier}/{latitude}/{longitude}")
	public String register(@PathParam("identifier") String identifier, @PathParam("latitude") double latitude, @PathParam("longitude") double longitude) {
		
		// Validate MAC address 
		if (!identifier.matches("^([0-9a-f]{12})$")) {
			return JsonWebResponse.build(false, "Not a valid MAC-48 address. Only lower case and hex is allowed, e.g. 1c5d0386bbf7.");
		}
		
		String activityDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS").format(Calendar.getInstance().getTime());
		
		// Create Device instance
		IDevice device = new Device();
		device.setIdentifier(identifier);
		device.setLatitude(latitude);
		device.setLongitude(longitude);
		device.setActivity(activityDate);
		
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
		List<IDevice> deviceList = repository.getActiveDevices(Integer.parseInt(this.config.get("Application.DeviceTimeout")));
		repository.destroy();		
		
		return JsonWebResponse.build(true, deviceList);
	}	

	// POST
	@GET
	@Path("alarm/{identifier}/{latitude}/{longitude}/{level}")
	public String alarm(@PathParam("identifier") String identifier, @PathParam("latitude") double latitude, @PathParam("longitude") double longitude, @PathParam("level") int level) {
		
		NotificationRepository repository = new NotificationRepository();
		repository.setPersister(this.persister);
		
		String activityDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS").format(Calendar.getInstance().getTime());
		
		INotification notification = new Notification();
		notification.setActivity(activityDate);
		notification.setIdentifier(identifier);
		notification.setLatitude(latitude);
		notification.setLongitude(longitude);
		notification.setLevel(level);
		
		boolean success = repository.persist(notification);
		
		repository.destroy();	
		
		return JsonWebResponse.build(success);
	}
	
	@GET
	@Path("meta/{identifier}")
	public String meta(@PathParam("identifier") String identifier) {
		
		DeviceRepository repository = new DeviceRepository();
		repository.setPersister(this.persister);
		IDevice device = repository.getDevice(identifier);
		repository.destroy();	
		
		boolean success = false;
		
		if (device.getIdentifier().equals(identifier))
		{
			success = true;
		}
		
		return JsonWebResponse.build(success, device);
	}
}