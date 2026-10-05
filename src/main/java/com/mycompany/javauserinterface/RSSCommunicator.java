
package com.mycompany.javauserinterface;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
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

            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int statusCode =connection.getResponseCode();

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

                String title = getText(item, "title");

                String link = getText(item, "link");

                String publishTime = getText(item, "pubDate");

                // Create News object
                //News news =
                //        new News(
                //                title,
                //                link,
                //                publishTime
                //        );

                // Add object to ArrayList
                // newsList.add(news);
            }


            input.close();

        } catch (Exception e) {

            System.out.println(
                    "RSS parsing error: "
                    + e.getMessage()
            );
        }

        return newsList;
    }


    // Read text from an XML tag
    public static String getText(
            Element element,
            String tagName) {

        NodeList nodes =
                element
                        .getElementsByTagName(
                                tagName
                        );

        if (nodes.getLength() > 0) {

            return nodes
                    .item(0)
                    .getTextContent();
        }

        return "";
    }
}
