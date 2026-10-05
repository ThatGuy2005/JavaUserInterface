
package com.mycompany.javauserinterface;

import java.lang.ProcessBuilder.Redirect.Type;
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
    // How can I connect it with RSS?
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
                String imagePath = res.getString("imagePath");
                Instant creationDate = 
                        Instant.ofEpochSecond(res.getInt("creationDate"));
                Instant updateDate = 
                        Instant.ofEpochSecond(res.getInt("updateDate"));
                String language = res.getString("language");
                news.add(new News(title, author, publisher,
                        description, publishTime, source, url, creationDate,
                        updateDate, language, imagePath));
            }
            return news;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return null;
        }
    }
    
    // I chose that the title and publication date will serve as
    // a primery key in both News and Events tables.
    // Use a trigger to update the updateTime in the database.
    public void updateAttributeIn(String attributeName,
            String table, Object value,
            String title, Instant publishTime) {
        
        String query = "UPDATE " + table + " SET " + attributeName + " = ?"
                + " WHERE title = ? AND date = ?";
        
        try (PreparedStatement stmt = con.prepareStatement(query)) {
            if(value instanceof String) {
                stmt.setString(1,(String) value);
            } else if (value instanceof Long) {
                stmt.setLong(1,(Long) value);
            } else {
                System.out.println("Invalid type chosen, update cancelled!");
                return;
            }
            stmt.setString(2, title);
            // Use long in the database
            stmt.setLong(3, publishTime.getEpochSecond());
            stmt.executeQuery();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }
    
    public void deleteEntry(
            String table,
            String title, Instant publishTime) {
        
        String query = "DELETE FROM " + table
                + " WHERE title = ? AND date = ?";
        
        try (PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setString(1, title);
            // Use long in the database
            stmt.setLong(2, publishTime.getEpochSecond());
            stmt.executeQuery();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }
    
    private SQLiteCommunicator instance; 
    private Connection con;
    private static boolean built = false;
}
