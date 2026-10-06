
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
// Add JScrollPanel and JSplitPanel
public class MainPanel extends JPanel {
    public MainPanel() throws Exception{
        ArrayList<News> temporalNews;
        if(!RSSCommunicator.hasInternet())
        {
            throw new Exception("No internet!");
        }
        this.news = new ArrayList<>();
        for (String url : urls) {
            int statusCode = RSSCommunicator.getStatusCode(url);
            if( statusCode !=
                    RSSCommunicator.ACCEPT_REQUEST_CODE) {
                throw new Exception("Error code passed from server "
                        + statusCode);
            }
            temporalNews = RSSCommunicator.parseRSS(url);
            for(News tNews : temporalNews) {
                news.add(new JButton(tNews.getTitle()));
            }
        }
        this.mainLayout = new BorderLayout();
        this.upperLayout = new FlowLayout();
        this.leftPanel = new JPanel();
        this.leftLayout = new BoxLayout(this.leftPanel,BoxLayout.PAGE_AXIS);
        
        this.leftPanel.setLayout(this.leftLayout);
        
        
        this.setLayout(this.mainLayout);
        
        for(JButton newsWidget : news) {
            this.leftPanel.add(newsWidget);
        }
        
        this.upperPanel = new JPanel();
        this.upperPanel.setLayout(this.upperLayout);
        this.upperPanel.add(new JButton("Refreash"));
        this.upperPanel.add(new JTextField(50));
        
        this.add(this.upperPanel, BorderLayout.NORTH);
        this.add(this.leftPanel, BorderLayout.WEST);
    }
    private BorderLayout mainLayout;
    private JPanel leftPanel;
    private BoxLayout leftLayout;
    private JPanel upperPanel;
    private FlowLayout upperLayout;
    private ArrayList<JButton> news;
    private final ArrayList<String> urls = new ArrayList<>(Arrays.asList(
            "https://stiri.infomures.ro/feed/"));
}
