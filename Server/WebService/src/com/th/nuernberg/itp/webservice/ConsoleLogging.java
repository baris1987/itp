package com.th.nuernberg.itp.webservice;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.th.nuernberg.itp.webservice.interfaces.ILogging;

public class ConsoleLogging implements ILogging {

	private boolean enabled;
	
	public ConsoleLogging() {
		this.enabled = true;
	}
	
	private void log(String message) {
		if (this.enabled) {
			String datetime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
			System.out.println(datetime + " | " + message);
		}
	}
	
	public void write(String type, Object... messages) {
		StringBuilder message = new StringBuilder();
		
	    for(Object m : messages){
	    	message.append(" | ").append(m.toString());
		}

		String build = type.toUpperCase() + message.toString();

		this.log(build);
	}

	public void enable(boolean enabled) {
		this.enabled = enabled;
	}
}
