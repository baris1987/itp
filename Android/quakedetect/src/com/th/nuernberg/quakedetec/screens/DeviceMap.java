package com.th.nuernberg.quakedetec.screens;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import pl.mg6.android.maps.extensions.Circle;
import pl.mg6.android.maps.extensions.CircleOptions;
import pl.mg6.android.maps.extensions.ClusteringSettings;
import pl.mg6.android.maps.extensions.DefaultClusterOptionsProvider;
import pl.mg6.android.maps.extensions.GoogleMap;
import pl.mg6.android.maps.extensions.GoogleMap.InfoWindowAdapter;
import pl.mg6.android.maps.extensions.GoogleMap.OnCameraChangeListener;
import pl.mg6.android.maps.extensions.Marker;
import pl.mg6.android.maps.extensions.MarkerOptions;
import pl.mg6.android.maps.extensions.SupportMapFragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
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
		googleMap = supportMapFragment.getExtendedMap();
		deviceMap = this;
		ImageButton mapTypeButton = (ImageButton) rootView.findViewById(R.id.map_type_button);
		mapTypeButton.setBackgroundResource(android.R.drawable.ic_menu_mapmode);
		
		mapTypeButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				DeviceMap.getDeviceMap().showMapTypePopup(v);
			}
		});
		
		ImageButton myLocationButton = (ImageButton) rootView.findViewById(R.id.my_location_button);
		myLocationButton.setBackgroundResource(android.R.drawable.ic_menu_compass);
		myLocationButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				DeviceMap.getDeviceMap().updateCameraToLastKnownLocation(12);
			}
		});
		
		googleMap.setInfoWindowAdapter(new InfoWindowAdapter() {

	        @Override
	        public View getInfoWindow(Marker arg0) {
	            return null;
	        }

	        @Override
	        public View getInfoContents(Marker marker) {
	        	View view = DeviceMap.inflater.inflate(R.layout.marker, null);
            	TextView infoText= (TextView) view.findViewById(R.id.marker_view);
            	
            	if(marker.isCluster())
            	{
            		int count = marker.getMarkers().size();
            		
            		try
            		{
	    				infoText.setText(Html.fromHtml("<b><font color=black>Devices: " + count + "</font><b>"));
            		}
            		catch (Exception e)
            		{
            			Log.d("QuakeDetec", e.toString());
            		}
            	}
            	else
            	{
		        	if(marker.getTitle().equals("myPosition"))
		        	{
		        		SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss", Locale.GERMAN);
		        		Date date = new Date(lastKnownLocation.getTime());
		        		String posRefresh 	= "<b>Positionsaktualisierung: " + formatter.format(date) + " Uhr</b>";
		        		String posAccuracy 	= "<b>Genauigkeit: " + lastKnownLocation.getAccuracy() + "m</b>";
		        		infoText.setText(Html.fromHtml("<font color=black>" + posRefresh + "<br>" + posAccuracy + "</font>"));
		        	}
		        	else {
						infoText.setText(Html.fromHtml("<b><font color=black>" + marker.getTitle() + "</font></b>"));
					}
	        	}

	            return view;
	        }
	    });
		
		OnCameraChangeListener googleCameraChangeListener = new GoogleMap.OnCameraChangeListener() 
	    {
			@Override
			public void onCameraChange(CameraPosition cameraPosition) {
				if(cameraPosition.zoom > 12)
				{
					/*CameraUpdate cameraUpdate = CameraUpdateFactory.zoomTo(12);
				    googleMap.animateCamera(cameraUpdate);*/
				}
			}
	    };
		
		googleMap.setOnCameraChangeListener(googleCameraChangeListener);
		
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
				googleMap.getUiSettings().setCompassEnabled(false);
				googleMap.setMapType(Integer.parseInt(sharedPrefs.getString("map_type", "1")));
				googleMap.getUiSettings().setZoomGesturesEnabled(false);
		
				updateCameraToLastKnownLocation(12);
					
				ClusteringSettings clusteringSettings = new ClusteringSettings();
				clusteringSettings.addMarkersDynamically(true);
				clusteringSettings.clusterSize(180);
				clusteringSettings.clusterOptionsProvider(new DefaultClusterOptionsProvider(getResources()));
				clusteringSettings.enabled(true);
				googleMap.setClustering(clusteringSettings);
				
				
				// Testmarker
				/*ProgressBar progressBar = (ProgressBar) getActivity().findViewById(R.id.progress_bar);
			    ArrayList<LatLng> devicePostions = getTestPositions(5000, 0.0001);
			    progressBar.setProgress(0);
				progressBar.setMax(devicePostions.size());
				new CreateAndAddMarkerTask(devicePostions, progressBar, 0).execute();*/
				// -----------
				
				initialized = true;
			}
		}
	}
	
	private ArrayList<LatLng> getTestPositions(int deviceCount, double abweichung)
	{
		ArrayList<LatLng> devicePosis = new ArrayList<LatLng>(deviceCount*4);
		for(int i = 0; i < deviceCount; i++)
		{
			LatLng latlng1 = new LatLng (lastKnownLocation.getLatitude() + abweichung, lastKnownLocation.getLongitude());
			LatLng latlng2 = new LatLng (lastKnownLocation.getLatitude() + 2 * abweichung, lastKnownLocation.getLongitude() + 4 * abweichung);
			LatLng latlng3 = new LatLng (lastKnownLocation.getLatitude() + 3 * abweichung, lastKnownLocation.getLongitude() - 2 * abweichung);
			LatLng latlng4 = new LatLng (lastKnownLocation.getLatitude() - abweichung, lastKnownLocation.getLongitude() - 5 * abweichung);
						
			devicePosis.add(latlng1);
			devicePosis.add(latlng2);
			devicePosis.add(latlng3);
			devicePosis.add(latlng4);
			
			abweichung = abweichung + 0.005;
		}
		return devicePosis;
	}
	
	public void addDeviceMarkerToMap(ArrayList<JSONObject> deviceJSONObjects)
	{
		ProgressBar progressBar = (ProgressBar) DeviceMap.getDeviceMap().getActivity().findViewById(R.id.progress_bar);
		ArrayList<LatLng> devicePostions = new ArrayList<LatLng>(deviceJSONObjects.size());
		String myRegId = BackgroundService.getRegistrationId();
		
		for(JSONObject deviceJsonObject : deviceJSONObjects)
		{
			try {
				String deviceRegId 	= deviceJsonObject.getString("identifier");
				Double latitude 	= deviceJsonObject.getDouble("latitude");
				Double longitude	= deviceJsonObject.getDouble("longitude");
				
				if(!deviceRegId.equals(myRegId) )
				{
					LatLng latlng = new LatLng(latitude, longitude);
					devicePostions.add(latlng);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
		new CreateAndAddMarkerTask(devicePostions, progressBar, 0).execute();
	}
	
	// Noch in Arbeit
	private void showVisibleMarkersAndRemoveOthers()
	{
		System.out.println("DisplayedMarkers: " + googleMap.getDisplayedMarkers().size());
		System.out.println("Markers: " + googleMap.getMarkers().size());
		LatLngBounds bounds = googleMap.getProjection().getVisibleRegion().latLngBounds;
		ArrayList<Marker> removeMarkers = new ArrayList<Marker>();
		
		/*for(Marker visibleMarker : visibleMarkers)
		{
			if(!bounds.contains(visibleMarker.getPosition()))
			{
				System.out.println("Removed: " + visibleMarker.getTitle());
				//visibleMarker.remove();
				removeMarkers.add(visibleMarker);
			}
		}
		
		for(Marker removeMarker : removeMarkers)
			visibleMarkers.remove(removeMarker);
		
		for(String deviceName : devicePositions.keySet())
	    {
			LatLng devicePostion = devicePositions.get(deviceName);
									
			if(bounds.contains(devicePostion))
			{
				boolean isAlreadyOnMap = false;
				for(Marker visibleMarker : visibleMarkers)
				{
					if(visibleMarker.getTitle().equals(deviceName))
						isAlreadyOnMap = true;
				}
				if(!isAlreadyOnMap)
				{
					visibleMarkers.add(addMarkerToMap(devicePostion, deviceName));
				}
			}
	    }
		
		for(Marker marker : visibleMarkers)
		{
			System.out.println("Visible marker: " + marker.getTitle());
		}*/
	}
	
	/*// fügt Marker für andere Geräte in der Umgebung hinzu
	public Marker addMarkerToMap(LatLng latlng, String DeviceInfo)
	{
		LatLng markerPos = latlng;
	    MarkerOptions markerOptions = new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)).position(markerPos);
	    markerOptions.title(DeviceInfo);

	    return googleMap.addMarker(markerOptions);
	}*/
	
	public void updateCameraToLastKnownLocation(int zoom)
	{
		if(googleMap != null && lastKnownLocation != null)
		{
			LatLng latLng = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
		    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoom);
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
		markerOptions.title("myPosition");
		CircleOptions circleOptions = new CircleOptions();
		circleOptions.center(myPosition);
		circleOptions.radius(lastKnownLocation.getAccuracy());
		circleOptions.fillColor(0x40007FFF);
		circleOptions.strokeColor(0x50007FFF);
		circleOptions.strokeWidth(2);
		
		myPostionMarker = googleMap.addMarker(markerOptions);
		myPostionMarker.setClusterGroup(-1);
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
	
	class CreateAndAddMarkerTask extends AsyncTask<String, String, String> {

		private ArrayList<MarkerOptions> markerOptionsList  = new ArrayList<MarkerOptions>();
		
		private ProgressBar progressBar;
		private int progressStatus;
		private ArrayList<LatLng> devicePosis;
		
		public CreateAndAddMarkerTask(ArrayList<LatLng> devicePosis, ProgressBar progressBar, int progressStatus)
		{
			this.devicePosis = devicePosis;
			this.progressBar = progressBar;
			this.progressStatus = progressStatus; 
			progressBar.setVisibility(ProgressBar.VISIBLE);
		}
		
		@Override
		protected String doInBackground(String... params) {
			int i = 0;
			while(this.devicePosis.size() > 0 && i < 5000 && i < devicePosis.size())
		    {				
			    MarkerOptions markerOptions = new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)).position(devicePosis.get(i));
			    markerOptions.title("Device");
			    markerOptionsList.add(markerOptions);
			    devicePosis.remove(i);

			    publishProgress(""); 
			    i++;
		    }
			return null;
		}
		
		protected void onProgressUpdate(String... progress) {
			
			progressStatus++;
			progressBar.setProgress(progressStatus);
		}
		
		protected void onPostExecute(String str) {
			for(MarkerOptions markerOptions : markerOptionsList)
	        	googleMap.addMarker(markerOptions);
			//showVisibleMarkersAndRemoveOthers();
			progressBar.setVisibility(ProgressBar.INVISIBLE);
			this.cancel(true);
			if(devicePosis.size() > 0)
				new CreateAndAddMarkerTask(devicePosis, progressBar, progressStatus).execute();
	    }
	}
}
