package com.main.javauserinterface;

import InnerComponents.News;
import Repositories.NewsRepository;
import Services.NewsService;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.sql.SQLException;
import java.util.List;
import javax.swing.*;

public class MainPanel extends JPanel {
    public MainPanel(String dbPath) throws Exception{
        this.newsService = new NewsService(new NewsRepository(dbPath));
        
        setLayout(new BorderLayout());

        // Left: list of news buttons
        newsListPanel.setLayout(new BoxLayout(newsListPanel,
                BoxLayout.PAGE_AXIS));

        // Right: reading area
        readingArea.setContentType("text/html");
        readingArea.setEditable(false);

        // Add 15 pixels of padding around the text
        readingArea.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Apply modern CSS styling
        javax.swing.text.html.HTMLEditorKit kit = new javax.swing.text.html.HTMLEditorKit();
        readingArea.setEditorKit(kit);
        javax.swing.text.html.StyleSheet styleSheet = kit.getStyleSheet();

        // Define your font family, sizes, and colors
        styleSheet.addRule("body { font-family: 'Segoe UI', Tahoma, Arial, sans-serif; font-size: 14px; color: #333333; }");
        styleSheet.addRule("h1, h2, h3 { color: #2C3E50; margin-top: 5px; }");
        styleSheet.addRule("p { margin-bottom: 10px; line-height: 1.5; }");
        styleSheet.addRule("a { color: #2980B9; text-decoration: none; }");

        javax.swing.text.Document doc = kit.createDefaultDocument();
        readingArea.setDocument(doc);

        // Set default text
        String welcomePath = "C:\\Users\\Csongor\\Documents\\NetBeansProjects\\JavaUserInterface\\welcome.png";
        String welcomeImg = new java.io.File(welcomePath).toURI().toString();

        readingArea.setText("<html><body style='text-align:center;'><img src='" + welcomeImg + "' width='200'/><br/><i>Select a news article...</i></body></html>");

        // Top: toolbar setup
        this.refreshButton = new JButton("Refresh");
        // When the refreash is pressed, then data should be loaded from the RSS
        refreshButton.addActionListener(e -> refreshCallback());
        this.searchButton = new JButton("Search");
        searchButton.addActionListener(e -> searchCallback());
        this.searchInput = new JTextField(50);
        searchInput.addActionListener(e -> searchCallback());
        
        // 1. Initialize the loading label and hide it
        this.loadingLabel = new JLabel("Loading...");
        this.loadingLabel.setForeground(java.awt.Color.GRAY);
        this.loadingLabel.setVisible(false);

        // 2. Use BorderLayout to push the label to the right
        JPanel upperPanel = new JPanel(new BorderLayout());
        
        JPanel leftControls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftControls.add(this.refreshButton);
        leftControls.add(this.searchButton);
        leftControls.add(this.searchInput);
        
        JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightControls.add(this.loadingLabel);

        upperPanel.add(leftControls, BorderLayout.WEST);
        upperPanel.add(rightControls, BorderLayout.EAST);

        JSplitPane splitPane = new JSplitPane(
        JSplitPane.HORIZONTAL_SPLIT,
        new JScrollPane(newsListPanel),
        new JScrollPane(readingArea));

        splitPane.setResizeWeight(0.33); // Ensures the 1/3 ratio is maintained when resizing the window

        // Defer the divider location setting until after the component has a physical size
        splitPane.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                splitPane.setDividerLocation(0.33);
                splitPane.removeComponentListener(this);
            }
        });

        add(upperPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
        
        // instant, local
        reloadFromDatabase();
        // Get the latest news
        refreshCallback();
    }
    
    private void reloadFromDatabase() {
        try {
            showNews(newsService.loadStored());
        } catch (SQLException e) {
            System.out.println("Could not read stored news: " + e.getMessage());
        }
    }

    private void showNews(List<News> news) {
        newsListPanel.removeAll();
        for (News n : news) {
            newsListPanel.add(new NewsWidget(n, this::showArticle));
        }
        newsListPanel.revalidate();
        newsListPanel.repaint();
    }

    private void showArticle(News news) {
        String imagePath = news.getImagePath();
        String imageSrc = "";
        
        if (imagePath != null && !imagePath.trim().isEmpty()) {
            if (imagePath.startsWith("http://") || imagePath.startsWith("https://")) {
                // It is a web URL from the RSS feed, use it exactly as is
                imageSrc = imagePath;
            } else {
                // It is a local file (like your welcome.png), safely convert it
                imageSrc = new java.io.File(imagePath).toURI().toString();
            }
        } 
        
        String finalDescription = news.getDescription();
        if (finalDescription == null) {
            finalDescription = "";
        } else if (!imageSrc.isEmpty()) {
            // ONLY remove embedded images IF we are showing our own cover image
            finalDescription = finalDescription.replaceAll("(?i)<img[^>]*>", "");
        }
        
        // Build the HTML
        String htmlContent = "<html><body>" + 
                             "<h2>" + news.getTitle() + "</h2>";
                             
        if (!imageSrc.isEmpty()) {
            htmlContent += "<img src='" + imageSrc + "' width='400' /><br/>";
        }
                             
        htmlContent += finalDescription + 
                       "</body></html>";
 
        readingArea.setText(htmlContent);
        readingArea.setCaretPosition(0);
    }
    
    private void searchCallback() {
        
        this.searchButton.setEnabled(false);
        
        new SwingWorker<List<News>, Void>() {
            @Override
            protected List<News> doInBackground() throws Exception {
                return newsService.searchSimilarTitle(searchInput.getText());
            }
            @Override
            protected void done() {
                searchButton.setEnabled(true);
                try {
                    showNews(get());
                } catch (Exception ex) {
                    // get() throws an ExecutionException 
                    // if doInBackground threw an error
                    JOptionPane.showMessageDialog(MainPanel.this,
                            "Could not load search results: " + 
                                    ex.getCause().getMessage(),
                            "Network Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }
    private void refreshCallback() {
        refreshButton.setEnabled(false); 
        loadingLabel.setVisible(true); // <-- Show loading

        new SwingWorker<List<News>, Void>() {
            @Override
            protected List<News> doInBackground() throws Exception {
                newsService.refreshFromRss("RSS.json");
                return newsService.fetchAll("RSS.json");
            }

            @Override
            protected void done() {
                refreshButton.setEnabled(true); 
                loadingLabel.setVisible(false); // <-- Hide loading
                
                try {
                    showNews(get()); 
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(MainPanel.this,
                            "Could not refresh news: " + ex.getCause().getMessage(),
                            "Network Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }
    
    private final NewsService newsService;
    private final JPanel newsListPanel = new JPanel();
    private final JEditorPane readingArea = new JEditorPane();
    private JButton refreshButton;
    private JButton searchButton;
    private JTextField searchInput;
    private JLabel loadingLabel;
}