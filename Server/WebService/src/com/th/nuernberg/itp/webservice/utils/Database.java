package com.th.nuernberg.itp.webservice.utils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import com.th.nuernberg.itp.webservice.interfaces.IDatabaseConfiguration;

public class Database {

	private Connection connection;
	
    public Database(IDatabaseConfiguration config) throws ClassNotFoundException, SQLException, IOException {
        Class.forName("org.h2.Driver");  
        this.connection = DriverManager.getConnection("jdbc:h2:"+config.getDatabase(), config.getUsername(), config.getPassword()); 
    }
    
    public void close() throws SQLException {
    	this.connection.close();
    }
    
    // NUR FÜR TESTZWECKE
    public static void main(String[] args) throws ClassNotFoundException, SQLException, IOException {
    	
    	IDatabaseConfiguration dc = new DatabaseConfiguration();
    	dc.setDatabase("./database/itp");
    	dc.setPassword("itp2013");
    	dc.setUsername("itp");
    	
    	Database d = new Database(dc);
    	System.out.println("DONE.");
    	d.close();
    }
}