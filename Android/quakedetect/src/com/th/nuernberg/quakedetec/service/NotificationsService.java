package com.th.nuernberg.quakedetec.service;

import com.th.nuernberg.quakedetec.R;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.Settings;

public class NotificationsService {
			
		public static void sendLocationProviderDisabledNotification(Context context)
		{
			
			Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, 0);
						
			Notification notification  = new Notification.Builder(context)
			        .setContentTitle("Standortdaten deaktiviert")
			        .setContentText("Bitte Standortdaten aktivieren")
			        .setSmallIcon(R.drawable.icon)
			        .setContentIntent(pIntent)
			        .setAutoCancel(false)
			        .setOngoing(true)
			        .setDefaults(0)
			        .setTicker("Standortzugriff deaktiviert")
			        .setOnlyAlertOnce(true)
			        .build();
			
			SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
						
			boolean soundActivated 		= sharedPrefs.getBoolean("notification_sound", true);
			boolean vibrationActivated 	= sharedPrefs.getBoolean("notification_vibrate", true);
			boolean ledActivated		= sharedPrefs.getBoolean("notification_led", true);
			
			if(soundActivated)
				notification.defaults |= Notification.DEFAULT_SOUND;
			if(vibrationActivated)
				notification.defaults |= Notification.DEFAULT_VIBRATE;
			if(ledActivated)
				notification.defaults |= Notification.DEFAULT_LIGHTS;
			
			NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
	
			notificationManager.notify(0, notification); 	
		}
		
		public static void dismissLocationProviderDisabledNotification(Context context)
		{
			((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(0);
		}
}
