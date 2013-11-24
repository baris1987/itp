package com.th.nuernberg.quakedetec.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import com.th.nuernberg.quakedetec.acceleration.Accelerometer;
import com.th.nuernberg.quakedetec.location.Localizer;
import com.th.nuernberg.quakedetec.service.BackgroundService;

public class ConnectionChangeReceiver extends BroadcastReceiver
{
	private static boolean runningTimer = false;
	
	@Override
	public void onReceive( Context context, Intent intent )
	{
		  ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService( Context.CONNECTIVITY_SERVICE );
		  NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
		  BackgroundService backgroundService = BackgroundService.getBackgroundService();
		  if ( activeNetInfo != null )
		  {
			  	if(activeNetInfo.isConnected() && runningTimer == false)
			  	{
			  		backgroundService.startLocationUpdateTimerOrChangeIfNeeded();
			  		backgroundService.startHeartBeatTimer();
				  	//Accelerometer.getAccelerometer().startAccelerometer();
			  		runningTimer = true;
			  		Localizer.getLocalizer().updateLocation();
			  		Toast.makeText(context, "Active Network Type : " + activeNetInfo.getTypeName() + "\nTimer started", Toast.LENGTH_SHORT ).show();
			  		
			  	}
			  	else if(!activeNetInfo.isConnected())
			  	{
			  		backgroundService.stopLocationUpdateTimer();
			  		backgroundService.stopHeartBeatTimer();
			  		//Accelerometer.getAccelerometer().stopAccelerometer();
			  		runningTimer = false;
			  		Toast.makeText( context, "No Network connected\nTimer stopped", Toast.LENGTH_SHORT ).show();
			  	}
		  }
		  else
		  {
			  	backgroundService.stopLocationUpdateTimer();
			  	backgroundService.stopHeartBeatTimer();
			  	//Accelerometer.getAccelerometer().stopAccelerometer();
			  	runningTimer = false;
			  	Toast.makeText( context, "No Network available\nTimer stopped", Toast.LENGTH_SHORT ).show();
		  }
	}
}