
package RSS;

import InnerComponents.News;
import static RSS.RssCommunicator.getChild;
import static RSS.RssCommunicator.getText;
import static RSS.RssCommunicator.parseDateString;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


public class NewsRSSParser implements ParsableRSS<News> {
    @Override
    public ArrayList<News> parseRSS(RssSource source) throws Exception {
        ArrayList<News> newsList = new ArrayList<News>();

        Document document;
        try (InputStream input =
                RssConnectionChecker.openStream(source.getURL())) {
            document = newBuilder().parse(input);
        } catch (SAXException e) {
            throw new IOException("The feed is not valid XML: "
                    + e.getMessage(), e);
        }
        
        // The channel title is the publisher, the language comes from the json
        Element channel = getChild(document.getDocumentElement(), "channel");
        String channelTitle = channel == null ? null
                : getText(channel, "title");
        String publisher = channelTitle != null ? channelTitle
                : source.getName();
        String language = source.getLanguage() != null
                ? source.getLanguage().toUpperCase() : "RO";
        Timestamp now = new Timestamp(System.currentTimeMillis());

        // Find all <item> elements
        NodeList items = document.getElementsByTagName("item");

        for (int i = 0; i < items.getLength(); i++) {
            Element item = (Element) items.item(i);

            String title = getText(item, "title");
            String link = getText(item, "link");
            if (title == null || link == null) {
                // Both are part of the primary key / required by the table
                continue;
            }
            // WordPress feeds use dc:creator instead of author
            String author = getText(item, "dc:creator");
            if (author == null) {
                author = getText(item, "author");
            }
            String description = getText(item, "description");
            String content = getText(item, "content:encoded");

            Timestamp publishDate = parseDateString(getText(item, "pubDate"));

            // The cover is downloaded later, only for the news that are new
            String imageUrl = findImageUrl(item, content, description);

            newsList.add(new News(title, author, publisher,
                    description == null ? "" : description, publishDate,
                    source.getName(), link, now, now, language, imageUrl));
        }
        return newsList;
    }
    
    private static String findImageUrl(Element item, String... htmlParts) {
        // 1. Standard RSS 2.0 <enclosure>, 2. <media:content>,
        // 3. <media:thumbnail>
        for (String tag : new String[]{"enclosure", "media:content",
                "media:thumbnail"}) {
            Element e = getChild(item, tag);
            if (e == null || e.getAttribute("url").isBlank()) {
                continue;
            }
            String type = e.getAttribute("type");
            String medium = e.getAttribute("medium");
            // Ensure it's an image, not a podcast audio file
            boolean image = tag.equals("media:thumbnail")
                    || type.startsWith("image/") || medium.equals("image")
                    || (tag.equals("media:content") && type.isEmpty()
                        && medium.isEmpty());
            if (image) {
                return e.getAttribute("url");
            }
        }
        // 4. The first <img> inside the text
        for (String html : htmlParts) {
            if (html != null) {
                Matcher m = IMG_IN_HTML.matcher(html);
                if (m.find()) {
                    return m.group(1);
                }
            }
        }
        return null;
    }
    
    private static DocumentBuilder newBuilder() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        // The feeds come from the internet: never load external entities
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setFeature(
                "http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature(
                "http://xml.org/sax/features/external-parameter-entities", false);
        DocumentBuilder builder = factory.newDocumentBuilder();
        // The default handler prints parse errors to the console
        builder.setErrorHandler(new DefaultHandler());
        return builder;
    }
    
    private static final Pattern IMG_IN_HTML = Pattern.compile(
            "<img[^>]+src=[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE);
}
