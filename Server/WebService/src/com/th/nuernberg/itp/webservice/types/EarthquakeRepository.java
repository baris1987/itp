package com.th.nuernberg.itp.webservice.types;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import com.th.nuernberg.itp.webservice.interfaces.IDatabase;
import com.th.nuernberg.itp.webservice.interfaces.IEarthquake;
import com.th.nuernberg.itp.webservice.interfaces.IPersistence;

public class EarthquakeRepository  implements IPersistence {

	private IDatabase persister;

	public void setPersister(IDatabase persister) {
		this.persister = persister;
	}

	public boolean destroy() {
		try {
			this.persister.close();
		} catch (SQLException e) {
			return false;
		}
		
		return true;
	}
	
	public boolean persist(IEarthquake earthquake) {
		try {
			this.persister.execute("INSERT INTO ITP.T_EARTHQUAKE (PK_EARTHQAUKEID, LATITUDE, LONGITUDE, ACTIVITY, RADIUS, DEVICES, RATIO) "+
								   "VALUES (NEXTVAL('ITP.S_EARTHQUAKE'), "+earthquake.getLatitude()+", "+earthquake.getLongitude()+", '"+earthquake.getActivity()+"', "+earthquake.getRadius()+", "+earthquake.getDevices()+", "+earthquake.getRatio()+");");
			return true;
			
		} catch (SQLException e) {
			
		}
		
		return false;
	}
	
	public List<IEarthquake> getEarthquakes(int limit) {
		String query = "SELECT LATITUDE, LONGITUDE, ACTIVITY, RADIUS, DEVICES, RATIO FROM ITP.T_EARTHQUAKE ORDER BY PK_EARTHQAUKEID DESC LIMIT 0,"+limit;
		List<IEarthquake> list = new ArrayList<IEarthquake>();
		
		try {
			ResultSet results = this.persister.get(query);
			
			while (results.next()) {
				IEarthquake earthquake = new Earthquake();
				earthquake.setActivity(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS").format(results.getTimestamp(3)));
				earthquake.setDevices(results.getInt(5));
				earthquake.setLatitude(results.getDouble(1));
				earthquake.setLongitude(results.getDouble(2));
				earthquake.setRadius(results.getDouble(4));
				earthquake.setRatio(results.getDouble(6));
				
				list.add(earthquake);
			}
		
		} catch (SQLException e) {
			
		}
		
		return list;
	}
	
	public List<IEarthquake> getEarthquakes() {
		return getEarthquakes(Integer.MAX_VALUE);
	}
}
