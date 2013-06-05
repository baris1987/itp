package com.th.nuernberg.quakedetec.service;

import java.util.Timer;
import java.util.TimerTask;

import com.th.nuernberg.quakedetec.acceleration.AccelSample;
import com.th.nuernberg.quakedetec.acceleration.Accelerometer;
import com.th.nuernberg.quakedetec.location.Localizer;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;


public class BackgroundService extends Service {
	private static final String TAG = "QuakeDetecService";
	public static final String ALARM = "com.th.nuernberg.quakedetec.alarm";
	public static final String ALARM_BC_KEY = "AlarmKey";
	
	private final IBinder binder = new BackgroundServiceBinder();

	private final int heartbeatMillis = 1000*60; //jede Minute

	
	private AccelerationBroadcastReceiver accelReceiver;
	private Localizer localizer;
	private Timer heartbeatTimer;
	
	@Override
	public void onCreate() {
		super.onCreate();

		localizer = new Localizer(getApplicationContext());

		// --------- Accelerometer ------------- //
		Intent accelStartIntent = new Intent(BackgroundService.this, Accelerometer.class);
		startService(accelStartIntent);

		// regestriert den Broadcast Receiver vom Beschleunigungssensor
		IntentFilter filter = new IntentFilter(Accelerometer.ACCEL_SAMPLE);
		accelReceiver = new AccelerationBroadcastReceiver();
		registerReceiver(accelReceiver, filter);

		

			Location location = localizer.getLocation();
			double lat = 0.0;
			double lon = 0.0;
			if (location != null) {
				lat = location.getLatitude();
				lon = location.getLongitude();
			}
			// Ger�te beim Server regestrieren mit Positionsangabe
		
		
		heartbeatTimer = new Timer("heartbeatTimer");
		heartbeatTimer.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				Location location = localizer.getLocation();
				if (location != null) {
					// Heartbeat beim Server mit aktueller Position
				} else {
					Log.d(TAG, "No location fix -> Heartbeat not send");
				}
			}
		}, heartbeatMillis, heartbeatMillis);

		// Hier muss die Kommunikation mit dem C2DM (Google Push Meldungen) regestriert werden

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		Log.d(TAG, "stopping accelerometer service...");
		Intent accelStopIntent = new Intent(BackgroundService.this,	Accelerometer.class);
		stopService(accelStopIntent);
		unregisterReceiver(accelReceiver);
		heartbeatTimer.cancel();
		// C2DM Kommunikation stoppen?

	}

	private class AccelerationBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

			if (intent.getAction().equals(Accelerometer.ACCEL_SAMPLE)) {

				AccelSample sample = intent.getParcelableExtra(Accelerometer.ACCEL_SAMPLE_KEY);
				if (sample != null) {
					//Hier muss die Auswertung stattfinden
				}
			}
		}
	}
	


	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	
	public class BackgroundServiceBinder extends Binder {
		public BackgroundService getService() {
			return BackgroundService.this;
		}
	}
	
	 public Location getLocation() {
	      return localizer.getLocation();
	    }

	

}
