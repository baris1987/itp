package com.th.nuernberg.quakedetec.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
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
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
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

	private final int heartbeatMillis = 1000 * 60; // jede Minute

	private int isAlarm = 0;
	private int isAlarmCycle = 0;
	private float oldAcclVal = 0;
	private AccelerationBroadcastReceiver accelReceiver;
	private Localizer localizer;
	private Timer heartbeatTimer;

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

		// Ger�te beim Server regestrieren mit Positionsangabe

		heartbeatTimer = new Timer("heartbeatTimer");
		heartbeatTimer.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				Location location = localizer.getLocation();
				if (location != null) {
					sendPosition2Server();
				} else {
					Log.d(TAG, "No location fix -> Heartbeat not send");
				}
			}
		}, heartbeatMillis, heartbeatMillis);

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
					final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
					String serverUrl = prefs.getString("server_url", "");
					String serverPort = prefs.getString("server_port", "8088");

					String requestUrl = String.format("http://%s:%s/itp/device/register/%s/%s/%s", serverUrl, serverPort, regid, lat, lon);
					Log.d(TAG, "Start server request: " + requestUrl);
					HttpClient client = new DefaultHttpClient();
					HttpGet request = new HttpGet();
					request.setURI(new URI(requestUrl));
					HttpResponse response = client.execute(request);
					int status = response.getStatusLine().getStatusCode();
					if (status != 200) {
						Log.d(TAG, "Server request faild: "	+ String.valueOf(status));
						return;
					}
					BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
					StringBuffer sb = new StringBuffer("");
					String l = "";
					String nl = System.getProperty("line.separator");
					while ((l = in.readLine()) != null) {
						sb.append(l + nl);
					}
					in.close();
					String data = sb.toString();
					if (!data.contains("\"success\":true"))
						Log.d(TAG, "Server request failed: " + data);
					Log.d(TAG, "Server request OK: " + data);
				} catch (Exception e) {
					Log.d(TAG, "Server request failed: " + e.getMessage());
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

					// You should send the registration ID to your server over
					// HTTP, so it
					// can use GCM/HTTP or CCS to send messages to your app.
					sendRegistrationIdToBackend();

					// For this demo: we don't need to send it because the
					// device will send
					// upstream messages to a server that echo back the message
					// using the
					// 'from' address in the message.

					// Persist the regID - no need to register again.
					storeRegistrationId(context, regid);
				} catch (IOException ex) {
					msg = "Error :" + ex.getMessage();
					// If there is an error, don't just keep trying to register.
					// Require the user to click a button again, or perform
					// exponential back-off.
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

		@Override
		public void onReceive(Context context, Intent intent) {

			if (intent.getAction().equals(Accelerometer.ACCEL_SAMPLE)) {

				AccelSample sample = intent
						.getParcelableExtra(Accelerometer.ACCEL_SAMPLE_KEY);
				if (sample != null) {

					// Erdbeben Auswertung

					// Absolutwert berechnen
					Float abs = Math.abs(sample.x) + Math.abs(sample.y)
							+ Math.abs(sample.z);

					// Liegt der momentanwert min +- 1.0 des alten Wertes wird
					// isAlarm erh�ht
					if (abs < (oldAcclVal - 1) || abs > (oldAcclVal + 1)) {
						isAlarm++;
						oldAcclVal = abs;
					}

					// Nach 100 wird geschaut wieviele Ausschl�ge es gegeben hat
					if (isAlarmCycle % 100 == 0) {
						Log.e(TAG, "AlarmCount " + String.valueOf(isAlarm));
						// Ist die Summe h�her als 50 wird ein Alarm ausgegeben
						if (isAlarm > 50) {
							Log.e(TAG, "EARTHQUAKE!");

							// Hier m�sste man den Alarm ausl�sen
						}
						isAlarmCycle = 0;
						isAlarm = 0;
					}
					isAlarmCycle++;

					// Log.d(TAG, "Acceleration Broadcast received " +
					// String.valueOf(abs));
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
