package com.th.nuernberg.itp.earthquakedetection;

import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class DeviceMap implements LocationListener {

	private SupportMapFragment sMapFragment;
	private LocationManager locationManager;
	private SharedPreferences sharedPrefs;
	
	public DeviceMap(LocationManager locationManager, SharedPreferences sharedPrefs)
	{
		sMapFragment = SupportMapFragment.newInstance();
		this.locationManager = locationManager;
		this.sharedPrefs = sharedPrefs;
	}
	
	public void initMap()
	{
		if(sMapFragment.getMap() != null)
		{
			GoogleMap map = sMapFragment.getMap();
			map.setMyLocationEnabled(true);
			map.setMapType(Integer.parseInt(sharedPrefs.getString("map_type", "0")));
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, (long)400, (float)1000, this);
		}
	}
	
	public SupportMapFragment getSMapFragment()
	{
		return sMapFragment;
	}
	
	public void addMarkerToMap(LatLng latlng)
	{
	        LatLng MarkerPos = latlng;
	        MarkerOptions markerOptions = new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)).position(MarkerPos);
	        markerOptions.title("Device...");
	        sMapFragment.getMap().addMarker(markerOptions);
	}
	
	@Override
	public void onLocationChanged(Location location) {
		if(sMapFragment.getMap() != null)
		{
			LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
		    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);
		    sMapFragment.getMap().animateCamera(cameraUpdate);
		    locationManager.removeUpdates(this);
		    

			// TestMarker
		    double abweichung = 0.001;
			addMarkerToMap(new LatLng (location.getLatitude() + abweichung, location.getLongitude()));
			addMarkerToMap(new LatLng (location.getLatitude() + 2 * abweichung, location.getLongitude() + 4 * abweichung));
			addMarkerToMap(new LatLng (location.getLatitude() + 3 * abweichung, location.getLongitude() - 2 * abweichung));
			addMarkerToMap(new LatLng (location.getLatitude() - abweichung, location.getLongitude() - 5 * abweichung));
			addMarkerToMap(new LatLng (49.458501, 11.102597)); //FH
		}
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
