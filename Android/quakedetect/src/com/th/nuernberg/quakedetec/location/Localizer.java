package com.th.nuernberg.quakedetec.location;

import java.util.ArrayList;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.th.nuernberg.quakedetec.screens.DeviceMap;
import com.th.nuernberg.quakedetec.screens.Info;
import com.th.nuernberg.quakedetec.service.NotificationsService;

public class Localizer implements LocationListener {
	private static String TAG = "Localizer";
	private LocationManager locationManager;
	private Location locationFromLastSignal;
	private static Localizer localizer;
	private Context context;
	final boolean gpsLocationReceived = false;
	
	public Localizer(Context context) {
		final String serviceString = Context.LOCATION_SERVICE;
		this.context = context;
		
		locationManager 	= (LocationManager) context.getSystemService(serviceString);
		
		localizer = this;
		
		if (locationManager == null) {
			System.err.println("Error could not create locationManager");
		}
		else
		{
			this.updateLocation();
		}
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

		// Wenn Genauigkeit von location schlechter als 3500 Meter
		if(location.getAccuracy() > 3500 && location.getProvider().equals(LocationManager.NETWORK_PROVIDER))
		{
			locationManager.removeUpdates(this);
			locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, this, null);
			Toast.makeText(context, "Single GPS request", Toast.LENGTH_SHORT).show();
		}
		
		this.locationFromLastSignal = location;
		
		if(DeviceMap.getDeviceMap() != null)
			DeviceMap.getDeviceMap().setLastKnownLocation(locationFromLastSignal);
		if(Info.getInfo() != null)
			Info.getInfo().setLocationInfo();
	}

	@Override
	public void onProviderDisabled(String provider) {
		fireNotificationIfAllProvidersDisabled();	
	}

	@Override
	public void onProviderEnabled(String provider) {
		fireNotificationIfAllProvidersDisabled();
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}
	
	public void updateLocation()
	{
		fireNotificationIfAllProvidersDisabled();
		ArrayList<String> enabledProvider = getEnabledProvider();
		
		if(enabledProvider.contains(LocationManager.NETWORK_PROVIDER))
		{
			updateNetworkLocation();
		}
		else if(enabledProvider.contains(LocationManager.GPS_PROVIDER)) 
		{
			updateGPSLocation();
		}
	}
	
	private void updateNetworkLocation()
	{
		Looper myLooper = Looper.getMainLooper();
	    locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, this, myLooper);
		final Handler myHandler = new Handler(myLooper);
		myHandler.postDelayed(new Runnable() {
			public void run() {
				locationManager.removeUpdates(localizer);
		    }
		}, 20000);
	}
	
	private void updateGPSLocation()
	{
		Looper myLooper = Looper.getMainLooper();
		locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, this, myLooper);
	    final Handler myHandler = new Handler(myLooper);
	    myHandler.postDelayed(new Runnable() {
	         public void run() {
	             locationManager.removeUpdates(localizer);
	         }
	    }, 30000);
	}
	
	public ArrayList<String> getEnabledProvider()
	{	
		ArrayList<String> enabledProvider = new ArrayList<String>();
		if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
			enabledProvider.add(LocationManager.GPS_PROVIDER);
		if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
			enabledProvider.add(LocationManager.NETWORK_PROVIDER);
		
		return enabledProvider;
	}
	
	public void fireNotificationIfAllProvidersDisabled()
	{
		ArrayList<String> enabledProvider = getEnabledProvider();
		
		if(enabledProvider.contains(LocationManager.NETWORK_PROVIDER) || enabledProvider.contains(LocationManager.GPS_PROVIDER))
		{
			NotificationsService.dismissLocationProviderDisabledNotification(context);
		}
		else
		{
			NotificationsService.sendLocationProviderDisabledNotification(context);
		}
	}
	
	public static Localizer getLocalizer()
	{
		return localizer;
	}
}
