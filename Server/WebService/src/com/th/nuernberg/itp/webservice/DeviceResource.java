package com.th.nuernberg.itp.webservice;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import com.th.nuernberg.itp.webservice.interfaces.*;
import com.th.nuernberg.itp.webservice.types.AndroidDevice;
import com.th.nuernberg.itp.webservice.types.Device;
import com.th.nuernberg.itp.webservice.types.DeviceRepository;
import com.th.nuernberg.itp.webservice.types.GoogleCloudMessagingConfiguration;
import com.th.nuernberg.itp.webservice.types.Notification;
import com.th.nuernberg.itp.webservice.types.NotificationRepository;

import javax.ws.rs.*;

@Path("device")
public class DeviceResource extends BaseResource implements IWebServiceDevice {

	private final IConfiguration config;
	private final IDatabase persister;
	private final ILogging console;
	
	public DeviceResource() {
		this.config = this.createConfigurationInstance(Constants.Configuration);
		this.persister = this.createDatabaseInstance(this.config);
		this.console = new Logging();
		this.console.enable(Boolean.parseBoolean(this.config.get("Application.Logging")));
	}

	@PUT
	@Path("register/{identifier}/{latitude}/{longitude}")
	public String register(@PathParam("identifier") String identifier, @PathParam("latitude") double latitude, @PathParam("longitude") double longitude) {
		
		// Validate MAC address 
		if (!identifier.matches("^([0-9a-zA-Z_-]+)$") || identifier.length() > 255 || identifier.length() < 16) {
			return JsonWebResponse.build(false, "Invalid identifier: Only characters, numbers, dashes and underscores are allowed. Minimum length is 16, maximum length is 255, e.g. G1cZ5_dA0386-T0f7.");
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

		this.console.write("METHOD", "Register",  identifier, latitude, longitude);
		
		return JsonWebResponse.build(success);
	}
	
	@GET
	@Path("list")
	public String list() {

		DeviceRepository repository = new DeviceRepository();
		repository.setPersister(this.persister);
		List<IDevice> deviceList = repository.getActiveDevices(Integer.parseInt(this.config.get("Application.DeviceTimeout")));
		repository.destroy();		
		
		this.console.write("METHOD", "List", deviceList.size());
		return JsonWebResponse.build(true, deviceList);
	}	

	@PUT
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

		this.console.write("METHOD", "Alarm", success);
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
		
		this.console.write("METHOD", "Meta", device.getIdentifier(), device.getActivity(), device.getLatitude(), device.getLongitude());
		return JsonWebResponse.build(success, device);
	}
	
	@GET
	@Path("analyze")
	public String analyze() {
		
		IGoogleCloudMessagingConfiguration config = new GoogleCloudMessagingConfiguration();
		config.setApiUrl("https://android.googleapis.com/gcm/send");
		config.setAuthorizationKey("AIzaSyAfSY3J-yW4R2AOdI4UEHpyfFqhSvTFQS8");
		
		AndroidDevice device = new AndroidDevice();
		device.setDeviceIdentifier("APA91bGG1nTq4ltHc39IC5SNDO4vhYdn83W0pia7_NvlIh1XEFRyBmi_5rPp4e1Xuol1mfhnu5pKlL-NEVDzAEu-I0e1rOqftfCaREL7EQBeJa0y43u3RP5aWqDXEx0ltqnRzHTXNt8smDiSn2VJLF1ScL-e1M7Z0jLWE5uRga0_spKbi4sL7Zo");
		

		IGoogleCloudMessagingNotification notification = new GoogleCloudMessagingNotification();
		notification.setAndroidDevices(new AndroidDevice[] { device });
		notification.setMessage("Test Notification.");
		
		IGoogleCloudMessaging messaging = new GoogleCloudMessaging();
		messaging.setMessagingConfiguration(config);
		
		messaging.send(notification);
		
		
		return "";
	}
}