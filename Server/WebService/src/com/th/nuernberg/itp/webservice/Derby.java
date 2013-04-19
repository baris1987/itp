package com.th.nuernberg.itp.webservice;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class Derby {
    public static void main(String[] a) throws ClassNotFoundException, SQLException {
        Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
        Properties properties = new Properties();
        properties.put("user", "itp");
        properties.put("password", "itp2013");
        Connection connection = DriverManager.getConnection("jdbc:derby:./database;create=true", properties);
        connection.close();
    }
}