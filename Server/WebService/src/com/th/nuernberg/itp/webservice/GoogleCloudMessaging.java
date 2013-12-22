package com.th.nuernberg.itp.webservice;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import com.th.nuernberg.itp.webservice.interfaces.IAndroidDevice;
import com.th.nuernberg.itp.webservice.interfaces.IGoogleCloudMessaging;
import com.th.nuernberg.itp.webservice.interfaces.IGoogleCloudMessagingConfiguration;
import com.th.nuernberg.itp.webservice.interfaces.IGoogleCloudMessagingNotification;

public class GoogleCloudMessaging implements IGoogleCloudMessaging {

	private IGoogleCloudMessagingConfiguration configuration;
	
	public void setMessagingConfiguration(IGoogleCloudMessagingConfiguration configuration) {
		this.configuration = configuration;
	}
	
	public IGoogleCloudMessagingConfiguration getMessagingConfiguration() {
		return this.configuration;
	}
	
	public void send(IGoogleCloudMessagingNotification notification) {
		
		IAndroidDevice[] devices = notification.getAndroidDevices();
		String[] androidDevices = new String[devices.length];
		
		int n = 0;
		for (IAndroidDevice device : devices) {
			androidDevices[n] = device.getIdentifier();
			n++;
		}
		
		String message = notification.getMessage();
		
		StringBuilder rawData = new StringBuilder();
		Json json = new Json();
		//\"APA91bGG1nTq4ltHc39IC5SNDO4vhYdn83W0pia7_NvlIh1XEFRyBmi_5rPp4e1Xuol1mfhnu5pKlL-NEVDzAEu-I0e1rOqftfCaREL7EQBeJa0y43u3RP5aWqDXEx0ltqnRzHTXNt8smDiSn2VJLF1ScL-e1M7Z0jLWE5uRga0_spKbi4sL7Zo\"
		//String rawData = "{\"registration_ids\":[],\"data\":{\"message\":\"Test Notification\"}}";
		 
		String jsonIds = json.build(androidDevices);
		rawData.append("{\"registration_ids\":"+jsonIds+",\"data\":{\"message\":\""+message+"\"}}");
		
		try {
			//
			URL url = new URL(this.configuration.getApiUrl());
			HttpURLConnection http = (HttpURLConnection) url.openConnection();
			http.setDoOutput(true);
			http.setRequestMethod("POST");
			http.setRequestProperty("Content-Type", "application/json");
			http.setRequestProperty("Authorization", "key="+this.configuration.getAuthorizationKey()); //
			
			OutputStreamWriter writer = new OutputStreamWriter(http.getOutputStream());
			writer.write(rawData.toString());
			writer.flush();

			BufferedReader reader = new BufferedReader(new InputStreamReader(http.getInputStream()));
			StringBuilder result = new StringBuilder();
			
			for (String line; (line = reader.readLine()) != null; ) {
				result.append(line);
			}

			writer.close();
			reader.close();
			
			// result.toString();
			
			
			//OutputStream output = http.getOutputStream();
			//output.write( encodedData.getBytes() );
			
		} catch (Exception e) {

		}
	}
	
}
