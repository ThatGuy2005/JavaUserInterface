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
                CREATE TABLE IF NOT EXISTS news (
                    title TEXT NOT NULL,
                    description TEXT,
                    source TEXT NOT NULL,
                    author TEXT,
                    publisher TEXT,
                    publish_date TIMESTAMP,
                    url TEXT NOT NULL,
                    creation_date TIMESTAMP NOT NULL,
                    last_modified_date TIMESTAMP NOT NULL,
                    language TEXT NOT NULL,
                    cover_image TEXT,
                    PRIMARY KEY (source, url)
                )""");
        }
    }

    // ---------- Create ----------

    public void insertNews(News news) throws SQLException {
        // Fails with an SQLException if (source, url) already exists
        try (Connection c = DriverManager.getConnection(dbUrl);
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO news (" + COLUMNS + ") VALUES (?,?,?,?,?,?,?,?,?,?,?)")) {
            bind(ps, news);
            ps.executeUpdate();
        }
    }

    public int insertNewNews(List<News> newsList) throws SQLException {
        // Saves only the news that are not in the database yet and
        // returns how many were really added
        String sql = "INSERT OR IGNORE INTO news (" + COLUMNS
                + ") VALUES (?,?,?,?,?,?,?,?,?,?,?)";
        int added = 0;
        try (Connection c = DriverManager.getConnection(dbUrl)) {
            c.setAutoCommit(false);                      // one transaction = much faster
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                for (News n : newsList) {
                    bind(ps, n);
                    added += ps.executeUpdate();
                }
                c.commit();
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        }
        return added;
    }

    // ---------- Read ----------

    public News getNews(String source, String url) throws SQLException {
        String sql = "SELECT * FROM news WHERE source = ? AND url = ?";
        try (Connection c = DriverManager.getConnection(dbUrl);
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, source);
            ps.setString(2, url);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public List<News> findAllNews() throws SQLException {
        // Get all the news, the newest first
        String sql = "SELECT * FROM news ORDER BY publish_date DESC";
        List<News> result = new ArrayList<>();
        try (Connection c = DriverManager.getConnection(dbUrl);
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                result.add(map(rs));
            }
        }
        return result;
    }

    public List<News> search(String text) throws SQLException {
        // News whose title or description contain the text. The escape
        // makes a typed % or _ be searched as a normal character
        String escaped = text.replace("\\", "\\\\")
                .replace("%", "\\%").replace("_", "\\_");
        String sql = "SELECT * FROM news WHERE title LIKE ? ESCAPE '\\' "
                + "OR description LIKE ? ESCAPE '\\' ORDER BY publish_date DESC";
        List<News> results = new ArrayList<>();
        try (Connection c = DriverManager.getConnection(dbUrl);
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, "%" + escaped + "%");
            ps.setString(2, "%" + escaped + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(map(rs));
                }
            }
        }
        return results;
    }

    // ---------- Update ----------

    public boolean updateNews(String oldSource, String oldUrl, News news)
            throws SQLException {
        // The key attributes can be edited too, so the old key is given
        // separately from the new values
        String sql = "UPDATE news SET title=?, description=?, source=?, "
                + "author=?, publisher=?, publish_date=?, url=?, "
                + "last_modified_date=?, language=?, cover_image=? "
                + "WHERE source=? AND url=?";
        try (Connection c = DriverManager.getConnection(dbUrl);
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, news.getTitle());
            ps.setString(2, news.getDescription());
            ps.setString(3, news.getSource());
            ps.setString(4, news.getAuthor());
            ps.setString(5, news.getPublisher());
            ps.setTimestamp(6, news.getPublishDate());
            ps.setString(7, news.getUrl());
            ps.setTimestamp(8, new Timestamp(System.currentTimeMillis()));
            ps.setString(9, news.getLanguage());
            ps.setString(10, news.getImagePath());
            ps.setString(11, oldSource);
            ps.setString(12, oldUrl);
            return ps.executeUpdate() > 0;
        }
    }

    // ---------- Delete ----------

    public boolean deleteNews(String source, String url) throws SQLException {
        try (Connection c = DriverManager.getConnection(dbUrl);
             PreparedStatement ps = c.prepareStatement(
                     "DELETE FROM news WHERE source = ? AND url = ?")) {
            ps.setString(1, source);
            ps.setString(2, url);
            return ps.executeUpdate() > 0;
        }
    }

    public void clearAll() throws SQLException {
        try (Connection c = DriverManager.getConnection(dbUrl);
             Statement st = c.createStatement()) {
            st.executeUpdate("DELETE FROM news");
        }
    }

    // ---------- Helpers ----------

    private static void bind(PreparedStatement ps, News n) throws SQLException {
        ps.setString(1, n.getTitle());
        ps.setString(2, n.getDescription());
        ps.setString(3, n.getSource());
        ps.setString(4, n.getAuthor());
        ps.setString(5, n.getPublisher());
        ps.setTimestamp(6, n.getPublishDate());
        ps.setString(7, n.getUrl());
        ps.setTimestamp(8, n.getCreationDate());
        ps.setTimestamp(9, n.getLastModifiedDate());
        ps.setString(10, n.getLanguage());
        ps.setString(11, n.getImagePath());
    }

    private static News map(ResultSet rs) throws SQLException {
        return new News(
                rs.getString("title"),
                rs.getString("author"),
                rs.getString("publisher"),
                rs.getString("description"),
                rs.getTimestamp("publish_date"),
                rs.getString("source"),
                rs.getString("url"),
                rs.getTimestamp("creation_date"),
                rs.getTimestamp("last_modified_date"),
                rs.getString("language"),
                rs.getString("cover_image"));
    }

    private static final String COLUMNS = "title, description, source, author, "
            + "publisher, publish_date, url, creation_date, "
            + "last_modified_date, language, cover_image";
    
    private final String dbUrl;
}
