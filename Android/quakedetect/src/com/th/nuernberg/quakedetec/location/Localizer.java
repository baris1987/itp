package com.th.nuernberg.quakedetec.location;

import java.util.Date;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

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
			this.fetchLocation();
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
			
		if(locationFromLastSignal != null)
		{
			System.out.println("onLocationChanged: " + location.getProvider());
			System.out.println("Accuracy location: " + location.getAccuracy() + "  <-->  Accuracy lastSignal: " + locationFromLastSignal.getAccuracy());
			System.out.println("locationFromLastSignal.getTime: " + new Date(locationFromLastSignal.getTime()) + "  <-->  location.getTime: " + new Date(location.getTime()));
			
			// Wenn locationFromLastSignal mehr als 30 Sekunden �lter ist, als die gerade gefundene Location wird sie mit der neuen ersetzt
			if(locationFromLastSignal.getTime() < (location.getTime() - 21000))
			{
				this.locationFromLastSignal = location;
				System.out.println("im getTime if locationFromLastSignal gesetzt");
			}
			else
			{
				// Wenn Genauigkeit von location besser als locationFromLastSignal
				if(location.getAccuracy() < locationFromLastSignal.getAccuracy())
				{
					this.locationFromLastSignal = location;
					System.out.println("im Accuracy if locationFromLastSignal gesetzt");
				}
			}
		}
		else
			this.locationFromLastSignal = location;
		
		//this.locationFromLastSignal = location;
		
		if(DeviceMap.getDeviceMap() != null)
			DeviceMap.getDeviceMap().setLastKnownLocation(locationFromLastSignal);
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
	
	public void fetchLocation()
	{
		Looper myLooper = Looper.getMainLooper();
        locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, this, myLooper);
        locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, this, myLooper);
	    final Handler myHandler = new Handler(myLooper);
	    myHandler.postDelayed(new Runnable() {
	         public void run() {
	             locationManager.removeUpdates(localizer);
	         }
	    }, 20000);
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
