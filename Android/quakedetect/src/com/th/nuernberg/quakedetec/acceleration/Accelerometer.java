package com.th.nuernberg.quakedetec.acceleration;import android.app.Service;import android.content.Context;import android.content.Intent;import android.content.SharedPreferences;import android.hardware.Sensor;import android.hardware.SensorEvent;import android.hardware.SensorEventListener;import android.hardware.SensorManager;import android.os.IBinder;import android.os.Parcelable;import android.preference.PreferenceManager;import android.util.Log;public class Accelerometer extends Service {	private static final String TAG = "Accelerometer";	public static final String ACCEL_SAMPLE = "com.th.nuernberg.quakedetec.ACCELERATION_SAMPLE";	public static final String ACCEL_SAMPLE_KEY = "ACCELERATION_SAMPLE";	private static Accelerometer accelerometer;	private SensorManager sensorManager;		@Override	public void onCreate() {		super.onCreate();		Log.d(TAG, "Create Accelerometer");		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);				startAccelerometer();				accelerometer = this;	}	@Override	public void onDestroy() {		super.onDestroy();		sensorManager.unregisterListener(this.sensorEventListener);	}	protected void broadcastSingleSample(AccelSample sample) {		Intent intent = new Intent(ACCEL_SAMPLE);		intent.putExtra(ACCEL_SAMPLE_KEY, (Parcelable) sample);		sendBroadcast(intent);	}	private final SensorEventListener sensorEventListener = new SensorEventListener() {		public void onAccuracyChanged(Sensor sensor, int accuracy) {		}				public void onSensorChanged(SensorEvent e) {			float x = e.values[0]; 			float y = e.values[1]; 			float z = e.values[2]; 			long t = System.currentTimeMillis();			AccelSample accelSample = new AccelSample(x, y, z, t);			broadcastSingleSample(accelSample);		}	};	@Override	public IBinder onBind(Intent intent) {		Log.d(TAG, "onBind");		return null;	}	public static Accelerometer getAccelerometer()	{		return accelerometer;	}		public void setNewSensorRate(int rate)	{		sensorManager.unregisterListener(this.sensorEventListener);		Sensor accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);		sensorManager.registerListener(sensorEventListener, accelerometerSensor, rate);			}		public void stopAccelerometer()	{		sensorManager.unregisterListener(this.sensorEventListener);	}		public void startAccelerometer()	{		Sensor accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);		int accelRate = 1;		sensorManager.registerListener(sensorEventListener, accelerometerSensor, accelRate);	}}