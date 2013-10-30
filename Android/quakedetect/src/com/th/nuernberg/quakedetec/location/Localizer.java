package com.th.nuernberg.quakedetec.location;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.th.nuernberg.quakedetec.screens.DeviceMap;
import com.th.nuernberg.quakedetec.screens.Info;
import com.th.nuernberg.quakedetec.service.NotificationsService;

public class Localizer implements LocationListener {
	private static String TAG = "Localizer";
	private LocationManager locationManager;
	private Location locationFromLastSignal;
	private SharedPreferences sharedPrefs;
	private long locationUpdateInterval;
	private float locationUpdateRadius;
	private static Localizer localizer;
	private Context context;
	
	public Localizer(Context context) {
		final String serviceString = Context.LOCATION_SERVICE;
		this.context = context;
		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		locationUpdateInterval  = Long.parseLong(sharedPrefs.getString("locationupdates_interval", "30000"));
		locationUpdateRadius 	= Float.parseFloat(sharedPrefs.getString("locationupdates_radius", "50"));
		locationManager 	= (LocationManager) context.getSystemService(serviceString);
		
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
			// Keine Location
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
		if(DeviceMap.getDeviceMap() != null)
			DeviceMap.getDeviceMap().setLastKnownLocation(location);
		if(Info.getInfo() != null)
			Info.getInfo().setLocationInfo();	
	}

	@Override
	public void onProviderDisabled(String provider) {
		checkProviderEnabled();	
	}

	@Override
	public void onProviderEnabled(String provider) {
		checkProviderEnabled();
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
		this.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, locationUpdateInterval, locationUpdateRadius, this);
		this.locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, locationUpdateInterval, locationUpdateRadius, this);
	}
	
	public boolean checkProviderEnabled()
	{
		if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
		{
			NotificationsService.dismissLocationProviderDisabledNotification(context);
			return true;
		}
		else
		{
			NotificationsService.sendLocationProviderDisabledNotification(context);
			return false;
		}
	}
	
	public static Localizer getLocalizer()
	{
		return localizer;
	}
}
