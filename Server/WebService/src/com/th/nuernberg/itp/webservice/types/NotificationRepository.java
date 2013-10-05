package com.th.nuernberg.itp.webservice.types;

import java.sql.SQLException;

import com.th.nuernberg.itp.webservice.interfaces.IDatabase;
import com.th.nuernberg.itp.webservice.interfaces.INotification;
import com.th.nuernberg.itp.webservice.interfaces.IPersistence;

public class NotificationRepository implements IPersistence {

	private IDatabase persister;

	public void setPersister(IDatabase persister) {
		this.persister = persister;
	}

	public boolean persist(INotification notification) {
		try {
			this.persister.execute("INSERT INTO ITP.T_NOTIFICATION (PK_NOTIFICATIONID, FK_DEVICEID, ACTIVITY, LATITUDE, LONGITUDE, LEVEL) " + 
								   "VALUES (NEXTVAL('ITP.S_NOTIFICATION'), (SELECT PK_DEVICEID FROM ITP.T_DEVICE WHERE IDENTIFIER = '"+notification.getIdentifier()+"'), '"+notification.getActivity()+"', "+notification.getLatitude()+", "+notification.getLongitude()+", "+notification.getLevel()+");");
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
}
