package com.th.nuernberg.quakedetec.location;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

public class Localizer {
	private static String TAG = "Localizer";
	private LocationManager locationManager;

	public Localizer(Context context) {
		final String serviceString = Context.LOCATION_SERVICE;

		locationManager = (LocationManager) context.getSystemService(serviceString);

		if (locationManager == null) {
			System.err.println("Error could not create locationManager");
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
		Location location = null;

		try {
			location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			if (location == null) {
				location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			}

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
}
