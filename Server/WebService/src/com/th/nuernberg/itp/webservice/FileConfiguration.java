package com.th.nuernberg.itp.webservice;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import com.th.nuernberg.itp.webservice.interfaces.IConfiguration;

public class FileConfiguration implements IConfiguration {
	
	private Properties properties;
	
	public void load(String source) throws FileNotFoundException, IOException {
		this.properties = new Properties();
		this.properties.load(FileConfiguration.class.getResourceAsStream(source));
	}
	
	public String get(String key) {
		return this.properties.getProperty(key);
	}
}
