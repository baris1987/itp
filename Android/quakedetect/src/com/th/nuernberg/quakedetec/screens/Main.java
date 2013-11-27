package com.th.nuernberg.quakedetec.screens;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Locale;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.th.nuernberg.quakedetec.R;
import com.th.nuernberg.quakedetec.location.Localizer;
import com.th.nuernberg.quakedetec.service.BackgroundService;

public class Main extends FragmentActivity implements
		ActionBar.TabListener {
	
	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	private ViewPager mViewPager;
	private DeviceMap deviceMap;
	private Info info;
	
	private static boolean appIsVisible = false;
	private static Fragment currentFragment;
	
	private static Context context;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		context = this;
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		
		super.onCreate(savedInstanceState);
		
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		this.setTheme(Integer.parseInt(sharedPreferences.getString("application_theme", "2131492866")));
		
		setContentView(R.layout.main);
		
		Intent service = new Intent(this,BackgroundService.class);
		this.startService(service);
		
		// Set up the action bar.
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		
		
		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						actionBar.setSelectedNavigationItem(position);
					}
				});
		
		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
			// Create a tab with text corresponding to the page title defined by
			// the adapter. Also specify this Activity object, which implements
			// the TabListener interface, as the callback (listener) for when
			// this tab is selected.
			actionBar.addTab(actionBar.newTab()
					.setText(mSectionsPagerAdapter.getPageTitle(i))
					.setTabListener(this));
		}
		
		
		if(DeviceMap.getDeviceMap() != null)
			deviceMap = DeviceMap.getDeviceMap();
		else
		{
			deviceMap = new DeviceMap();
			DeviceMap.setDeviceMap(deviceMap);
		}
		if(Info.getInfo() != null)
			info = Info.getInfo();
		else
		{
			info = new Info();
			Info.setInfoActivity(info);
		}
		currentFragment = info;
		
		appIsVisible = true;
		fetchDeviceListFromServer();
	}

	@Override
	protected void onStart() {
	    super.onStart();
	    appIsVisible = true;
	    
	    if(BackgroundService.getBackgroundService() != null)
	    	BackgroundService.getBackgroundService().startLocationUpdateTimerOrChangeIfNeeded();
	    
	    // schreibt in die statische Variable currentFragment das Fragment, 
	    // welches gerade angezeigt wird
	    if(currentFragment.equals(info))
	    	currentFragment = info;
	    else if(currentFragment.equals(deviceMap))
	    	currentFragment = deviceMap;
	    
	    // nicht besonders clean, aber momentan leider noch notwendig,
	    // da bei manchen Starts der App die onStarts der verschiedenen Klassen zeitlich varieren
	    if(Localizer.getLocalizer() != null)
	    	Localizer.getLocalizer().fireNotificationIfAllProvidersDisabled();
	}
	
	@Override
	protected void onStop() {
	    super.onStop();
	    appIsVisible = false;
	    
	    if(BackgroundService.getBackgroundService() != null)
	    	BackgroundService.getBackgroundService().startLocationUpdateTimerOrChangeIfNeeded();
	    
	    // Nochmal testen, ob benštigt
	    if(Localizer.getLocalizer() != null)
	    	Localizer.getLocalizer().fireNotificationIfAllProvidersDisabled();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main, menu);
        return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) { 
	    // Handle item selection
	    switch (item.getItemId()) {
	        case R.id.action_settings:
	        	Intent intent = new Intent();
	            intent.setClass(Main.this, Settings.class);
	            startActivityForResult(intent, 100); // intent SettingsActivity, requestCode 100
	            
	            return true;																	 
	        default:																			 
	            return super.onOptionsItemSelected(item);										
	    }																						 
	}																							
	
	@Override
	public void onTabSelected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		mViewPager.setCurrentItem(tab.getPosition());
		
		// Beim Swipe durch die App, wird das neu angezeigte Fragment
		// in currentFragment geschrieben
		if(tab.getPosition() == 0)
			currentFragment = info;
		else if(tab.getPosition() == 1)
		{
			currentFragment = deviceMap;
			if(!deviceMap.isInitialized())
			{
				deviceMap.updateCameraToLastKnownLocation(12);
			}
			
			Looper myLooper = Looper.getMainLooper();
			final Handler myHandler = new Handler(myLooper);
		    myHandler.postDelayed(new Runnable() {
		         public void run() 
		         {
		        	 if(DeviceMap.getDeviceMap().getLastKnownLocation() == null)
		        	 {
				       	 Toast.makeText(context, context.getResources().getString(R.string.toast_noLocation), Toast.LENGTH_LONG).show();
		        	 }
		         }
		    }, 2000);
		}
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}
	
	public static boolean appIsVisible()
	{
		return appIsVisible;
	}
	
	public static Fragment getCurrentFragment()
	{
		return currentFragment;
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {
		
		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}
		
		@Override
		public int getItemPosition(Object object) {
		    return POSITION_NONE;
		}
		
		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			// Return a DummySectionFragment (defined as a static inner class
			// below) with the page number as its lone argument.
			Fragment fragment;
			if(position == 0)
				fragment = info;
			
			else if(position == 1)
				fragment = deviceMap;
			
			else
			{
				fragment = info;
			}
			return fragment;
		}

		@Override
		public int getCount() {
			// Show 2s total pages.
			return 2;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.sec_1).toUpperCase(l);
			case 1:
				return getString(R.string.sec_2).toUpperCase(l);
			}
			return null;
		}
	}
	
	private void fetchDeviceListFromServer()
	{
		if(appIsVisible)
		{
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Main.context);
						
						String serverUrl = prefs.getString("server_url", "");
						String serverPort = prefs.getString("server_port", "8088");
					
						String requestUrl = String.format("http://%s:%s/itp/device/list/", serverUrl, serverPort);
						
						HttpClient client = new DefaultHttpClient();
						HttpGet request = new HttpGet();
						request.setURI(new URI(requestUrl));
						HttpResponse response = client.execute(request);
						BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
						StringBuffer sb = new StringBuffer("");
						String l = "";
						while ((l = in.readLine()) != null) {
							sb.append(l);
						}
						
						in.close();
						String data = sb.toString();
						
						JSONObject completeDataJSON = (JSONObject) new JSONTokener(data).nextValue();
						String completeData = completeDataJSON.getString("data");
						
						JSONArray jsonArray = (JSONArray) new JSONTokener(completeData).nextValue();
						
						ArrayList<JSONObject> deviceJSONObjects = new ArrayList<JSONObject>(jsonArray.length());
						
						for(int i = 0; i < jsonArray.length(); i++)
						{
							JSONObject device = jsonArray.getJSONObject(i);
							deviceJSONObjects.add(device);
						}
						info.setConnectedDevicesTextView(deviceJSONObjects.size());
						deviceMap.addDeviceMarkerToMap(deviceJSONObjects);
					} catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			}).start();
		}
	}
}
