package com.th.nuernberg.quakedetec.screens;

import com.th.nuernberg.quakedetec.R;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.view.MenuItem;


// Activity for SettingsFragment

public class Settings extends Activity {

	@Override
	 protected void onCreate(Bundle savedInstanceState) {
	  // TODO Auto-generated method stub
	  super.onCreate(savedInstanceState);
	  getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
	  getActionBar().setDisplayHomeAsUpEnabled(true); // adds Backbutton to the ActionBar
	 }
	
	
	// action for backbutton ---> navigates to parentactivity (Main)
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    // Respond to the action bar's Up/Home button
	    case android.R.id.home:
	        finish();
	        return true;
	    }
	    return super.onOptionsItemSelected(item);
	}
	
	public static class SettingsFragment extends PreferenceFragment {
	    @Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        addPreferencesFromResource(R.xml.preferences);
	    }
	}
}


