package com.th.nuernberg.itp.webservice.utils;
import com.th.nuernberg.itp.webservice.interfaces.IDatabaseConfiguration;

public class DatabaseConfiguration implements IDatabaseConfiguration {
	private String database;
	private String username;
	private String password;
	
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getDatabase() {
		return database;
	}
	public void setDatabase(String database) {
		this.database = database;
	}
}
