package com.th.nuernberg.itp.webservice;

import java.io.BufferedReader;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import com.th.nuernberg.itp.webservice.interfaces.*;
import com.th.nuernberg.itp.webservice.types.AndroidDevice;
import com.th.nuernberg.itp.webservice.types.Device;
import com.th.nuernberg.itp.webservice.types.DeviceRepository;
import com.th.nuernberg.itp.webservice.types.Earthquake;
import com.th.nuernberg.itp.webservice.types.EarthquakeRepository;
import com.th.nuernberg.itp.webservice.types.GoogleCloudMessagingConfiguration;
import com.th.nuernberg.itp.webservice.types.Notification;
import com.th.nuernberg.itp.webservice.types.NotificationRepository;
import com.th.nuernberg.itp.webservice.types.SystemStatistic;

import javax.ws.rs.*;

@Path("device")
public class DeviceResource extends BaseResource implements IWebServiceDevice {

	private final IConfiguration config;
	private final IDatabase persister;
	private final ILogging log;
	
	public DeviceResource() {
		this.config = this.createConfigurationInstance(Constants.Configuration);
		this.persister = this.createDatabaseInstance(this.config);
		this.log = new FileLogging(this.config.get("Application.LoggingFile"));
		this.log.enable(Boolean.parseBoolean(this.config.get("Application.Logging")));
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

		this.log.write("METHOD", "Register",  success, identifier, latitude, longitude);
		
		return JsonWebResponse.build(success);
	}
	
	@GET
	@Path("list")
	public String list() {

		DeviceRepository repository = new DeviceRepository();
		repository.setPersister(this.persister);
		List<IDevice> deviceList = repository.getActiveDevices(Integer.parseInt(this.config.get("Application.DeviceTimeout")));
		repository.destroy();		
		
		this.log.write("METHOD", "List", true, deviceList.size());
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
		
		//repository.destroy();	

		this.log.write("METHOD", "Alarm", success);
		
		analyze(latitude, longitude);
		
		return JsonWebResponse.build(success);
	}
	
	@GET
	@Path("meta")
	public String meta() {
		
		String version = this.config.get("Application.Version");
		
		ISystemStatistic stats = new SystemStatistic();
		
		DeviceRepository deviceRepository = new DeviceRepository();
		EarthquakeRepository erathquakeRepository = new EarthquakeRepository();
		deviceRepository.setPersister(this.persister);
		erathquakeRepository.setPersister(this.persister);
		
		List<IDevice> deviceList = deviceRepository.getActiveDevices(Integer.parseInt(this.config.get("Application.DeviceTimeout")));
		List<IEarthquake> earthquakeList = erathquakeRepository.getEarthquakes(15);
		
		deviceRepository.destroy();		
		
		stats.setConnectedDevices(deviceList.size());
		stats.setEarthquakes(earthquakeList);
		stats.setVersion(version);
		
		return JsonWebResponse.build(true, stats);
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
		
		this.log.write("METHOD", "Meta", device.getIdentifier(), device.getActivity(), device.getLatitude(), device.getLongitude());
		return JsonWebResponse.build(success, device);
	}
	
	@GET
	@Path("debug")
	public String debug() {
		try {
		   BufferedReader bufferedReader = new BufferedReader(new FileReader(this.config.get("Application.LoggingFile")));
		    
	        StringBuilder debugMessagesBuilder = new StringBuilder();
	        String line = bufferedReader.readLine();

	        while (line != null) {
	            debugMessagesBuilder.append(line);
	            debugMessagesBuilder.append("\n");
	            line = bufferedReader.readLine();
	        }
	        bufferedReader.close();
	        return debugMessagesBuilder.toString();
		} 
		catch (Exception e) {
			return "Exception: " + e.getMessage();
		}
	}
	
	@POST
	@Path("analyze/{latitude}/{longitude}")
	public String analyze(@PathParam("latitude") double latitude, @PathParam("longitude") double longitude) {
		
		int searchDistanceKm = Integer.parseInt(this.config.get("Algorithm.SearchDistance"));
		int notifyDistanceKm = Integer.parseInt(this.config.get("Algorithm.NotifyDistance"));
		int notifyTimeoutSeconds = Integer.parseInt(this.config.get("Algorithm.NotifyTimeout"));
		int deviceTimeoutSeconds = Integer.parseInt(this.config.get("Algorithm.DeviceTimeout"));
		double detectionRatio = Double.parseDouble(this.config.get("Algorithm.Ratio"));
		String notifyMessage = this.config.get("Algorithm.NotifyText");
		
		boolean success = false;
		boolean updatedLastNotifications = false;
		boolean savedEarthquake = false;
		
		
		IGoogleCloudMessagingConfiguration config = new GoogleCloudMessagingConfiguration();
		config.setApiUrl(this.config.get("CloudMessaging.Api"));
		config.setAuthorizationKey(this.config.get("CloudMessaging.Authorization"));
	
		List<IAndroidDevice> deviceList = new ArrayList<IAndroidDevice>();
		DeviceRepository deviceRepository = new DeviceRepository();
		EarthquakeRepository earthquakeRepository = new EarthquakeRepository();
		
		deviceRepository.setPersister(this.persister);
		earthquakeRepository.setPersister(this.persister);
		double ratio = deviceRepository.getDetectionRatio(searchDistanceKm, deviceTimeoutSeconds, notifyTimeoutSeconds, latitude, longitude);

		if (ratio >= detectionRatio) {
			
			deviceList = deviceRepository.getNotifyDevices(notifyDistanceKm, deviceTimeoutSeconds, notifyTimeoutSeconds, latitude, longitude);
			
			if (deviceList.size() > 0) {
				IAndroidDevice[] devices = deviceList.toArray(new AndroidDevice[0]);
				
				IGoogleCloudMessagingNotification notification = new GoogleCloudMessagingNotification();
				notification.setAndroidDevices(devices);
				notification.setMessage(notifyMessage);
				
				IGoogleCloudMessaging messaging = new GoogleCloudMessaging();
				messaging.setMessagingConfiguration(config);
				messaging.send(notification);
				
				updatedLastNotifications = deviceRepository.setDeviceLastNotifications(devices, Calendar.getInstance().getTime());
			}
			
			String activityDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS").format(Calendar.getInstance().getTime());
			Earthquake earthquake = new Earthquake();
			earthquake.setLatitude(latitude);
			earthquake.setLongitude(longitude);
			earthquake.setActivity(activityDate);
			earthquake.setRadius(searchDistanceKm);
			earthquake.setRatio(ratio);
			earthquake.setDevices(deviceList.size());
			
			savedEarthquake = earthquakeRepository.persist(earthquake);
			
			success = true;
			
		}
	
		//deviceRepository.destroy();	

		this.log.write("METHOD", "algorithm", "searchDistance [km]", searchDistanceKm, "notifyDistance [km]", notifyDistanceKm, "notifyTimeout [s]", notifyTimeoutSeconds, "detectionRatio [%]", detectionRatio);
		this.log.write("METHOD", "analyze", "current ratio", ratio, "notify devices", deviceList.size(), "updated notifies", updatedLastNotifications, "saved earthquake", savedEarthquake);
		return JsonWebResponse.build(success);
	}
}