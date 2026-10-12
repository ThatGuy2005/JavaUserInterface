
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

public class NewsService implements Servicable {
    public NewsService(NewsRepository repo) {
        this.repository = repo;
    }
    @Override
    public List<News> fetchAll(String path) {
        // The function fetches all the news from the RSS
        List<News> news = new ArrayList<News>();
        List<News> temporalNews = new ArrayList<News>();
        var rssSources = RssReaderJson.getSources(path);
        
        // Check for internet
        if(!RssCommunicator.hasInternet())
        {
            // If no internet, then try loading from the database
            try {
                return loadStored();
            } catch (SQLException e) {
                System.out.println("Could not read stored news: "
                        + e.getMessage());
                return null;
            }
            
        }
        
        for(RssSource source : rssSources) {
            String url = source.getURL();
            int statusCode = RssCommunicator.getStatusCode(url);
            if( statusCode !=
                    RssConnectionChecker.ACCEPT_REQUEST_CODE) {
                continue;
            }
            temporalNews = new NewsRSSParser().parseRSS(url);
            // Create the buttons based on the news
            // The next step is to insert the news into the database
            for(News tNews : temporalNews) {
                news.add(tNews);
            }
        }
        return news;
    }
    
    public List<News> loadStored() throws SQLException {
        // Loads the news from the database
        return repository.findAllNews();
    }
    
    public List<News> searchSimilarTitle(String title) {
        return repository.similarTitle(title);
    }
    
    public Integer refreshFromRss(String path) throws Exception {
        // Fetches the news from a given RSS.json
        List<News> fresh = fetchAll(path);    
        // Saves the news into the database
        repository.saveAll(fresh);
        // If no new news were loaded this will be zero
        return fresh.size();
    }
    
    private final NewsRepository repository;
}
