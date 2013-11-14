package com.th.nuernberg.quakedetec.screens;

import java.util.Map;
import java.util.Map.Entry;

import com.th.nuernberg.quakedetec.R;
import com.th.nuernberg.quakedetec.acceleration.Accelerometer;
import com.th.nuernberg.quakedetec.location.Localizer;
import com.th.nuernberg.quakedetec.service.BackgroundService;
import com.th.nuernberg.quakedetec.service.NotificationsService;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
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
		//DeviceMap Maptype
		if(key.equals("map_type"))
		{
			if(DeviceMap.getDeviceMap() != null)
			{
				if(DeviceMap.getDeviceMap().getMap() != null)
				{
					int maptype = Integer.parseInt(prefs.getString("map_type", "1"));
					DeviceMap.getDeviceMap().getMap().setMapType(maptype);
				}
			}
		}
		
		//LocationManager Aktualisiserungsinterval
		else if(key.equals("locationupdates_interval"))
		{
			
			int locationUpdateInterval = (int) Long.parseLong(prefs.getString("locationupdates_interval", "120000"));
			System.out.println("changeLocationUpdateInterval: " + locationUpdateInterval);
			BackgroundService.changeLocationUpdateTimerInterval(locationUpdateInterval);
		}
		
		// Beschleunigungssensor Abtastrate
		else if(key.equals("accelerometer_rate"))
		{
			Integer accelRate = Integer.parseInt(prefs.getString("accelerometer_rate", "2"));
			if(Accelerometer.getAccelerometer() != null)
				Accelerometer.getAccelerometer().setNewSensorRate(accelRate);
		}
		
		// Notification Settings
		else if(key.equals("notification_sound") || key.equals("notification_vibrate") || key.equals("notification_led"))
		{
			boolean sound = prefs.getBoolean("notification_sound", true);	
			boolean vibration = prefs.getBoolean("notification_vibrate", true);	
			boolean led = prefs.getBoolean("notification_led", true);	

			NotificationsService.setNotificationSettings(sound, vibration, led);
		}
	}
	
	// alle App Settings werden durchlaufen und gesetzt
	// Aufruf beim Start im BackgroundService
	// (evtl. setSettings und updateAll Methoden entfernen und Ÿber onStart, onStop, usw. in den jeweiligen Klassen regeln
	// um cleaneren code zu erzeugen)
	public static void updateAll(Context context)
	{
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		Map<String, ?> prefsMap = sharedPrefs.getAll();
		
		for (Entry<String, ?> entry : prefsMap.entrySet()) {
		    Settings.setSettings(sharedPrefs, entry.getKey());
		}
	}
}


