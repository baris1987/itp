package com.th.nuernberg.quakedetec.receivers;

import com.th.nuernberg.quakedetec.services.BackgroundService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootCompletedReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		
		Intent backgroundServiceIntent = new Intent(context, BackgroundService.class);
		context.startService(backgroundServiceIntent);
		Log.i("QuakeDetect", "Run background service");
	}

}
