package com.th.nuernberg.quakedetec.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.th.nuernberg.quakedetec.acceleration.AccelSample;
import com.th.nuernberg.quakedetec.acceleration.Accelerometer;
import com.th.nuernberg.quakedetec.location.Localizer;
import com.th.nuernberg.quakedetec.screens.Settings;

public class BackgroundService extends Service {
	private static final String TAG = "QuakeDetecService";
	public static final String ALARM = "com.th.nuernberg.quakedetec.alarm";
	public static final String ALARM_BC_KEY = "AlarmKey";

	private final IBinder binder = new BackgroundServiceBinder();
	private static BackgroundService backgroundService;

	private final int heartbeatMillis = 1000 * 600; // alle 10 Minuten
	private static int locationGpsUpdateRequestsMillis = 1000 * 60; // alle 60 sek

	private int isAlarm = 0;
	private int isAlarmCycle = 0;
	private boolean accelGreaterZero = true;
	private long alarmCycleTime = System.currentTimeMillis();
	private AccelerationBroadcastReceiver accelReceiver;
	private Localizer localizer;
	private Timer heartbeatTimer;
	private TimerTask heartBeatTimerTask;
	
	private Timer locationNetworkUpdateTimer;
	private TimerTask locationNetworkUpdateTimerTask;
	
	private Timer locationGpsUpdateTimer;
	private TimerTask locationGpsUpdateTimerTask; 
	
	private String runningLocationTimer = "none";

	// GCM
	public static final String EXTRA_MESSAGE = "message";
	public static final String PROPERTY_REG_ID = "registration_id";
	private static final String PROPERTY_APP_VERSION = "appVersion";
	private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
	private static final String SENDER_ID = "569992207546";
	private GoogleCloudMessaging gcm;
	private AtomicInteger msgId = new AtomicInteger();
	private Context context;
	private String regid;

	// END GSM

	@Override
	public void onCreate() {
		super.onCreate();
		
		backgroundService = this;
		
		Toast.makeText(this, "QuakeDetect Service Started", Toast.LENGTH_SHORT).show();
		// GSM

		context = getApplicationContext();

		// Check device for Play Services APK. If check succeeds, proceed with
		// GCM registration.
		if (checkPlayServices()) {
			gcm = GoogleCloudMessaging.getInstance(this);
			regid = getRegistrationId(context);

			if (regid.isEmpty()) {
				registerInBackground();
			}

		} else {
			Log.i(TAG, "No valid Google Play Services APK found.");
		}

		// END GSM
		localizer = new Localizer(getApplicationContext());

		// --------- Accelerometer ------------- //
		Intent accelStartIntent = new Intent(BackgroundService.this,
				Accelerometer.class);
		startService(accelStartIntent);

		// regestriert den Broadcast Receiver vom Beschleunigungssensor
		IntentFilter accelFilter = new IntentFilter(Accelerometer.ACCEL_SAMPLE);
		accelReceiver = new AccelerationBroadcastReceiver();
		registerReceiver(accelReceiver, accelFilter);
		sendPosition2Server();

		// Geräte beim Server regestrieren mit Positionsangabe

		
		
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		locationGpsUpdateRequestsMillis = (int) Long.parseLong(sharedPrefs.getString("locationupdates_interval", "30000"));
		
		localizer.updateLocation();
		
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService( Context.CONNECTIVITY_SERVICE );
	    NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
		
	    //Timer starten, wenn eine Internetverbindung besteht
	    if(activeNetInfo != null)
	    {
	    	if(activeNetInfo.isConnected())
	    	{
	    		startLocationUpdateTimerOrChangeIfNeeded();
	    		startHeartBeatTimer();
	    	}
	    }
	    
		// Settings initialisieren
		Settings.updateAll(context);

		// Hier muss die Kommunikation mit dem C2DM (Google Push Meldungen)
		// regestriert werden
	}

	private void sendPosition2Server() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {

					double lat = 0;
					double lon = 0;
					Location location = localizer.getLocation();
					if (location != null) {
						lat = location.getLatitude();
						lon = location.getLongitude();
					}
					final SharedPreferences prefs = PreferenceManager
							.getDefaultSharedPreferences(context);
					String serverUrl = prefs.getString("server_url", "");
					String serverPort = prefs.getString("server_port", "8088");

					String requestUrl = String.format(
							"http://%s:%s/itp/device/register/%s/%s/%s",
							serverUrl, serverPort, regid, lat, lon);
					Log.d(TAG, "Start server request: " + requestUrl);
					HttpClient client = new DefaultHttpClient();
					HttpPut request = new HttpPut();
					request.setURI(new URI(requestUrl));
					HttpResponse response = client.execute(request);
					int status = response.getStatusLine().getStatusCode();
					if (status != 200) {
						Log.d(TAG, "Server request faild: " + String.valueOf(status));
						return;
					}
					BufferedReader in = new BufferedReader(
							new InputStreamReader(response.getEntity()
									.getContent()));
					StringBuffer sb = new StringBuffer("");
					String l = "";
					String nl = System.getProperty("line.separator");
					while ((l = in.readLine()) != null) {
						sb.append(l + nl);
					}
					in.close();
					String data = sb.toString();
					if (data.contains("\"success\":true"))
						Log.d(TAG, "Server request OK: " + data);
					else 
						Log.d(TAG, "Server request failed: " + data);
					
				} catch (Exception e) {
					Log.d(TAG, "Server request failed: " + e.getMessage());
				}
			}
		}).start();
	}

	private void sendAlarmToServer() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					double lat = 0;
					double lon = 0;
					Location location = localizer.getLocation();
					if (location != null) {
						lat = location.getLatitude();
						lon = location.getLongitude();
					}
					final SharedPreferences prefs = PreferenceManager
							.getDefaultSharedPreferences(context);
					String serverUrl = prefs.getString("server_url", "");
					String serverPort = prefs.getString("server_port", "8088");
					String requestUrl = String.format(
							"http://%s:%s/itp/device/alarm/%s/%s/%s/%s",
							serverUrl, serverPort, regid, lat, lon, 1);
					Log.d(TAG, "Start server request: " + requestUrl);
					HttpClient client = new DefaultHttpClient();
					HttpPut request = new HttpPut();
					request.setURI(new URI(requestUrl));
					HttpResponse response = client.execute(request);
					int status = response.getStatusLine().getStatusCode();
					if (status != 200) {
						Log.d(TAG,
								"Server request failed: "
										+ String.valueOf(status));
						return;
					}
					BufferedReader in = new BufferedReader(
							new InputStreamReader(response.getEntity()
									.getContent()));
					StringBuffer sb = new StringBuffer("");
					String l = "";
					String nl = System.getProperty("line.separator");
					while ((l = in.readLine()) != null) {
						sb.append(l + nl);
					}
					in.close();
					String data = sb.toString();
					if (data.contains("\"success\":true"))
						Log.d(TAG, "Server request OK: " + data);
					else
						Log.d(TAG, "Server request failed: " + data);
				} catch (Exception e) {
					Log.d(TAG,
							"Exception: Server request failed: "
									+ e.getMessage());
					e.printStackTrace();
				}
			}
		}).start();
	}

	/**
	 * Check the device to make sure it has the Google Play Services APK. If it
	 * doesn't, display a dialog that allows users to download the APK from the
	 * Google Play Store or enable it in the device's system settings.
	 */
	private boolean checkPlayServices() {
		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(this);
		if (resultCode != ConnectionResult.SUCCESS) {
			Log.i(TAG, "This device is not supported. No Play Services found.");
			return false;
		}
		return true;
	}

	/**
	 * Stores the registration ID and the app versionCode in the application's
	 * {@code SharedPreferences}.
	 * 
	 * @param context
	 *            application's context.
	 * @param regId
	 *            registration ID
	 */
	private void storeRegistrationId(Context context, String regId) {
		final SharedPreferences prefs = getGcmPreferences(context);
		int appVersion = getAppVersion(context);
		Log.i(TAG, "Saving regId on app version " + appVersion);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(PROPERTY_REG_ID, regId);
		editor.putInt(PROPERTY_APP_VERSION, appVersion);
		editor.commit();
	}

	/**
	 * Gets the current registration ID for application on GCM service, if there
	 * is one.
	 * <p>
	 * If result is empty, the app needs to register.
	 * 
	 * @return registration ID, or empty string if there is no existing
	 *         registration ID.
	 */
	private String getRegistrationId(Context context) {
		final SharedPreferences prefs = getGcmPreferences(context);
		String registrationId = prefs.getString(PROPERTY_REG_ID, "");
		if (registrationId.isEmpty()) {
			Log.i(TAG, "Registration not found.");
			return "";
		}
		// Check if app was updated; if so, it must clear the registration ID
		// since the existing regID is not guaranteed to work with the new
		// app version.
		int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION,
				Integer.MIN_VALUE);
		int currentVersion = getAppVersion(context);
		if (registeredVersion != currentVersion) {
			Log.i(TAG, "App version changed.");
			return "";
		}
		return registrationId;
	}

	/**
	 * Registers the application with GCM servers asynchronously.
	 * <p>
	 * Stores the registration ID and the app versionCode in the application's
	 * shared preferences.
	 */
	private void registerInBackground() {
		new AsyncTask<Void, Void, String>() {
			@Override
			protected String doInBackground(Void... params) {
				String msg = "";
				try {
					if (gcm == null) {
						gcm = GoogleCloudMessaging.getInstance(context);
					}
					regid = gcm.register(SENDER_ID);
					msg = "Device registered, registration ID=" + regid;
					sendRegistrationIdToBackend();
					storeRegistrationId(context, regid);
				} catch (IOException ex) {
					msg = "Error: " + ex.getMessage();
				}
				return msg;
			}

			@Override
			protected void onPostExecute(String msg) {
				Log.d(TAG, msg);
			}
		}.execute(null, null, null);
	}

	/**
	 * @return Application's version code from the {@code PackageManager}.
	 */
	private static int getAppVersion(Context context) {
		try {
			PackageInfo packageInfo = context.getPackageManager()
					.getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionCode;
		} catch (NameNotFoundException e) {
			// should never happen
			throw new RuntimeException("Could not get package name: " + e);
		}
	}

	/**
	 * @return Application's {@code SharedPreferences}.
	 */
	private SharedPreferences getGcmPreferences(Context context) {
		return getSharedPreferences(this.getPackageName(), Context.MODE_PRIVATE);
	}

	/**
	 * Sends the registration ID to your server over HTTP, so it can use
	 * GCM/HTTP or CCS to send messages to your app. Not needed for this demo
	 * since the device sends upstream messages to a server that echoes back the
	 * message using the 'from' address in the message.
	 */
	private void sendRegistrationIdToBackend() {
		// Your implementation here.
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "QuakeDetect Service started");
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		Log.d(TAG, "stopping accelerometer service...");
		Intent accelStopIntent = new Intent(BackgroundService.this,
				Accelerometer.class);
		stopService(accelStopIntent);
		unregisterReceiver(accelReceiver);
		heartbeatTimer.cancel();
		// C2DM Kommunikation stoppen?

	}

	private class AccelerationBroadcastReceiver extends BroadcastReceiver {

		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService( Context.CONNECTIVITY_SERVICE );
	    
		
		@Override
		public void onReceive(Context context, Intent intent) {
			
			NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
			if(activeNetInfo != null)
		    {
		    	if(activeNetInfo.isConnected())
		    	{
		    		if (intent.getAction().equals(Accelerometer.ACCEL_SAMPLE)) {

						AccelSample sample = intent.getParcelableExtra(Accelerometer.ACCEL_SAMPLE_KEY);
						if (sample != null) {

							// Erdbeben Auswertung
							if (sample.abs < -0.5 && accelGreaterZero) {
								isAlarm++;
								accelGreaterZero = false;
							} else if (sample.abs > 0.5)
								accelGreaterZero = true;
							// Alle 5s wird eine Auswertung gemacht
							if (System.currentTimeMillis() - alarmCycleTime > 5000) {
								double alarmRatio = 0;
								if(isAlarm != 0)
									alarmRatio = (double)isAlarm/(double)isAlarmCycle * 100.0;
								//Abhaengig von der Frequenz der Alarmauswertung wird die Anzahl der Alarme bewertet	
								double alarmFrequRel = isAlarmCycle * 0.01 + 0.5;
									alarmRatio = alarmRatio * alarmFrequRel;
							//	Log.e(TAG + "_ALARM", "AlarmCount " + String.valueOf(isAlarm) + "/" + String.valueOf(isAlarmCycle) + "->" + String.valueOf(alarmRatio));
								
								if (alarmRatio > 25) {
								//	Log.e(TAG + "_ALARM", "EARTHQUAKE!");
									Toast.makeText(getApplicationContext(), "Earthquake", Toast.LENGTH_SHORT).show();
									sendAlarmToServer();
								}
								alarmCycleTime = System.currentTimeMillis();
								isAlarmCycle = 0;
								isAlarm = 0;
							}
							isAlarmCycle++;
						}
					}
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

	public void startLocationUpdateTimerOrChangeIfNeeded()
	{
		ArrayList<String> enabledProvider = Localizer.getLocalizer().getEnabledProvider();
		
		if(enabledProvider.contains(LocationManager.NETWORK_PROVIDER))
		{
			if(!runningLocationTimer.equals(LocationManager.NETWORK_PROVIDER))
			{
				stopLocationUpdateTimer();
				
				locationNetworkUpdateTimer = new Timer("locationNetworkUpdateTimer");
				locationNetworkUpdateTimerTask = new TimerTask() {
					public void run() {
						Log.d(TAG, "run location timer for Network");
						startLocationUpdateTimerOrChangeIfNeeded();
						Localizer.getLocalizer().updateLocation();
					}
				};
					
				locationNetworkUpdateTimer.scheduleAtFixedRate(locationNetworkUpdateTimerTask, 20000, 30000);
				runningLocationTimer = LocationManager.NETWORK_PROVIDER;
				
				Looper myLooper = Looper.getMainLooper();
				final Handler myHandler = new Handler(myLooper);
			    myHandler.postDelayed(new Runnable() {
			         public void run() {
			        	 Toast.makeText(context, "LocationProvider: Now using NETWORK", Toast.LENGTH_SHORT).show();
			         }
			    }, 0);				
			}
		}
		else if(enabledProvider.contains(LocationManager.GPS_PROVIDER))
		{
			if(!runningLocationTimer.equals(LocationManager.GPS_PROVIDER))
			{
				stopLocationUpdateTimer();
								
				locationGpsUpdateTimer = new Timer("locationGpsUpdateTimer");
				locationGpsUpdateTimerTask = new TimerTask() {
					public void run() {
						Log.d(TAG, "Run location timer for GPS");
						startLocationUpdateTimerOrChangeIfNeeded();
						Localizer.getLocalizer().updateLocation();
					}
				};
				
				locationGpsUpdateTimer.scheduleAtFixedRate(locationGpsUpdateTimerTask, 20000, locationGpsUpdateRequestsMillis);
				runningLocationTimer = LocationManager.GPS_PROVIDER;
				
				Looper myLooper = Looper.getMainLooper();
				final Handler myHandler = new Handler(myLooper);
			    myHandler.postDelayed(new Runnable() {
			         public void run() {
			        	 Toast.makeText(context, "LocationProvider: Now using GPS", Toast.LENGTH_SHORT).show();
			         }
			    }, 0);
			}
		}
	}
	
	public void stopLocationUpdateTimer()
	{
		if(locationGpsUpdateTimer != null && locationGpsUpdateTimerTask != null)
		{
			locationGpsUpdateTimer.cancel();
			locationGpsUpdateTimerTask.cancel();
		}
		
		if(locationNetworkUpdateTimer != null && locationNetworkUpdateTimerTask != null)
		{
			locationNetworkUpdateTimer.cancel();
			locationNetworkUpdateTimerTask.cancel();
		}
		
		runningLocationTimer = "none";
	}
	
	public void changeGpsLocationUpdateTimerInterval(int milliseconds) {
		locationGpsUpdateRequestsMillis = milliseconds;
		
		stopLocationUpdateTimer();
		
		locationGpsUpdateTimer = new Timer("locationGpsUpdateTimer");
		locationGpsUpdateTimerTask = new TimerTask() {
			public void run() {
				Log.d(TAG, "run location timer");
				startLocationUpdateTimerOrChangeIfNeeded();
				Localizer.getLocalizer().updateLocation();
			}
		};
		
		startLocationUpdateTimerOrChangeIfNeeded();
	}
	
	public void startHeartBeatTimer()
	{
		stopHeartBeatTimer();
		
		heartbeatTimer = new Timer("heartbeatTimer");
		
		heartbeatTimer = new Timer("heartbeatTimer");
		heartBeatTimerTask = new TimerTask() {
			public void run() {
				Log.d(TAG, "HeartBeat!");
				Localizer localizer = Localizer.getLocalizer();
				localizer.fireNotificationIfAllProvidersDisabled();
				startLocationUpdateTimerOrChangeIfNeeded();
				
				Location location = localizer.getLocation();
				if (location != null) {
					sendPosition2Server();
				} else {
					Log.d(TAG, "No location fix -> Heartbeat not send");
				}
			}
		};
		
		heartbeatTimer.scheduleAtFixedRate(heartBeatTimerTask, 2000, heartbeatMillis);
	}
		
		
	public void stopHeartBeatTimer()
	{
		if(heartbeatTimer != null)
			heartbeatTimer.cancel();
		if(heartBeatTimerTask != null)
			heartBeatTimerTask.cancel();
	}
	
	public static BackgroundService getBackgroundService()
	{
		return backgroundService;
	}
}
