package com.th.nuernberg.quakedetec.service;

import com.th.nuernberg.quakedetec.R;
import com.th.nuernberg.quakedetec.screens.Info;
import com.th.nuernberg.quakedetec.screens.Main;

import android.app.AlertDialog;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;

public class NotificationsService extends Application{
		
		private static boolean soundActivated 		= true;
		private static boolean vibrationActivated	= true;
		private static boolean ledActivated			= true;
		private static AlertDialog alertDialog;
			
		private NotificationsService(Context context) {}
		
		public static void sendLocationProviderDisabledNotification(Context context)
		{
			Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, 0);
						
			Notification notification  = new Notification.Builder(context)
			        .setContentTitle("QuakeDetec")
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
			
			// AlertDialog, wenn App geöffnet ist und noch kein Alertdialog offen ist
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
					.setPositiveButton("Öffnen",new DialogInterface.OnClickListener() {
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
	
		
		 // Put the message into a notification and post it.
	    // This is just one simple example of what you might choose to do with
	    // a GCM message.
	    public static void sendAlarmNotification(Context context, String msg) {
	    	NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

	    	PendingIntent contentIntent = PendingIntent.getActivity(context, 0, new Intent(context, Info.class), 0);

	        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
	        .setSmallIcon(R.drawable.icon)
	        .setContentTitle("QuakeDetec")
	        .setStyle(new NotificationCompat.BigTextStyle()
	        .bigText(msg))
	        .setContentText(msg);
	        
	        if(vibrationActivated)
	        	mBuilder.setVibrate(new long[]{0,300,100,300,100,300,100,300,100,300,100});
        	if(soundActivated)
	        	mBuilder.setSound(Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.alarm));
	        
	        mBuilder.setContentIntent(contentIntent);
	        mNotificationManager.notify(1, mBuilder.build());
	    }
	    
		// entfernt AlertDialog und Notification für deaktivierte LocationProvider
		public static void dismissLocationProviderDisabledNotification(Context context)
		{
			((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(0);
			if(alertDialog != null)
				alertDialog.cancel();
		}
		
		// setzt NotificationSettings 
		// (evtl. nochmal umschreiben, da ausschließliche Verwendung dieser Methode nicht zur Speicherung der SharedPrefs führt
		// dadurch Missverständnisse möglich)
		public static void setNotificationSettings(boolean sound, boolean vibration, boolean led)
		{
			soundActivated 		= sound;
			vibrationActivated 	= vibration;
			ledActivated		= led;
		}
}
