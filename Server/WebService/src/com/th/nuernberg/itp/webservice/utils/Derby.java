package com.th.nuernberg.itp.webservice.utils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import com.th.nuernberg.itp.webservice.core.*;

public class Derby {
	// Test Connection Method
    public static void ConnectionTest() throws ClassNotFoundException, SQLException, IOException {
    	
    	Configuration.load(Constants.Configuration);
    	
        Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
        Properties properties = new Properties();
        properties.put("user", Configuration.get("Database.Username"));
        properties.put("password", Configuration.get("Database.Password"));
        
        Connection connection = DriverManager.getConnection("jdbc:derby:./database;create=true", properties);
        connection.close();
    }
}