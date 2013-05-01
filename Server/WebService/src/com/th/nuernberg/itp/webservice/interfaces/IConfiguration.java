package com.th.nuernberg.itp.webservice.interfaces;

import java.io.FileNotFoundException;
import java.io.IOException;

public interface IConfiguration {
	public void load(String source) throws FileNotFoundException, IOException;
	public String get(String key);
}
