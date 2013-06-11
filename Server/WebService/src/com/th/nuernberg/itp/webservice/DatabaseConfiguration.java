package com.th.nuernberg.itp.webservice;
import com.th.nuernberg.itp.webservice.interfaces.IConfiguration;
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
	
	public static IDatabaseConfiguration createInstance(IConfiguration config) {
		IDatabaseConfiguration databaseConfig = new DatabaseConfiguration();
		databaseConfig.setDatabase(config.get("Database.Name"));
		databaseConfig.setPassword(config.get("Database.Password"));
		databaseConfig.setUsername(config.get("Database.Username"));
		return databaseConfig;
	}
}
