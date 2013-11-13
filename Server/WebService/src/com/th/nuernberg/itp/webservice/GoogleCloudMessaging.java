package com.th.nuernberg.itp.webservice;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import com.th.nuernberg.itp.webservice.interfaces.IMessaging;


/*
 * { "data": {
    "score": "5x1",
    "time": "15:10"
  },
  "registration_ids": ["4", "8", "15", "16", "23", "42"]
}
 * */



public class GoogleCloudMessaging implements IMessaging {
	
	public String send() {
		
		String rawData = "{\"registration_ids\":[\"APA91bGG1nTq4ltHc39IC5SNDO4vhYdn83W0pia7_NvlIh1XEFRyBmi_5rPp4e1Xuol1mfhnu5pKlL-NEVDzAEu-I0e1rOqftfCaREL7EQBeJa0y43u3RP5aWqDXEx0ltqnRzHTXNt8smDiSn2VJLF1ScL-e1M7Z0jLWE5uRga0_spKbi4sL7Zo\"],\"data\":{\"message\":\"Test Notification\"}}";
		 
		try {
			URL url = new URL("https://android.googleapis.com/gcm/send");
			HttpURLConnection http = (HttpURLConnection) url.openConnection();
			http.setDoOutput(true);
			http.setRequestMethod( "POST" );
			http.setRequestProperty( "Content-Type", "application/json");
			http.setRequestProperty( "Authorization", "key=AIzaSyAfSY3J-yW4R2AOdI4UEHpyfFqhSvTFQS8");
			
			OutputStreamWriter writer = new OutputStreamWriter( http.getOutputStream() );
			writer.write( rawData );
			writer.flush();


			BufferedReader reader = new BufferedReader(
			                          new InputStreamReader(http.getInputStream()) );

			StringBuilder result = new StringBuilder();
			for ( String line; (line = reader.readLine()) != null; )
			{
				result.append(line);
			}

			writer.close();
			reader.close();
			
			return result.toString();
			
			
			//OutputStream output = http.getOutputStream();
			//output.write( encodedData.getBytes() );
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		return "Error";
	}
	
}
