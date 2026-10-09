
package Services;

import InnerComponents.News;
import RSS.NewsRSSParser;
import RSS.RssCommunicator;
import RSS.RssConnectionChecker;
import RSS.RssReaderJson;
import RSS.RssSource;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;

public class NewsService implements Servicable {
    @Override
    public List<News> fetchAll(String path) {
        List<News> news = new ArrayList<News>();
        List<News> temporalNews = new ArrayList<News>();
        var rssSources = RssReaderJson.getSources(path);
        
        if(!RssCommunicator.hasInternet())
        {
            return null;
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
}
