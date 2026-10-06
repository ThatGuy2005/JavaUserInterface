
package com.main.javauserinterface;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.Arrays;
import java.time.Instant;
import java.util.ArrayList;
import javax.swing.*;

// Note to self. Create an abstract class so that
// you can store both news and event or maybe an
// interface is better for creating the custom widgets 
public class MainPanel extends JPanel {
    public MainPanel() throws Exception{
        ArrayList<News> temporalNews;
        if(!RSSCommunicator.hasInternet())
        {
            throw new Exception("No internet!");
        }
        for (String url : urls) {
            int statusCode = RSSCommunicator.getStatusCode(url);
            if( statusCode !=
                    RSSCommunicator.ACCEPT_REQUEST_CODE) {
                throw new Exception("Error code passed from server "
                        + statusCode);
            }
            temporalNews = RSSCommunicator.parseRSS(url);
        }
        this.mainLayout = new BorderLayout();
        this.upperLayout = new FlowLayout();
        
        this.setLayout(this.mainLayout);
        
        this.upperPanel = new JPanel();
        this.upperPanel.setLayout(this.upperLayout);
        this.upperPanel.add(new JButton("Refreash"));
        this.upperPanel.add(new JTextField(50));
        
        this.add(this.upperPanel, BorderLayout.NORTH);
        
    }
    private BorderLayout mainLayout;
    private JPanel upperPanel;
    private FlowLayout upperLayout;
    private ArrayList<NewsWidget> news;
    private final ArrayList<String> urls = new ArrayList<>(Arrays.asList(
            "https://stiri.infomures.ro/feed/"));
}
