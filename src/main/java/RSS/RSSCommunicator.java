
package RSS;

import InnerComponents.News;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class RSSCommunicator {
    // Check if internet connection exists
    public static boolean hasInternet() {
        try {
            URL url = new URL("https://www.google.com");
            
            HttpURLConnection connection =
                    (HttpURLConnection)
                            url.openConnection();
            
            // Vodoo constant, what does it mean? Why 3000?
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);

            connection.connect();

            int statusCode = connection.getResponseCode();

            connection.disconnect();

            return statusCode == 200;

        } catch (Exception e) {

            return false;
        }
    }
    
    public static int getStatusCode(String urlAddress) {
        try {

            URL url = new URL(urlAddress);

            HttpURLConnection connection = (HttpURLConnection)
                    url.openConnection();

            connection.setRequestMethod("GET");
               
            // Vodoo constant, what does it mean? Why 5000?
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int statusCode = connection.getResponseCode();

            connection.disconnect();

            return statusCode;

        } catch (Exception e) {
            System.out.println( "Connection error: "+ e.getMessage());
            return -1;
        }
    }
    
    // Download and parse RSS
    public static ArrayList<News> parseRSS(String urlAddress) {
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
                String language = getText(item, "language");
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

    // This may need explanation
    // Read text from an XML tag
    public static String getText(Element element, String tagName) {
        NodeList nodes = element.getElementsByTagName(tagName);
        
        if (nodes.getLength() > 0) {
            // Why the first? What if there are more
            return nodes.item(0).getTextContent();
        }
        // I should also check for empty Strings, but then why not return null?
        return "";
    }
    
    public static Instant parseDateString(String date) {
        if(date == null || date.isEmpty()) {
            return null;
        }
        try {
            return ZonedDateTime.parse(date, 
                    DateTimeFormatter.RFC_1123_DATE_TIME).toInstant();
        } catch (DateTimeParseException e) {
            System.out.println("Could not parse date!");
            return null;
        }
    }
    public static final int ACCEPT_REQUEST_CODE = 200;
}
