/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Repositories;

import InnerComponents.News;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class NewsRepository {
    public NewsRepository(String dbPath) throws SQLException {
        this.dbUrl = "jdbc:sqlite:" + dbPath;
        createSchema();
    }

    private void createSchema() throws SQLException {
        // If we don't have the News table, then create it
        try (Connection c = DriverManager.getConnection(dbUrl);
             Statement st = c.createStatement()) {
            st.execute("""
                CREATE TABLE IF NOT EXISTS item (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    type TEXT NOT NULL CHECK (type IN ('NEWS','EVENT')),
                    title TEXT NOT NULL CHECK (length(trim(title)) > 0),
                    language TEXT NOT NULL CHECK (length(language) = 2),
                    author TEXT, publisher TEXT, description TEXT, source TEXT,
                    url TEXT UNIQUE, image_path TEXT,
                    publish_time INTEGER, creation_date INTEGER, update_date INTEGER,
                    event_date INTEGER, event_location TEXT,
                    fetched_at INTEGER NOT NULL DEFAULT (strftime('%s','now')),
                    CHECK ((type='EVENT' AND event_date IS NOT NULL)
                        OR (type='NEWS' AND event_date IS NULL AND event_location IS NULL))
                )""");
            st.execute("CREATE INDEX IF NOT EXISTS idx_item_publish_time ON item (publish_time DESC)");
        }
    }

    public void saveAll(List<News> newsList) throws SQLException {
        // Save a list of news
        String sql = """
            INSERT INTO item (type, title, language, author, publisher, description,
                              source, url, image_path, publish_time, creation_date, update_date)
            VALUES ('NEWS', ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT(url) DO UPDATE SET
                title = excluded.title,
                description = excluded.description,
                update_date = excluded.update_date,
                image_path = excluded.image_path,
                fetched_at = strftime('%s','now')
            """;
        try (Connection c = DriverManager.getConnection(dbUrl)) {
            c.setAutoCommit(false);                      // one transaction = much faster
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                for (News n : newsList) {
                    ps.setString(1, n.getTitle());
                    ps.setString(2, n.getLanguage());
                    ps.setString(3, n.getAuthor());
                    ps.setString(4, n.getPublisher());
                    ps.setString(5, n.getDescription());
                    ps.setString(6, n.getSource());
                    ps.setString(7, n.getUrl());
                    ps.setString(8, n.getImagePath());
                    setInstant(ps, 9,n.getPublishTime());
                    setInstant(ps, 10, n.getCreationDate());
                    setInstant(ps, 11, n.getUpdateDate());
                    ps.addBatch();
                }
                ps.executeBatch();
                c.commit();
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        }
    }

    public List<News> findAllNews() throws SQLException {
        // Get all the news
        String sql = "SELECT * FROM item WHERE type = "
                + "'NEWS' ORDER BY publish_time DESC";
        List<News> result = new ArrayList<>();
        try (Connection c = DriverManager.getConnection(dbUrl);
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                result.add(new News(
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getString("publisher"),
                        rs.getString("description"),
                        getInstant(rs, "publish_time"),
                        rs.getString("source"),
                        rs.getString("url"),
                        getInstant(rs, "creation_date"),
                        getInstant(rs, "update_date"),
                        rs.getString("language"),
                        rs.getString("image_path")));
            }
        }
        return result;
    }
    
    public List<News> similarTitle(String title) {
        // Get's the news which have a title similar to a given string
        String sql = "SELECT * FROM item WHERE title LIKE ?";
        List<News> results = new ArrayList<News>();
        try(Connection con = DriverManager.getConnection(dbUrl);) {
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, "%" + title + "%");
            ResultSet rs = stmt.executeQuery();
            while(rs.next()) {
                results.add(new News(
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getString("publisher"),
                        rs.getString("description"),
                        getInstant(rs, "publish_time"),
                        rs.getString("source"),
                        rs.getString("url"),
                        getInstant(rs, "creation_date"),
                        getInstant(rs, "update_date"),
                        rs.getString("language"),
                        rs.getString("image_path")));
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return results;
        }
        return results;
    }

    private static void setInstant(PreparedStatement ps, int idx, Instant i)
            throws SQLException {
        // If the Instant is null then set the parameter at index idx to SQL
        // NULL
        if (i == null) ps.setNull(idx, Types.INTEGER);
        // Otherwise just convert to Long and save the seconds
        else ps.setLong(idx, i.getEpochSecond());
    }

    private static Instant getInstant(ResultSet rs, String col)
            throws SQLException {
        long v = rs.getLong(col);
        // Returns null or the epochs in seconds
        return rs.wasNull() ? null : Instant.ofEpochSecond(v);
    }
    
    private final String dbUrl;
}
