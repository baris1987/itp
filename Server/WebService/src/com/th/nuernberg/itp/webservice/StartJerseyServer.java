package com.th.nuernberg.itp.webservice;

import java.io.IOException;
import com.sun.jersey.api.container.httpserver.HttpServerFactory;
import com.sun.net.httpserver.HttpServer;

// commit in eclipse

public class StartJerseyServer {
	public static void main(String[] args) throws IllegalArgumentException, IOException {
		
		Configuration.load(Constants.Configuration);
		String host = Configuration.get("WebService.Host");
		String path = Configuration.get("WebService.Path");
		String port = Configuration.get("WebService.Port");
		String url = "http://"+host+":"+port+"/"+path;
		
		HttpServer server = HttpServerFactory.create(url);
		server.start();
		
		System.out.println("Jersey WebServer started ...");
		System.out.println("Running on "+url);
		//server.stop(0);
	}

}