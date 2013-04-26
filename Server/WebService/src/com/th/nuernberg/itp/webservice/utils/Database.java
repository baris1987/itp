package com.th.nuernberg.itp.webservice.utils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import com.th.nuernberg.itp.webservice.interfaces.IDatabaseConfiguration;

public class Database {

	private Connection connection;
	
    public Database(IDatabaseConfiguration databaseConfiguration) throws ClassNotFoundException, SQLException, IOException {

        Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
        Properties properties = new Properties();
        properties.put("user", databaseConfiguration.getUsername());
        properties.put("password", databaseConfiguration.getPassword());
        
        this.connection = DriverManager.getConnection("jdbc:derby:"+databaseConfiguration.getDatabase()+";create=true", properties);
    }
    
    protected void finalize() throws Throwable
    {
      this.connection.close();
    } 
    
    public void execute() throws SQLException {
    	 Statement statement = this.connection.createStatement(); 
    	 
    	 // NUR TEST
         int count = statement.executeUpdate(
           "CREATE TABLE T_DEVICE (PK_DEVICEID INT, ID VARCHAR(100), CREATEDATE DATE)");

         statement.close();        
    }
}