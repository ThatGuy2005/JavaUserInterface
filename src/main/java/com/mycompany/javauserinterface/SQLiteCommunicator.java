
package com.mycompany.javauserinterface;

import java.sql.*;

public class SQLiteCommunicator {
    private SQLiteCommunicator(String serverURL) throws SQLException {
        con = DriverManager.getConnection(serverURL);
        built = true;
    }
    public SQLiteCommunicator getInstance(String url) throws SQLException {
        if(built) {
            return this.instance;
        } else {
            this.instance = new SQLiteCommunicator(url);
            return this.instance;
        }
    }
    private SQLiteCommunicator instance; 
    private Connection con;
    private static boolean built = false;
}
