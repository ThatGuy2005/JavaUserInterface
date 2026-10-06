package com.main.javauserinterface;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.Arrays;
import java.util.ArrayList;
import javax.swing.*;

// Note to self. Create an abstract class so that
// you can store both news and event or maybe an
// interface is better for creating the custom widgets 
// Add JScrollPanel and JSplitPanel
// TODO REFACTOR THIS STUFF!!!!!!
public class MainPanel extends JPanel {
    public MainPanel() throws Exception{
        ArrayList<News> temporalNews;
        // Check for internet
        if(!RSSCommunicator.hasInternet())
        {
            throw new Exception("No internet!");
        }
        this.news = new ArrayList<>();
        // For now go throught the given urls
        for (String url : urls) {
            int statusCode = RSSCommunicator.getStatusCode(url);
            if( statusCode !=
                    RSSCommunicator.ACCEPT_REQUEST_CODE) {
                throw new Exception("Error code passed from server "
                        + statusCode);
            }
            temporalNews = RSSCommunicator.parseRSS(url);
            // Create the buttons based on the news
            // The next step is to insert the news into the database
            for(News tNews : temporalNews) {
                JButton newsButton = new JButton(tNews.getTitle());
                // Add a action listener to display the description on
                // the reading area
                newsButton.addActionListener(e -> {
                    readingArea.setText(tNews.getDescription()); 
                    readingArea.setCaretPosition(0);
                });
                news.add(newsButton);
            }
            // After this invoke the getAllNews() to also cache them
        }
        
        // Initialize the components
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
        
        this.setLayout(this.mainLayout);
        
        for(JButton newsWidget : news) {
            this.leftPanel.add(newsWidget);
        }
        
        this.upperPanel = new JPanel();
        this.upperPanel.setLayout(this.upperLayout);
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refreshCallback());
        this.upperPanel.add(refreshButton); 
        this.upperPanel.add(new JTextField(50));
        
        // 1. Create a reading area (JTextArea or JTextPane)
        this.readingArea = new JEditorPane();
        this.readingArea.setContentType("text/html");
        this.readingArea.setEditable(false);

        // Note: Remove setLineWrap(true) and setWrapStyleWord(true). 
        // JEditorPane wraps HTML automatically based on the window size.
        this.readingArea.setText("<html><body><i>Select a news article...</i></body></html>");
        
        // 2. Wrap both panels in ScrollPanes so they scroll independently
        JScrollPane leftScroll = new JScrollPane(this.leftPanel);
        JScrollPane rightScroll = new JScrollPane(this.readingArea);
        
        // 3. Create the SplitPane and set the 1/3 ratio
        this.splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftScroll, rightScroll);
        this.splitPane.setResizeWeight(0.33); // Allocates 33% of space to the left component
        
        this.add(this.upperPanel, BorderLayout.NORTH);
        
        // 4. Add the split pane to the CENTER instead of WEST so it fills the screen
        this.add(this.splitPane, BorderLayout.CENTER);
    }
    
    private void refreshCallback() {
        try {
            if (!RSSCommunicator.hasInternet()) {
                throw new Exception("No internet!");
            }
        
            this.news.clear(); // Clear the underlying array
            this.leftPanel.removeAll(); // Clear the old buttons from the screen
        
            for (String url : urls) {
                int statusCode = RSSCommunicator.getStatusCode(url);
                if (statusCode != RSSCommunicator.ACCEPT_REQUEST_CODE) {
                    throw new Exception("Error code passed from server " + statusCode);
                }
            
                ArrayList<News> temporalNews = RSSCommunicator.parseRSS(url);
                for (News tNews : temporalNews) {
                    JButton newsWidget = new JButton(tNews.getTitle());
                    this.news.add(newsWidget);
                    this.leftPanel.add(newsWidget); // Add the new button to the panel
                }
            }
        
            // Force Swing to recalculate the layout and redraw the screen
            this.leftPanel.revalidate();
            this.leftPanel.repaint();
        
        } catch (Exception ex) {
            // Pop up an error box so the user knows the refresh failed
            JOptionPane.showMessageDialog(this, 
                "Could not refresh news: " + ex.getMessage(), 
                "Network Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    private BorderLayout mainLayout;
    private JPanel leftPanel;
    private BoxLayout leftLayout;
    private JPanel upperPanel;
    private FlowLayout upperLayout;
    private ArrayList<JButton> news;
    private JSplitPane splitPane;
    private JEditorPane readingArea;
    
    private final ArrayList<String> urls = new ArrayList<>(Arrays.asList(
            "https://stiri.infomures.ro/feed/"));
}