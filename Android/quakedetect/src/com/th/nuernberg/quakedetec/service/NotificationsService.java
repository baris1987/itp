package com.th.nuernberg.quakedetec.service;

import com.th.nuernberg.quakedetec.R;
import com.th.nuernberg.quakedetec.screens.Main;

import android.app.AlertDialog;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.sax.StartElementListener;

public class NotificationsService extends Application{
		
		private static boolean soundActivated 		= true;
		private static boolean vibrationActivated	= true;
		private static boolean ledActivated			= true;
		private static AlertDialog alertDialog;
			
		public NotificationsService(Context context)
		{
			SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
			soundActivated 		= sharedPrefs.getBoolean("notification_sound", true);	
			vibrationActivated 	= sharedPrefs.getBoolean("notification_vibrate", true);	
			vibrationActivated	= sharedPrefs.getBoolean("notification_led", true);	
		}
		
		public static void sendLocationProviderDisabledNotification(Context context)
		{
			Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, 0);
						
			Notification notification  = new Notification.Builder(context)
			        .setContentTitle("Standortbestimmung deaktiviert")
			        .setContentText("Bitte Standortbestimmung aktivieren")
			        .setSmallIcon(R.drawable.icon)
			        .setContentIntent(pIntent)
			        .setAutoCancel(false)
			        .setOngoing(true)
			        .setDefaults(0)
			        .setTicker("Standortzugriff deaktiviert")
			        .setOnlyAlertOnce(true)
			        .build();
			
			if(soundActivated)
				notification.defaults |= Notification.DEFAULT_SOUND;
			if(vibrationActivated)
				notification.defaults |= Notification.DEFAULT_VIBRATE;
			if(ledActivated)
				notification.defaults |= Notification.DEFAULT_LIGHTS;
			
			NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
	
			notificationManager.notify(0, notification); 	
			
			boolean isAlertShown;
			
			if(alertDialog == null)
				isAlertShown = false;
			else 
				isAlertShown = alertDialog.isShowing();
			
			if(Main.appIsVisible() && Main.getCurrentFragment() != null && isAlertShown == false)
			{
				if(Main.getCurrentFragment().getActivity() != null)
				{
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Main.getCurrentFragment().getActivity());
			
				// set title
				alertDialogBuilder.setTitle("Standortbestimmung deaktiviert");
				
				// set dialog message
				alertDialogBuilder
					.setMessage("Bitte aktivieren Sie die Standortbestimmung")
					.setCancelable(false)
					.setPositiveButton("…ffnen",new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,int id) {
							// if this button is clicked, close
							// current activity
							Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
							Main.getCurrentFragment().startActivity(intent);
							dialog.cancel();
						}
					  });
		 						
					// create alert dialog
					alertDialog = alertDialogBuilder.create();
					// show it
					alertDialog.show();
				}
			}
		}
	
		
		public static void dismissLocationProviderDisabledNotification(Context context)
		{
			((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(0);
			if(alertDialog != null)
				alertDialog.cancel();
		}
		
		public static void setNotificationSettings(boolean sound, boolean vibration, boolean led)
		{
			soundActivated 		= sound;
			vibrationActivated 	= vibration;
			ledActivated		= led;
		}
}
