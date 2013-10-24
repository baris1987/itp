package com.th.nuernberg.quakedetec.screens;

import com.th.nuernberg.quakedetec.R;
import com.th.nuernberg.quakedetec.acceleration.Accelerometer;
import com.th.nuernberg.quakedetec.location.Localizer;

import android.app.Activity;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.view.MenuItem;


// Activity for SettingsFragment

public class Settings extends Activity {

	@Override
	 protected void onCreate(Bundle savedInstanceState) {
	  // TODO Auto-generated method stub
	  super.onCreate(savedInstanceState);
	  getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
	  getActionBar().setDisplayHomeAsUpEnabled(true); // adds Backbutton to the ActionBar
	 }
	
	
	// action for backbutton ---> navigates to parentactivity (Main)
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    // Respond to the action bar's Up/Home button
	    case android.R.id.home:
	        finish();
	        return true;
	    }
	    return super.onOptionsItemSelected(item);
	}
	
	public static class SettingsFragment extends PreferenceFragment {
	    @Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        addPreferencesFromResource(R.xml.preferences);
	    }
	}
	
	public static void setSettings(SharedPreferences prefs, String key)
	{
		System.out.println("setSettings: " + key);
		
		//DeviceMap Maptype
		if(key.equals("map_type"))
		{
			if(DeviceMap.getDeviceMap() != null)
			{
				if(DeviceMap.getDeviceMap().getMap() != null)
				{
					int maptype = Integer.parseInt(prefs.getString("map_type", "1"));
					System.out.println("Deviemapupdate: " + maptype);
					DeviceMap.getDeviceMap().getMap().setMapType(maptype);
				}
			}
		}
		
		//LocationManager Aktualisiserungsinterval
		else if(key.equals("locationupdates_interval"))
		{
			long locationUpdateInterval = Long.parseLong(prefs.getString("locationupdates_interval", "1000"));
			System.out.println("interval: " + locationUpdateInterval);
			Localizer.getLocalizer().changeUpdateIntervall(locationUpdateInterval);
		}
		
		//LocationManager Aktualisierungsradius
		else if(key.equals("locationupdates_radius"))
		{
			float locationUpdateRadius = Float.parseFloat(prefs.getString("locationupdates_radius", "5"));
			System.out.println("radiusupdate: " + locationUpdateRadius);
			Localizer.getLocalizer().changeUpdateRadius(locationUpdateRadius);
		}
		
		// Beschleunigungssensor Abtastrate
		else if(key.equals("accelerometer_rate"))
		{
			Integer accelRate = Integer.parseInt(prefs.getString("accelerometer_rate", "0"));			
			Accelerometer.getAccelerometer().setNewSensorRate(accelRate);
		}
	}
}


