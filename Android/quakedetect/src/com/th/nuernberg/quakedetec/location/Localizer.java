package com.th.nuernberg.quakedetec.location;

import com.th.nuernberg.quakedetec.screens.DeviceMap;
import com.th.nuernberg.quakedetec.screens.Info;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

public class Localizer implements LocationListener{
	private static String TAG = "Localizer";
	private LocationManager locationManager;
	private Location locationFromLastSignal;

	public Localizer(Context context) {
		final String serviceString = Context.LOCATION_SERVICE;

		locationManager = (LocationManager) context.getSystemService(serviceString);

		if (locationManager == null) {
			System.err.println("Error could not create locationManager");
		}
		else
		{
			for(String provider : locationManager.getProviders(true))
				this.locationManager.requestLocationUpdates(provider, (long)10000, (float)50, this); // Locationupdates 10 Sekunden, 50m
		}
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
}
