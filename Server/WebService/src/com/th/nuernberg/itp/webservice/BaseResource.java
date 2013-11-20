package com.th.nuernberg.itp.webservice;

import java.util.Random;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import com.th.nuernberg.itp.webservice.interfaces.*;

@Produces(MediaType.APPLICATION_JSON)
public abstract class BaseResource implements IWebService {

	public IConfiguration createConfigurationInstance(String file) {
		IConfiguration config = new FileConfiguration();
		try {
			config.load(file);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		} 
		return config;
	}
	
	public IDatabase createDatabaseInstance(IConfiguration config) {
		IDatabaseConfiguration databaseConfig = DatabaseConfiguration.createInstance(config);
		
		IDatabase database = new Database();
		try {
			database.Initalize(databaseConfig);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return database;
	}
	
	@GET
	@Path("ping")
	public String ping() {
		Random rn = new Random();
		return "{ping: " + (rn.nextInt() % 10000) + "}";
	}
}
