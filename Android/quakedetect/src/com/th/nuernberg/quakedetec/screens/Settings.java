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
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;


// Activity for SettingsFragment

public class Settings extends Activity {
	private static final String TAG = "QuakeDetecService";
	@Override
	 protected void onCreate(Bundle savedInstanceState) {
	  // TODO Auto-generated method stub
	  super.onCreate(savedInstanceState);
	  getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
	  getActionBar().setDisplayHomeAsUpEnabled(true); // adds Backbutton to the ActionBar
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.settings_options_menu, menu);
        return true;
	}
	
	// action for backbutton ---> navigates to parentactivity (Main)
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    // Respond to the action bar's Up/Home button
	    case android.R.id.home:
	        finish();
	        return true;
	    // Respond to the action bar`s Settings button
        case R.id.reset_settings:
        	Log.d(TAG, "Einstellung zurücksetzen gedrückt!");
        	resetPreferencesToDefault(this);
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
				if(DeviceMap.getDeviceMap().getGoogleMap() != null)
				{
					int maptype = Integer.parseInt(prefs.getString("map_type", "1"));
					DeviceMap.getDeviceMap().getGoogleMap().setMapType(maptype);
				}
			}
		}
		
		//LocationManager Aktualisiserungsinterval
		else if(key.equals("locationupdates_interval"))
		{
			int locationUpdateInterval = (int) Long.parseLong(prefs.getString("locationupdates_interval", "120000"));
			BackgroundService.getBackgroundService().changeGpsLocationUpdateTimerInterval(locationUpdateInterval);
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
	// (evtl. setSettings und updateAll Methoden entfernen und über onStart, onStop, usw. in den jeweiligen Klassen regeln
	// um cleaneren code zu erzeugen)
	public static void updateAll(Context context)
	{
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		Map<String, ?> prefsMap = sharedPrefs.getAll();
		
		for (Entry<String, ?> entry : prefsMap.entrySet()) {
		    Settings.setSettings(sharedPrefs, entry.getKey());
		}
	}
	
	// Funktion, die Einstellungen auf die Standardwerte setzt
	public void resetPreferencesToDefault(Context context)
	{
		SharedPreferences defaultPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = defaultPreferences.edit();
		editor.clear();
		editor.commit();
		restartThis();
	}
	
	// Hilfsfunktion für resetPreferencesToDefault
	// Workaround von http://www.devlog.en.alt-area.org/?p=1209 um das Display zu
	// refreshen, ohne werden zwar die Werte auf die Standardwerte gesetzt,
	// man müsste aber erst in eine andere Activity, bevor man in Settings eine
	// Auswirkung erkennt
	private void restartThis() {
	    finish();
	    overridePendingTransition(0, 0);
	    startActivity(getIntent());
	    overridePendingTransition(0, 0);
	}
}


