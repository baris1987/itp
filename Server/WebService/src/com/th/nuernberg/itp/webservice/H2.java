package com.th.nuernberg.itp.webservice;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class H2 {
    public static void main(String[] a) throws ClassNotFoundException, SQLException {
        	  Class.forName("org.h2.Driver");
              Connection conn = DriverManager.getConnection("jdbc:h2:./db/webservice", "sa", "");
              conn.close();
    }
}