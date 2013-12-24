package com.th.nuernberg.itp.webservice.types;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.th.nuernberg.itp.webservice.interfaces.IAndroidDevice;
import com.th.nuernberg.itp.webservice.interfaces.IDatabase;
import com.th.nuernberg.itp.webservice.interfaces.IDevice;
import com.th.nuernberg.itp.webservice.interfaces.IPersistence;

public class DeviceRepository implements IPersistence {

	private IDatabase persister;

	public void setPersister(IDatabase persister) {
		this.persister = persister;
	}

	public boolean persist(IDevice device) {
		
		String notification = "";
		
		if (device.getLastNotification() != null) {
			notification = "'"+device.getLastNotification()+"'";
		}
		else {
			notification = " NULL ";
		}
		
			
		try {
			this.persister.execute("INSERT INTO ITP.T_DEVICE (PK_DEVICEID, IDENTIFIER, ACTIVITY, LATITUDE, LONGITUDE, NOTIFICATION) " +
					  			   "VALUES (NEXTVAL('ITP.S_DEVICE'), '"+device.getIdentifier()+"', '"+device.getActivity()+"', "+device.getLatitude()+", "+device.getLongitude()+", "+device.getLastNotification()+");");
			return true;
			
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		
		try {
			this.persister.execute("UPDATE ITP.T_DEVICE SET ACTIVITY = '"+device.getActivity()+"', LATITUDE = "+device.getLatitude()+", LONGITUDE = "+device.getLongitude()+", NOTIFICATION = "+notification+" WHERE IDENTIFIER = '"+device.getIdentifier()+"' ");
			return true;
			
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		
		return false;
	}

	public boolean destroy() {
		try {
			this.persister.close();
		} catch (SQLException e) {
			return false;
		}
		
		return true;
	}
	
	public IDevice getDevice(String identifier) {
		IDevice device = new Device();
		device.setIdentifier("");
		
		try {
			ResultSet results = this.persister.get("SELECT ACTIVITY, LATITUDE, LONGITUDE, NOTIFICATION FROM ITP.T_DEVICE WHERE IDENTIFIER = '"+identifier+"'");
			
			if (results.next()) 
			{
				device.setIdentifier(identifier);
				device.setActivity(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS").format(results.getTimestamp(1)));
				device.setLatitude(results.getDouble(2));
				device.setLongitude(results.getDouble(3));
				if (results.getString(4) != null) {
					device.setLastNotification(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS").format(results.getTimestamp(4)));
				}
			}
		
		} catch (SQLException e) {
			return device;
		}
		
		return device;
	}
	
	public double getDetectionRatio(int searchDistanceKm, int hasSentAlarmSeconds, double latitude, double longitude) {

		try {
			ResultSet results = this.persister.get("  SELECT sum(CONVERT(hassentalarm, DOUBLE)) / CONVERT(count(*), DOUBLE) as ratio FROM ( " + 
														"SELECT d.*, " + 
														       "NVL( " + 
														             "(SELECT CASE WHEN datediff('SECOND', activity, CURRENT_TIMESTAMP()) < "+hasSentAlarmSeconds+" THEN 1 ELSE 0 END  " + 
														             " FROM itp.t_notification " + 
														             " WHERE fk_deviceid = pk_deviceid " + 
														            "  ORDER BY activity DESC LIMIT 1), 0) hassentalarm " + 
														"FROM " + 
														 " (SELECT pk_deviceid,  " + 
																 " (6371 * acos(cos(radians("+longitude+")) * cos(radians(latitude)) * cos(radians(longitude) - radians("+latitude+")) + sin(radians("+longitude+")) * sin(radians(latitude)))) AS distance " + 
														  " FROM itp.t_device " + 
														  " GROUP BY pk_deviceid, " + 
														           " latitude, " + 
														           " longitude HAVING distance < "+ searchDistanceKm +
														" ) d) t ");

			results.next();
			double ratio = results.getDouble(1);
			return ratio;
		
		} catch (SQLException e) {
			return 0.0;
		}
	}
	
	public boolean setDeviceLastNotifications(IAndroidDevice[] devices, Date notifyDate) {
		
		if (devices.length == 0) {
			return false;
		}
		
		String lastNotifyDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS").format(notifyDate);
		StringBuilder conditions = new StringBuilder();
		
		for (IAndroidDevice device : devices) {
			conditions.append("'"+device.getIdentifier()+"', ");
		}

		String update = "UPDATE itp.t_device SET notification = '"+lastNotifyDate+"' WHERE identifier IN("+conditions.substring(0, conditions.length()-1).toString()+")";
		
		try {
			this.persister.execute(update);
		} catch (SQLException e) {
			return false;
		}
		
		return false;
	}
	
	public List<IAndroidDevice> getNotifyDevices(int notifyDistanceKm, int notifyTimeoutSeconds, double latitude, double longitude) {
		List<IAndroidDevice> deviceList = new ArrayList<IAndroidDevice>();
		
		try {
			ResultSet results = this.persister.get("  SELECT d.identifier, d.sendnotify FROM (SELECT pk_deviceid, " + 
											                    "identifier, " + 
													  "(6371 * acos(cos(radians("+longitude+")) * cos(radians(latitude)) * cos(radians(longitude) - radians("+latitude+")) + sin(radians("+longitude+")) * sin(radians(latitude)))) AS distance,  " + 
											                  "(CASE WHEN datediff('SECOND', notification, CURRENT_TIMESTAMP()) < "+ notifyTimeoutSeconds +" THEN 1 ELSE 0 END) sendnotify " + 
											   "FROM itp.t_device " + 
											   "GROUP BY pk_deviceid, " + 
											            "latitude, " + 
											           " longitude HAVING distance < "+notifyDistanceKm + 
											" ) d " +
											" WHERE sendnotify = 1 ");

			while (results.next()) {
				AndroidDevice device = new AndroidDevice();
				device.setIdentifier(results.getString(1));
				deviceList.add(device);
			}
		
		} catch (SQLException e) {
			return deviceList;
		}
		
		return deviceList;
	}
	
	public List<IDevice> getActiveDevices(int timeoutSeconds) {
		
		List<IDevice> deviceList = new ArrayList<IDevice>();
		
		try {
			ResultSet results = this.persister.get("SELECT IDENTIFIER, ACTIVITY, LATITUDE, LONGITUDE, NOTIFICATION FROM ITP.T_DEVICE WHERE DATEDIFF('SECOND', ACTIVITY, CURRENT_TIMESTAMP()) < "+timeoutSeconds);

			while (results.next()) {
				IDevice device = new Device();
				device.setIdentifier(results.getString(1));
				device.setActivity(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS").format(results.getTimestamp(2)));
				device.setLatitude(results.getDouble(3));
				device.setLongitude(results.getDouble(4));
				if (results.getString(5) != null) {
					device.setLastNotification(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS").format(results.getTimestamp(5)));
				}
				deviceList.add(device);
			}
		
		} catch (SQLException e) {
			return deviceList;
		}
		
		return deviceList;
	}
}
