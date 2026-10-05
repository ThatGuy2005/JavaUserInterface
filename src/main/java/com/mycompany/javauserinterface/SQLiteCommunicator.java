
package com.mycompany.javauserinterface;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;

// Note that this class is a Singleton, there shouldn't be any need to have more
// objects to communicate with the server side.
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
    
    // Get all the news with a given title and date (this is the primery key)
    public ArrayList<News> getNewsWithTitleAndDate(String title, Instant publishTime) {
        String query = "SELECT * FROM News WHERE title=? AND date=?";
        
        try (PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setString(1, title);
            // Use long in the database
            stmt.setLong(2, publishTime.getEpochSecond());
            ResultSet res = stmt.executeQuery();
            ArrayList<News> news = new ArrayList<News>();
            while(res.next()) {
                String publisher = res.getString("publisher");
                String author = res.getString("author");
                String description = res.getString("description");
                String source = res.getString("source");
                String url = res.getString("url");
                Instant creationDate = 
                        Instant.ofEpochSecond(res.getInt("creationDate"));
                Instant updateDate = 
                        Instant.ofEpochSecond(res.getInt("updateDate"));
                String language = res.getString("language");
                news.add(new News(title, author, publisher,
                        description, publishTime, source, url, creationDate,
                        updateDate,language));
            }
            return news;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return null;
        }
    }
    private SQLiteCommunicator instance; 
    private Connection con;
    private static boolean built = false;
}
