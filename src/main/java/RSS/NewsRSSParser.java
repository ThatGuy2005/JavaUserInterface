
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

            for (int i = 0; i < items.getLength(); i++) {
                Element item = (Element) items.item(i);
                
                String title = getText(item, "title");
                String link = getText(item, "link");
                String author = getText(item, "author");
                String publisher = getText(item, "publisher");
                String description = getText(item, "description");
                String language = getText(document.getDocumentElement(), "language");  
                language = language.length() >= 2 ? 
                        language.substring(0, 2).toLowerCase() : "ro";
                String source = getText(item, "source");
                
                Instant publishTime = parseDateString((getText(item, "pubDate")));
                Instant creationDate = parseDateString((getText(item, "creationDate")));
                Instant updateDate = parseDateString((getText(item, "updateDate")));
                
                // --- NEW IMAGE EXTRACTION LOGIC ---
                String imagePath = null;

                // 1. Check for standard RSS 2.0 <enclosure>
                NodeList enclosures = item.getElementsByTagName("enclosure");
                if (enclosures.getLength() > 0) {
                    Element enclosure = (Element) enclosures.item(0);
                    String type = enclosure.getAttribute("type");
                    // Ensure it's actually an image, not a podcast audio file
                    if (type != null && type.startsWith("image/")) {
                        imagePath = enclosure.getAttribute("url");
                    }
                }

                // 2. Fallback check for <media:content>
                if (imagePath == null) {
                    NodeList mediaContents = item.getElementsByTagName("media:content");
                    if (mediaContents.getLength() > 0) {
                        Element media = (Element) mediaContents.item(0);
                        String medium = media.getAttribute("medium");
                        String type = media.getAttribute("type");
                        
                        if ("image".equals(medium) || (type != null && type.startsWith("image/"))) {
                            imagePath = media.getAttribute("url");
                        }
                    }
                }
                // ----------------------------------
                
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
                                imagePath // Replaced 'null' with the extracted URL
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
