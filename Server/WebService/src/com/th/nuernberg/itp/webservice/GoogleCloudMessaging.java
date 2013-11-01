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
		
		String rawData = "{\"registration_ids\":[\"APA91bFgHShF18D2GmGaJE_nmNzFWoXuZDnxmuiHInQrjdAupTz6c4Ixi6ZcOmVz3LtOFcngapqYjPkontV6W2e6CTaMZlC5Z3x3HzlnbQROo4ng4Yi67X6FqOBlHPfP2DcKtzKCaIDCcwzekm0FQNy0KQMB4fiBDwnxsONa3sjrHMPDWJRT06w\"],\"data\":{\"message\":\"Test Notification\"}}";
		String encodedData;
		try {
			encodedData = java.net.URLEncoder.encode( rawData, "UTF-8");

			URL url = new URL("https://android.googleapis.com/gcm/send");
			HttpURLConnection http = (HttpURLConnection) url.openConnection();
			http.setDoOutput(true);
			http.setRequestMethod( "POST" );
			http.setRequestProperty( "Content-Type", "application/json");
			http.setRequestProperty( "'Authorization", "key=AIzaSyCQ79pKaeRoWfy-33bcn4oAYZ6ILqWE3H0");
			
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
