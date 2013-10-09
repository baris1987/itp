package com.th.nuernberg.quakedetec.location;

import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;

import com.th.nuernberg.quakedetec.screens.DeviceMap;
import com.th.nuernberg.quakedetec.screens.Info;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

public class Localizer implements LocationListener {
	private static String TAG = "Localizer";
	private LocationManager locationManager;
	private Location locationFromLastSignal;
	private SharedPreferences sharedPrefs;
	private long locationUpdateInterval;
	private float locationUpdateRadius;
	private static Localizer localizer;
	
	public Localizer(Context context) {
		final String serviceString = Context.LOCATION_SERVICE;
		
		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		locationUpdateInterval = Long.parseLong(sharedPrefs.getString("locationupdates_interval", "1000"));
		locationUpdateRadius 	= Float.parseFloat(sharedPrefs.getString("locationupdates_radius", "5"));
		
				
		locationManager = (LocationManager) context.getSystemService(serviceString);

		if (locationManager == null) {
			System.err.println("Error could not create locationManager");
		}
		else
		{
			this.refreshLocationUpdateRequests();
		}
		
		localizer = this;
	}

	public Location getLocation(Criteria criteria) throws SecurityException,
			IllegalArgumentException {
		String bestProvider = locationManager.getBestProvider(criteria, true);
		if (bestProvider == null) {
			System.err.println("no location providers. Did you enable them?");
		}
		Location location = locationManager.getLastKnownLocation(bestProvider);
		return location;
	}

	public Location getLocation() {
		Location location = this.locationFromLastSignal;
		try {
			if (location != null) {
				Log.v(TAG, "lat: "	+ Double.toString(location.getLatitude())
						+ ", lon: "	+ Double.toString(location.getLongitude())
						+ ", acc: " + location.getAccuracy());
			} else {
				Log.d(TAG, "Could not obtain location.");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return location;
	}

	@Override
	public void onLocationChanged(Location location) {
		this.locationFromLastSignal = location;
		System.out.println("loc changed");
		if(DeviceMap.getDeviceMap() != null)
			DeviceMap.getDeviceMap().setLastKnownLocation(location);
		if(Info.getInfo() != null)
			Info.getInfo().setLocationInfo();	
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}
	
	public void changeUpdateIntervall(long intervalMS)
	{
		locationUpdateInterval = intervalMS;
		refreshLocationUpdateRequests();
	}
	
	public void changeUpdateRadius(float meter)
	{
		locationUpdateRadius = meter;
		refreshLocationUpdateRequests();		
	}
	
	public void refreshLocationUpdateRequests()
	{
		for(String provider : locationManager.getProviders(true))
			this.locationManager.requestLocationUpdates(provider, locationUpdateInterval, locationUpdateRadius, this); // Locationupdates 10 Sekunden, 50m
	}
	
	public static Localizer getLocalizer()
	{
		return localizer;
	}
}
