package com.th.nuernberg.itp.earthquakedetection;

import android.content.SharedPreferences;
import android.location.Location;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class DeviceMap extends SupportMapFragment {
	
	private static DeviceMap deviceMap;
	
	private SharedPreferences sharedPrefs;
	private Location lastKnownLocation;
	
	public DeviceMap()
	{
		setRetainInstance(true);
	}
	
	public void initMap()
	{
		System.out.println(this);
		System.out.println("getMap: " + getMap());
		if(getMap() != null)
		{
			GoogleMap map = getMap();
			map.clear();
			map.setMyLocationEnabled(true);
			map.setMapType(Integer.parseInt(sharedPrefs.getString("map_type", "1")));
			updateCamera(this.lastKnownLocation);
			System.out.println();
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
	}
	
	public void addMarkerToMap(LatLng latlng, String DeviceInfo)
	{
	        LatLng MarkerPos = latlng;
	        MarkerOptions markerOptions = new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)).position(MarkerPos);
	        markerOptions.title(DeviceInfo);
	        getMap().addMarker(markerOptions);
	}
	
	public void refreshPrefsOnDeviceMap()
	{
		if(getMap() != null)
			getMap().setMapType(Integer.parseInt(sharedPrefs.getString("map_type", "1")));
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
	
	public void setSharedPreferences(SharedPreferences sharedPreferences)
	{
		this.sharedPrefs = sharedPreferences;
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
