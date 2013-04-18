package com.th.nuernberg.itp.webservice;
import java.io.IOException;
//import javax.swing.JOptionPane;
import com.sun.jersey.api.container.httpserver.HttpServerFactory;
import com.sun.net.httpserver.HttpServer;

public class StartJerseyServer {
	public static void main(String[] args) throws IllegalArgumentException, IOException {
		HttpServer server = HttpServerFactory.create("http://localhost/itp");
		server.start();
		
		System.out.println("WebService started ...");
		//JOptionPane.showMessageDialog(null, "Done.");
		//server.stop(0);
	}

}