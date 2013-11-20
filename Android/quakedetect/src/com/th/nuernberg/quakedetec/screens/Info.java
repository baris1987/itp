package com.th.nuernberg.quakedetec.screens;

import java.util.List;
import java.util.Locale;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import com.th.nuernberg.quakedetec.R;
import com.th.nuernberg.quakedetec.acceleration.AccelSample;
import com.th.nuernberg.quakedetec.acceleration.Accelerometer;
import com.th.nuernberg.quakedetec.location.Localizer;
import com.th.nuernberg.quakedetec.service.BackgroundService;
import com.th.nuernberg.quakedetec.service.BackgroundService.BackgroundServiceBinder;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Info extends Fragment {

	private static Info info;

	private Geocoder geoCoder;
	private Location location = null;
	private TextView connectedDevices;
	private TextView lastEarthquake;
	private TextView yourLocation;
	private TextView locationProvider;

	// Chart
	private final static int SAMPLE_SIZE = 50;
	private GraphicalView view;
	LinearLayout chartViewHolder;
	private XYSeries[] xySeries;
	private XYMultipleSeriesDataset dataset;
	private XYMultipleSeriesRenderer renderer;
	private int x = 0;
	private AccelerationBroadcastReceiver accelReceiver;
	private boolean isAccelRecieverUnregistered = true;

	private BackgroundService mService;
	private boolean mBound = false;

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

		IntentFilter filter = new IntentFilter(Accelerometer.ACCEL_SAMPLE);
		accelReceiver = new AccelerationBroadcastReceiver();
		this.getActivity().registerReceiver(accelReceiver, filter);
		isAccelRecieverUnregistered = false;
		dataset = new XYMultipleSeriesDataset();
		renderer = new XYMultipleSeriesRenderer();
		renderer = getRenderer();
		xySeries = getXYSeries();
		getXYSeriesRenderer();

		connectedDevices = (TextView) rootView.findViewById(R.id.txt_connected_devices);
		lastEarthquake = (TextView) rootView.findViewById(R.id.txt_last_earthquake);
		yourLocation = (TextView) rootView.findViewById(R.id.txt_your_location);
		locationProvider = (TextView) rootView.findViewById(R.id.txt_location_provider);

		this.geoCoder = new Geocoder(container.getContext());
		// setLocationInfo();
		initChartView(rootView);
		return rootView;
	}

	@Override
	public void onStart() {
		Intent intent = new Intent(this.getActivity(), BackgroundService.class);
		this.getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
		super.onStart();
		if (Localizer.getLocalizer() != null)
			Localizer.getLocalizer().fireNotificationIfAllProvidersDisabled();
	}

	@Override
	public void onStop() {
		super.onStop();
		// Unbind from the service
		if (accelReceiver != null && isAccelRecieverUnregistered == false) {
			this.getActivity().unregisterReceiver(accelReceiver);
			isAccelRecieverUnregistered = true;
		}
		if (mBound) {
			this.getActivity().unbindService(mConnection);
			mBound = false;
		}
	}

	@Override
	public void onResume() {
		IntentFilter filter = new IntentFilter(Accelerometer.ACCEL_SAMPLE);
		this.getActivity().registerReceiver(accelReceiver, filter);
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	private void initChartView(View rootView) {
		chartViewHolder = (LinearLayout) rootView.findViewById(R.id.chart);
		view = ChartFactory.getTimeChartView(this.getActivity(), dataset,
				renderer, "");
		view.refreshDrawableState();
		view.repaint();
		chartViewHolder.addView(view);
	}

	private XYMultipleSeriesRenderer getRenderer() {
		XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();

		renderer.setApplyBackgroundColor(true);
		renderer.setBackgroundColor(Color.TRANSPARENT);
		renderer.setMarginsColor(Color.argb(0x00, 0xff, 0x00, 0x00));
		renderer.setClickEnabled(false);
		renderer.setExternalZoomEnabled(false);
		renderer.setPanEnabled(false, false);
		renderer.setYAxisMin(-10, 0);
		renderer.setYAxisMax(15, 0);
		renderer.setPanEnabled(false, false);
		renderer.setZoomEnabled(false, false);
		renderer.setAntialiasing(true);
		renderer.setShowLegend(false);
		renderer.setYAxisAlign(Align.LEFT, 0);
		renderer.setAxesColor(Color.BLACK);

		return renderer;
	}

	private void getXYSeriesRenderer() {
		// Abs axis
		XYSeriesRenderer r = new XYSeriesRenderer();
		r.setPointStyle(PointStyle.POINT);
		r.setColor(getResources().getColor(R.color.chart_abs));
		r.setLineWidth(5);
		renderer.addSeriesRenderer(r);
	}

	private XYSeries[] getXYSeries() {
		XYSeries[] series = new XYSeries[1];
		series[0] = new XYSeries("Abs");
		dataset.addSeries(series[0]);
		return series;
	}

	public void setLocationInfo() {

		if (mService == null)
			return;

		location = mService.getLocation();

		if (locationProvider != null && location != null) {
			locationProvider.setText(location.getProvider().toUpperCase(
					Locale.ENGLISH)
					+ " (Accuracy: " + location.getAccuracy() + "m)");

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

	private class AccelerationBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(Accelerometer.ACCEL_SAMPLE)) {

				AccelSample sample = intent
						.getParcelableExtra(Accelerometer.ACCEL_SAMPLE_KEY);
				if (sample != null) {
					if (xySeries[0].getItemCount() > SAMPLE_SIZE) {
						xySeries[0].remove(0);
					}
					xySeries[0].add(x, sample.abs);
					x++;
					view.repaint();
				}
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
