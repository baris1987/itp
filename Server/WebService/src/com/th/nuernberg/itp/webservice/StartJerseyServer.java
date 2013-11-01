package com.th.nuernberg.itp.webservice;

import java.io.IOException;
import java.sql.SQLException;

import com.sun.jersey.api.container.httpserver.HttpServerFactory;
import com.sun.net.httpserver.HttpServer;
import com.th.nuernberg.itp.webservice.interfaces.IConfiguration;
import com.th.nuernberg.itp.webservice.interfaces.ILogging;

public class StartJerseyServer {
	public static void main(String[] args) throws IllegalArgumentException, IOException, ClassNotFoundException, SQLException {
		
		ILogging console = new Logging();
		
		IConfiguration config = new FileConfiguration();
		config.load(Constants.Configuration);

		String host = config.get("WebService.Host");
		String path = config.get("WebService.Path");
		String port = config.get("WebService.Port");
		String url = "http://"+host+":"+port+"/"+path;
		
		HttpServer server = HttpServerFactory.create(url);
		server.start();
		
		
		console.write("SERVER", "Jersey WebServer started ...");
		console.write("SERVER", "Running on "+url);
		//server.stop(0);
	}

}