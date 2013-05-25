package com.th.nuernberg.itp.earthquakedetection;

import java.util.List;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

public class InfoActivity extends Fragment implements LocationListener {

	LocationManager locationManager;
	Geocoder geoCoder;
	EditText connectedDevices;
	EditText lastEarthquake;
	EditText yourLocation;
	
	public InfoActivity() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		View rootView = inflater.inflate(R.layout.activity_info, container, false);
		
		connectedDevices 	= (EditText) rootView.findViewById(R.id.edittext_connected_devices);
		lastEarthquake		= (EditText) rootView.findViewById(R.id.edittext_last_earthquake);
		yourLocation	 	= (EditText) rootView.findViewById(R.id.edittext_your_location);
		
		connectedDevices.setText("under construction...");
		lastEarthquake.setText("under construction...");
	
		this.locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
		this.locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, (long)400, (float)1000, this);
		this.geoCoder = new Geocoder(container.getContext());
		
		return rootView;
	}

	@Override
	public void onLocationChanged(Location location) {
		try {
			List<Address> addressList = geoCoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
			String addressString = "";
			
			Address address = addressList.get(0);
			
			for(int i = 0; i < address.getMaxAddressLineIndex(); i++)
			{
				addressString += address.getAddressLine(i) + "\n"; 
			}
			
			addressString = addressString.substring(0, addressString.length()-1);
			yourLocation.setText(addressString);
				
		} catch (Exception e) {
			e.printStackTrace();
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
