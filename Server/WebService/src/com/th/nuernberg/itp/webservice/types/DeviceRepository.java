package com.th.nuernberg.itp.webservice.types;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import com.th.nuernberg.itp.webservice.interfaces.IDatabase;
import com.th.nuernberg.itp.webservice.interfaces.IDevice;
import com.th.nuernberg.itp.webservice.interfaces.IPersistence;

public class DeviceRepository implements IPersistence {

	private IDatabase persister;

	public void setPersister(IDatabase persister) {
		this.persister = persister;
	}

	public boolean persist(IDevice device) {
		try {
			this.persister.execute("INSERT INTO ITP.T_DEVICE (PK_DEVICEID, IDENTIFIER, ACTIVITY, LATITUDE, LONGITUDE) " +
					  			   "VALUES (NEXTVAL('ITP.S_DEVICE'), '"+device.getIdentifier()+"', CURRENT_TIMESTAMP(), "+device.getLatitude()+", "+device.getLongitude()+");");
			return true;
			
		} catch (SQLException e) {
			
		}
		
		try {
			this.persister.execute("UPDATE ITP.T_DEVICE SET ACTIVITY = CURRENT_TIMESTAMP(), LATITUDE = "+device.getLatitude()+", LONGITUDE = "+device.getLongitude()+" WHERE IDENTIFIER = '"+device.getIdentifier()+"' ");
			return true;
			
		} catch (SQLException e) {
			
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

	public List<IDevice> getActiveDevices(int timeoutSeconds) {
		
		List<IDevice> deviceList = new ArrayList<IDevice>();
		
		try {
			ResultSet results = this.persister.get("SELECT IDENTIFIER, ACTIVITY, LATITUDE, LONGITUDE FROM ITP.T_DEVICE WHERE DATEDIFF('SECOND', ACTIVITY, CURRENT_TIMESTAMP()) < "+timeoutSeconds);

			while (results.next()) {
				IDevice device = new Device();
				device.setIdentifier(results.getString(1));
				device.setActivity(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS").format(results.getTimestamp(2)));
				device.setLatitude(results.getDouble(3));
				device.setLongitude(results.getDouble(4));
				
				deviceList.add(device);
			}
		
		} catch (SQLException e) {
			return deviceList;
		}
		
		return deviceList;
	}
}
