package com.th.nuernberg.itp.webservice;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
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
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return config;
	}
	
	public IDatabase createDatabaseInstance(IConfiguration config) {
		IDatabaseConfiguration databaseConfig = DatabaseConfiguration.createInstance(config);
		
		IDatabase database = new Database();
		try {
			database.Initalize(databaseConfig);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
