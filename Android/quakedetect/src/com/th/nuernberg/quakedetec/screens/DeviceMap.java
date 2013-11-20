package com.th.nuernberg.quakedetec.screens;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.th.nuernberg.quakedetec.R;
import com.th.nuernberg.quakedetec.service.BackgroundService;
import com.th.nuernberg.quakedetec.service.BackgroundService.BackgroundServiceBinder;

public class DeviceMap extends Fragment {
	
	private static DeviceMap deviceMap;
	
	private SharedPreferences sharedPrefs;
	private Location lastKnownLocation;
	
	private Marker myPostionMarker;
	private Circle myPostionCircle;
	
	private SupportMapFragment supportMapFragment;
	private GoogleMap googleMap;
	
	private boolean initialized = false;
	
	private static LayoutInflater inflater;
	
		
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	
		DeviceMap.inflater = inflater;
		View rootView = inflater.inflate(R.layout.devicemap, container, false);
		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this.getActivity().getApplicationContext());
		supportMapFragment = (SupportMapFragment) this.getFragmentManager().findFragmentById(R.id.google_map_fragment);
		googleMap = supportMapFragment.getMap();
		
		ImageButton mapTypeButton = (ImageButton) rootView.findViewById(R.id.map_type_button);
		mapTypeButton.setBackgroundResource(android.R.drawable.ic_menu_mapmode);
		
		mapTypeButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				DeviceMap.deviceMap.showMapTypePopup(v);
			}
		});
		
		ImageButton myLocationButton = (ImageButton) rootView.findViewById(R.id.my_location_button);
		myLocationButton.setBackgroundResource(android.R.drawable.ic_menu_compass);
		myLocationButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				DeviceMap.deviceMap.updateCameraToLastKnownLocation();
			}
		});
		
		/*googleMap.setInfoWindowAdapter(new InfoWindowAdapter() {

	        @Override
	        public View getInfoWindow(Marker arg0) {
	            return null;
	        }

	        @Override
	        public View getInfoContents(Marker marker) {
	        	View view = DeviceMap.inflater.inflate(R.layout.marker, null);
            	TextView infoText= (TextView) view.findViewById(R.id.marker_view);
            	
	        	if(marker.getTitle().equals("myPosition"))
	        	{
	        		SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss", Locale.GERMAN);
	        		Date date = new Date(lastKnownLocation.getTime());
	        		String posRefresh 	= "<b>Positionsaktualisierung: " + formatter.format(date) + " Uhr</b>";
	        		String posAccuracy 	= "<b>Genauigkeit: " + lastKnownLocation.getAccuracy() + "m</b>";
	        		infoText.setText(Html.fromHtml(posRefresh + "<br>" + posAccuracy));
	        	}
	        	else {
					infoText.setText(Html.fromHtml("<b>" + marker.getTitle() + "</b>"));
				}

	            return view;
	        }
	    });*/
		
		return rootView;
	}


	BackgroundService mService;
	boolean mBound = false;

	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			BackgroundServiceBinder binder = (BackgroundServiceBinder) service;
			mService = binder.getService();
			mBound = true;
			if(mService.getLocation() != null)
				setLastKnownLocation(mService.getLocation());
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
			initialized = false;
		}
	}
	
	
	public void initMap()
	{		
		if(!initialized)
		{
			if(googleMap != null)
			{
				googleMap.clear();
				googleMap.setMyLocationEnabled(false);
									
				googleMap.setMapType(Integer.parseInt(sharedPrefs.getString("map_type", "1")));
					
				updateCameraToLastKnownLocation();
					
				//if(lastKnownLocation != null)
						//setLastKnownLocation(lastKnownLocation);
					
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
				initialized = true;
			}
		}
	}
	
	// fügt Marker für andere Geräte in der Umgebung hinzu
	public void addMarkerToMap(LatLng latlng, String DeviceInfo)
	{
	        LatLng MarkerPos = latlng;
	        MarkerOptions markerOptions = new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)).position(MarkerPos);
	        markerOptions.title(DeviceInfo);
	        googleMap.addMarker(markerOptions);
	}
	
	public void updateCameraToLastKnownLocation()
	{
		if(googleMap != null && lastKnownLocation != null)
		{
			LatLng latLng = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
		    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);
		    googleMap.animateCamera(cameraUpdate);
		}
	}
	
	public void setLastKnownLocation(Location location)
	{
		this.lastKnownLocation = location;
		if(!initialized)
			initMap();
		
		changeMyPositionMarker();
	}
	
	public Location getLastKnownLocation()
	{
		return this.lastKnownLocation;
	}
	
	public void changeMyPositionMarker()
	{		
		if(myPostionMarker != null)
			myPostionMarker.remove();
		if(myPostionCircle != null)
			myPostionCircle.remove();
			
		LatLng myPosition = new LatLng (this.lastKnownLocation.getLatitude(), this.lastKnownLocation.getLongitude());
		
		MarkerOptions markerOptions = new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)).position(myPosition);
		//markerOptions.title("Deine Positionsdaten").snippet("Position um " + formatter.format(date) + "Genauigkeit von " + lastKnownLocation.getAccuracy() + "m");
		markerOptions.title("myPosition");
		CircleOptions circleOptions = new CircleOptions();
		circleOptions.center(myPosition);
		circleOptions.radius(lastKnownLocation.getAccuracy());
		circleOptions.fillColor(0x40007FFF);
		circleOptions.strokeColor(0x50007FFF);
		circleOptions.strokeWidth(2);
		
		myPostionMarker = googleMap.addMarker(markerOptions);
		myPostionCircle = googleMap.addCircle(circleOptions);
	}
	
	public boolean isInitialized()
	{
		return this.initialized;
	}
	
	public GoogleMap getGoogleMap()
	{
		return this.googleMap;
	}
	
	public static DeviceMap getDeviceMap()
	{
		return DeviceMap.deviceMap;
	}
	
	public static void setDeviceMap(DeviceMap deviceMap)
	{
		DeviceMap.deviceMap = deviceMap;
	}
	
	private void showMapTypePopup(View v)
	{
		PopupMenu popupMenu = new PopupMenu(this.getActivity(), v);
		popupMenu.getMenuInflater().inflate(R.menu.maptype_popup, popupMenu.getMenu());
		
		popupMenu.getMenu().getItem(this.googleMap.getMapType()-1).setChecked(true);
		
		popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() 
		{
			@Override
			public boolean onMenuItemClick(MenuItem item) 
			{
				switch (item.getItemId()) 
				{
				       case R.id.menu_maptype_normal:
				           DeviceMap.getDeviceMap().googleMap.setMapType(1);
				           return true;
				       case R.id.menu_maptype_satellite:
				       	DeviceMap.getDeviceMap().googleMap.setMapType(2);
				           return true;
				       case R.id.menu_maptype_terrain:
				           DeviceMap.getDeviceMap().googleMap.setMapType(3);
				           return true;
				       case R.id.menu_maptype_hybrid:
				       	DeviceMap.getDeviceMap().googleMap.setMapType(4);
				           return true;
				       default:
				          return false;
				}
			}
		});
		
		popupMenu.show();
		
	}
}
