package com.th.nuernberg.quakedetec.screens;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.th.nuernberg.quakedetec.service.BackgroundService;
import com.th.nuernberg.quakedetec.service.BackgroundService.BackgroundServiceBinder;

public class DeviceMap extends SupportMapFragment {
	
	private static DeviceMap deviceMap;
	
	private SharedPreferences sharedPrefs;
	private Location lastKnownLocation;
	
	private boolean initialized = false;
		
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		setRetainInstance(true);
		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this.getActivity().getApplicationContext());
		return super.onCreateView(inflater, container, savedInstanceState);
	}


	BackgroundService mService;
	boolean mBound = false;

	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			BackgroundServiceBinder binder = (BackgroundServiceBinder) service;
			mService = binder.getService();
			mBound = true;
			lastKnownLocation = mService.getLocation();
			initMap();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mBound = false;

		}
	};
	
	@Override
	public void onStart() {
		Intent intent = new Intent(this.getActivity(), BackgroundService.class);
		this.getActivity().bindService(intent, mConnection,	Context.BIND_AUTO_CREATE);
		
		super.onStart();
	}

	@Override
	public void onStop() {
		super.onStop();
		// Unbind from the service
		if (mBound) {
			this.getActivity().unbindService(mConnection);
			mBound = false;
		}
	}
	
	
	public void initMap()
	{
		if(!initialized)
		{
			if(getMap() != null)
			{
				GoogleMap map = getMap();
				map.clear();
				map.setMyLocationEnabled(true);
				map.setMapType(Integer.parseInt(sharedPrefs.getString("map_type", "1")));
				
				updateCamera(this.lastKnownLocation);
				
				// TestMarker
				if(this.lastKnownLocation != null)
				{
				    double abweichung = 0.001;
					addMarkerToMap(new LatLng (this.lastKnownLocation.getLatitude() + abweichung, this.lastKnownLocation.getLongitude()), "Nexus 4");
					addMarkerToMap(new LatLng (this.lastKnownLocation.getLatitude() + 2 * abweichung, this.lastKnownLocation.getLongitude() + 4 * abweichung), "Samsung Galaxy S4");
					addMarkerToMap(new LatLng (this.lastKnownLocation.getLatitude() + 3 * abweichung, this.lastKnownLocation.getLongitude() - 2 * abweichung), "HTC One");
					addMarkerToMap(new LatLng (this.lastKnownLocation.getLatitude() - abweichung, this.lastKnownLocation.getLongitude() - 5 * abweichung), "Sony Xperia Z");
					addMarkerToMap(new LatLng (49.458501, 11.102597), "Samsung Galaxy Note"); //FH
				}
			}
			initialized = true;
		}
	}
	
	public void addMarkerToMap(LatLng latlng, String DeviceInfo)
	{
	        LatLng MarkerPos = latlng;
	        MarkerOptions markerOptions = new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)).position(MarkerPos);
	        markerOptions.title(DeviceInfo);
	        getMap().addMarker(markerOptions);
	}
	
	public void updateCamera(Location location)
	{
		if(getMap() != null && location != null)
		{
			LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
		    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);
		    getMap().animateCamera(cameraUpdate);
		}
	}
	
	public void setLastKnownLocation(Location location)
	{
		this.lastKnownLocation = location;
		updateCamera(this.lastKnownLocation);
	}
	
	public boolean isInitialized()
	{
		return this.initialized;
	}
	
	public static DeviceMap getDeviceMap()
	{
		return DeviceMap.deviceMap;
	}
	
	public static void setDeviceMap(DeviceMap deviceMap)
	{
		DeviceMap.deviceMap = deviceMap;
	}
}
