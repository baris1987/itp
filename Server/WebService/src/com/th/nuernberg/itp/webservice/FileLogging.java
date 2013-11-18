package com.th.nuernberg.itp.webservice;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.th.nuernberg.itp.webservice.interfaces.ILogging;

public class FileLogging implements ILogging {

	private boolean enabled;
	private String logFile;
	
	public FileLogging(String logFile) {
		this.enabled = true;
		this.logFile = logFile;
	}
	
	private boolean writeFile(String message) {
		try {
			
			File file = new File(this.logFile);
	
			if(!file.exists()){
					file.createNewFile();
			}
	
			FileWriter fileWritter = new FileWriter(file.getName(), true);
	        BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
	        bufferWritter.write("\n" + message);
	        bufferWritter.close();
	        fileWritter.close();
		} catch (IOException e) {
			return false;
		}
		
		return true;
	}
	
	private void log(String message) {
		if (this.enabled) {
			String datetime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
			this.writeFile(datetime + " | " + message);
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
