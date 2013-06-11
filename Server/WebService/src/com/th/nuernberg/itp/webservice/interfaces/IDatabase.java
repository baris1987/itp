package com.th.nuernberg.itp.webservice.interfaces;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

public interface IDatabase {
    public void Initalize(IDatabaseConfiguration config) throws ClassNotFoundException, SQLException, IOException;
    public void close() throws SQLException;
    public int execute(String command) throws SQLException;
    public ResultSet get(String command) throws SQLException;
}
