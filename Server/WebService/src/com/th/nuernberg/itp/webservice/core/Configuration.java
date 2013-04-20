package com.th.nuernberg.itp.webservice.core;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class Configuration {
	
	private static Properties properties;
	
	public static void load(String file) throws IOException {
		Configuration.properties = new Properties();
		properties.load(new FileReader(file));
	}
	
	public static String get(String key) {
		return Configuration.properties.getProperty(key);
	}
}
