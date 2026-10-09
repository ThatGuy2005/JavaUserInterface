
package RSS;

import InnerComponents.News;
import static RSS.RssCommunicator.getText;
import static RSS.RssCommunicator.parseDateString;
import java.io.InputStream;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


public class NewsRSSParser implements ParsableRSS {
    @Override
    public ArrayList<News> parseRSS(String urlAddress) {
        ArrayList<News> newsList = new ArrayList<News>();

        try {
            URL url = new URL(urlAddress);
            InputStream input = url.openStream();
            
            // Create XML parser
            DocumentBuilderFactory factory = DocumentBuilderFactory
                    .newInstance();

            DocumentBuilder builder = factory.newDocumentBuilder();

            // Parse XML
            Document document = builder.parse(input);

            // Find all <item> elements
            NodeList items = document.getElementsByTagName("item");

            for (int i = 0;i < items.getLength();i++) {
                Element item = (Element)items.item(i);
                
                // Ask if this is OKAY. Personally I don't think so.
                // Also add checks for empty strings
                String title = getText(item, "title");
                String link = getText(item, "link");
                String author = getText(item, "author");
                String publisher = getText(item, "publisher");
                String description = getText(item, "description");
                String language = getText(document.getDocumentElement(),
                        "language");  // 
                language = language.length() >= 2 ? 
                        language.substring(0, 2).toLowerCase() : "ro";
                String source = getText(item, "source");
                
                // Ask how to process the dates
                Instant publishTime = parseDateString
                    ((getText(item, "pubDate")));
                Instant creationDate = parseDateString
                    ((getText(item, "creationDate")));
                Instant updateDate = parseDateString
                    ((getText(item, "updateDate")));
                
                // Create News object
                News news =
                        new News(
                                title,
                                author,
                                publisher,
                                description,
                                publishTime,
                                source,
                                link,
                                creationDate,
                                updateDate,
                                language,
                                null
                        );

                // Add object to ArrayList
                 newsList.add(news);
            }
            input.close();
        } catch (Exception e) {
            System.out.println("RSS parsing error: " + e.getMessage());
        }

        return newsList;
    }
}
