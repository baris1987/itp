package com.th.nuernberg.itp.webservice;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.th.nuernberg.itp.webservice.interfaces.IConfiguration;
import com.th.nuernberg.itp.webservice.interfaces.IDatabaseConfiguration;

public class Database {

	private Connection connection;
	
	public static Database createInstanceByConfiguration(IConfiguration config) throws IOException, ClassNotFoundException, SQLException {
		IDatabaseConfiguration databaseConfig = new DatabaseConfiguration();
		databaseConfig.setDatabase(config.get("Database.Name"));
		databaseConfig.setPassword(config.get("Database.Password"));
		databaseConfig.setUsername(config.get("Database.Username"));
		
		return new Database(databaseConfig);
	}
	
    public Database(IDatabaseConfiguration config) throws ClassNotFoundException, SQLException, IOException {
        Class.forName("org.h2.Driver");  
        this.connection = DriverManager.getConnection("jdbc:h2:"+config.getDatabase(), config.getUsername(), config.getPassword()); 
    }
    
    public void close() throws SQLException {
    	this.connection.close();
    }
    
    public int execute(String command) throws SQLException {
    	Statement statement = this.connection.createStatement();
    	int results = statement.executeUpdate(command);
    	statement.close();
    	return results;
    }
    
    public ResultSet get(String command) throws SQLException {
    	Statement statement = this.connection.createStatement();
    	ResultSet result = statement.executeQuery(command);
    	statement.close();
    	return result;
    }
}