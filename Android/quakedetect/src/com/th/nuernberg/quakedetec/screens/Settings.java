package com.th.nuernberg.quakedetec.screens;

import java.util.Map;
import java.util.Map.Entry;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.widget.Toast;

import com.th.nuernberg.quakedetec.R;
import com.th.nuernberg.quakedetec.service.BackgroundService;
import com.th.nuernberg.quakedetec.service.NotificationsService;


// Activity for SettingsFragment

public class Settings extends PreferenceActivity {

	private OnSharedPreferenceChangeListener prefChangeListener;
	
	private static Settings settingsObject;
	
	private static boolean isRestore = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		this.setTheme(Integer.parseInt(sharedPreferences.getString("application_theme", String.valueOf(R.style.AppThemeHoloLightDarkActionBar))));
		
		super.onCreate(savedInstanceState);
		getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
		getActionBar().setDisplayHomeAsUpEnabled(true); // adds Backbutton to the ActionBar
		
		settingsObject = this;
		
		prefChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
			  public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
								  
				 if(key.equals("application_theme") && !isRestore)
				 {
					 AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Settings.getSettingsObject());
					
					 alertDialogBuilder.setTitle(getResources().getString(R.string.settings_changedTheme));
							
					 alertDialogBuilder
					 	.setMessage(getResources().getString(R.string.settings_restart))
						.setCancelable(false)
						.setPositiveButton(getResources().getString(R.string.btn_ok),new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,int id) 
							{
								Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage( getBaseContext().getPackageName() );
								i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
								startActivity(i);
							}
						});
							
						AlertDialog alertDialog = alertDialogBuilder.create();
						alertDialog.show();
				 }
				 else
					 Settings.setSettings(prefs, key);
			  }
		};
		
		isRestore = false;
		
		sharedPreferences.registerOnSharedPreferenceChangeListener(prefChangeListener);
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
	        
	        Preference restorePref = (Preference) findPreference("application_restore");
	        
	        restorePref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
	             public boolean onPreferenceClick(Preference preference) {
	            	 AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Settings.getSettingsObject());
						
					 alertDialogBuilder.setTitle(getResources().getString(R.string.settings_reset));
							
					 alertDialogBuilder
					 	.setMessage(getResources().getString(R.string.settings_reset_sure))
						.setCancelable(false)
						.setPositiveButton(getResources().getString(R.string.btn_ok),new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,int id) 
							{
								isRestore = true;
								Settings.getSettingsObject().resetPrefsToDefault();
							}
						});
					 
					 alertDialogBuilder.setNegativeButton(getResources().getString(R.string.btn_cancel), new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,int id) 
							{
								//dialog.cancel();
							}
						});
					 
					 AlertDialog alertDialog = alertDialogBuilder.create();
					 alertDialog.show();
	            	 return true;
	             }
	         });
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
			int locationUpdateInterval = (int) Long.parseLong(prefs.getString("locationupdates_interval", "300000"));
			BackgroundService.getBackgroundService().changeGpsLocationUpdateTimerInterval(locationUpdateInterval);
		}
		
		// Beschleunigungssensor Abtastrate
		/*else if(key.equals("accelerometer_rate"))
		{
			Integer accelRate = Integer.parseInt(prefs.getString("accelerometer_rate", "1"));
			if(Accelerometer.getAccelerometer() != null)
				Accelerometer.getAccelerometer().setNewSensorRate(accelRate);
		}*/
		
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
	
	public void resetPrefsToDefault()
	{
		PreferenceManager.getDefaultSharedPreferences(this).edit().clear().commit();	// nullt die sharedPrefs
		PreferenceManager.setDefaultValues(this, R.xml.preferences, true);				// initialisiert die Prefs
		restartApp();																	// šffnet Settings Activity neu
	}
	
	public void restartThis() {
		Intent intent = getIntent();
		//overridePendingTransition(0, 0);
		finish();
		//overridePendingTransition(0, 0);
		startActivity(intent);
	}
	
	private void restartApp()
	{
		Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage( getBaseContext().getPackageName() );
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(i);
		
		Toast.makeText(this, this.getResources().getString(R.string.toast_resetSettings), Toast.LENGTH_SHORT).show();
	}
	
	public static Settings getSettingsObject()
	{
		return settingsObject;
	}
}


