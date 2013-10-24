package com.th.nuernberg.quakedetec.screens;

import java.util.List;
import java.util.Locale;

import com.th.nuernberg.quakedetec.R;
import com.th.nuernberg.quakedetec.service.BackgroundService;
import com.th.nuernberg.quakedetec.service.BackgroundService.BackgroundServiceBinder;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

public class Info extends Fragment {

	private static Info info;

	private Geocoder geoCoder;
	private Location location = null;
	private EditText connectedDevices;
	private EditText lastEarthquake;
	private EditText yourLocation;
	private EditText locationProvider;

	BackgroundService mService;
	boolean mBound = false;

	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			// We've bound to LocalService, cast the IBinder and get
			// LocalService instance
			BackgroundServiceBinder binder = (BackgroundServiceBinder) service;
			mService = binder.getService();
			mBound = true;
			setLocationInfo();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mBound = false;

		}
	};

	public Info() {
		setRetainInstance(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.info, container, false);

		connectedDevices = (EditText) rootView
				.findViewById(R.id.edittext_connected_devices);
		lastEarthquake = (EditText) rootView
				.findViewById(R.id.edittext_last_earthquake);
		yourLocation = (EditText) rootView
				.findViewById(R.id.edittext_your_location);
		locationProvider = (EditText) rootView
				.findViewById(R.id.edittext_location_provider);

		this.geoCoder = new Geocoder(container.getContext());
		//setLocationInfo();
		return rootView;
	}

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

	public void setLocationInfo() {
		
		if(mService == null)
			return;
		
		location = mService.getLocation();
				
		if (locationProvider != null && location != null) {
			locationProvider.setText(location.getProvider().toUpperCase(Locale.ENGLISH));

			try {
				List<Address> addressList = geoCoder.getFromLocation(
						location.getLatitude(), location.getLongitude(), 1);
				String addressString = "";

				Address address = addressList.get(0);

				for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
					addressString += address.getAddressLine(i) + "\n";
				}

				addressString = addressString.substring(0,
						addressString.length() - 1);
				yourLocation.setText(addressString);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static Info getInfo() {
		return Info.info;
	}

	public static void setInfoActivity(Info info) {
		Info.info = info;
	}
}
