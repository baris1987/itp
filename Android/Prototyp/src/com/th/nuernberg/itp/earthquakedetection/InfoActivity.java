package com.th.nuernberg.itp.earthquakedetection;

import java.util.List;
import java.util.Locale;

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

public class InfoActivity extends Fragment {

	Geocoder geoCoder;
	EditText connectedDevices;
	EditText lastEarthquake;
	EditText yourLocation;
	EditText locationProvider;
	
	public InfoActivity() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		View rootView = inflater.inflate(R.layout.activity_info, container, false);
		
		connectedDevices 	= (EditText) rootView.findViewById(R.id.edittext_connected_devices);
		lastEarthquake		= (EditText) rootView.findViewById(R.id.edittext_last_earthquake);
		yourLocation	 	= (EditText) rootView.findViewById(R.id.edittext_your_location);
		locationProvider 	= (EditText) rootView.findViewById(R.id.edittext_location_provider);
		
		this.geoCoder = new Geocoder(container.getContext());
		
		return rootView;
	}

	public void setYourLocation(Location location)
	{
		if(location != null && locationProvider != null)
		{
			locationProvider.setText(location.getProvider().toUpperCase(Locale.ENGLISH));
			
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
	}
}
