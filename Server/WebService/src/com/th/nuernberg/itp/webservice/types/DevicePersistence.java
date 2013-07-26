package com.th.nuernberg.itp.webservice.types;

import java.sql.SQLException;

import com.th.nuernberg.itp.webservice.interfaces.IDatabase;
import com.th.nuernberg.itp.webservice.interfaces.IDevice;
import com.th.nuernberg.itp.webservice.interfaces.IDevicePersistence;

public class DevicePersistence implements IDevicePersistence {

	private IDevice device;
	private IDatabase persister;
	
	public void setPersister(IDatabase persister) {
		this.persister = persister;
	}
	
	public void setDevice(IDevice device) {
		this.device = device;
	}

	public IDevice getDevice() {
		return this.device;
	}	
	
	public boolean persist() {
		try {
			this.persister.execute("INSERT INTO ITP.T_DEVICE (PK_DEVICEID, IDENTIFIER, ACTIVITY, LATITUDE, LONGITUDE) " +
					  			   "VALUES (NEXTVAL('ITP.S_DEVICE'), '"+this.device.getIdentifier()+"', CURRENT_TIMESTAMP(), "+this.device.getLatitude()+", "+this.device.getLongitude()+");");
			return true;
			
		} catch (SQLException e) {
			
		}
		
		try {
			this.persister.execute("UPDATE ITP.T_DEVICE SET ACTIVITY = CURRENT_TIMESTAMP(), LATITUDE = "+this.device.getLatitude()+", LONGITUDE = "+this.device.getLongitude()+" WHERE IDENTIFIER = '"+this.device.getIdentifier()+"' ");
			return true;
			
		} catch (SQLException e) {
			
		}
		
		return false;
	}
}
