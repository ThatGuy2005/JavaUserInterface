
package Services;

import InnerComponents.News;
import RSS.NewsRSSParser;
import RSS.RssCommunicator;
import RSS.RssConnectionChecker;
import RSS.RssReaderJson;
import RSS.RssSource;
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
import javax.swing.JButton;
import Repositories.NewsRepository;
import java.io.IOException;

public class NewsService implements Servicable {
    public NewsService(NewsRepository repo) {
        this.repository = repo;
    }
    @Override
    public RefreshResult refresh(String path) throws IOException {
        // Downloads every RSS from the json and saves only the new news.
        // A source that fails does not stop the others, it is reported
        List<String> errors = new ArrayList<>();
        int added = 0;
        
        for(RssSource source : RssReaderJson.getSources(path)) {
            try {
                List<News> fresh = new ArrayList<>();
                for (News n : RssCommunicator.parseRSS(source,
                        new NewsRSSParser())) {
                    if (repository.getNews(n.getSource(), n.getUrl()) == null) {
                        fresh.add(n);
                    }
                }
                // Only the new news need a cover image download
                List<News> withCover = new ArrayList<>();
                fresh.parallelStream().map(n -> new News(n.getTitle(),
                        n.getAuthor(), n.getPublisher(), n.getDescription(),
                        n.getPublishDate(), n.getSource(), n.getUrl(),
                        n.getCreationDate(), n.getLastModifiedDate(),
                        n.getLanguage(), toLocalCover(n.getImagePath())))
                        .forEachOrdered(withCover::add);
                added += repository.insertNewNews(withCover);
            } catch (SQLException e) {
                errors.add(source.getName() + ": database error: "
                        + e.getMessage());
            } catch (Exception e) {
                errors.add(source.getName() + ": "
                        + RssConnectionChecker.explain(e));
            }
        }
        return new RefreshResult(added, errors);
    }
    
    private static String toLocalCover(String imagePath) {
        // The parser leaves the remote url of the image here
        if (imagePath != null && imagePath.startsWith("http")) {
            return ImageCache.download(imagePath);
        }
        return imagePath;
    }
    
    @Override
    public List<News> loadStored() throws SQLException {
        // Loads the news from the database
        return repository.findAllNews();
    }
    
    public List<News> search(String text) throws SQLException {
        return repository.search(text);
    }
    
    public void add(News news) throws SQLException {
        repository.insertNews(news);
    }
    
    public void update(News old, News edited) throws SQLException {
        repository.updateNews(old.getSource(), old.getUrl(), edited);
    }
    
    public void delete(News news) throws SQLException {
        repository.deleteNews(news.getSource(), news.getUrl());
    }
    
    public void clearAll() throws SQLException {
        repository.clearAll();
    }
    
    private final NewsRepository repository;
}
