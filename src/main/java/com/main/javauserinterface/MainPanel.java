package com.main.javauserinterface;

import InnerComponents.News;
import RSS.NewsRSSParser;
import RSS.ParsableRSS;
import RSS.RssCommunicator;
import RSS.RssConnectionChecker;
import Repositories.NewsRepository;
import Services.NewsService;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.ArrayList;
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
        readingArea.setText
                ("<html><body><i>Select a news article...</i></body></html>");

        // Top: toolbar
        this.refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refreshCallback());
        this.searchButton = new JButton("Search");
        searchButton.addActionListener(e -> searchCallback());
        this.searchInput = new JTextField(50);
        searchInput.addActionListener(e -> searchCallback());
        
        JPanel upperPanel = new JPanel(new FlowLayout());
        upperPanel.add(this.refreshButton);
        upperPanel.add(this.searchButton);
        upperPanel.add(this.searchInput);

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
        
        reloadFromDatabase();     // instant, local
        refresh();
        
    }
    
    private void reloadFromDatabase() {
        try {
            showNews(newsService.loadStored());
        } catch (SQLException e) {
            System.out.println("Could not read stored news: " + e.getMessage());
        }
    }

    private void refresh() {
        refreshButton.setEnabled(false);
        new SwingWorker<Integer, Void>() {
            @Override protected Integer doInBackground() throws Exception {
                return newsService.refreshFromRss("RSS.json");
            }
            @Override protected void done() {
                refreshButton.setEnabled(true);
                try {
                    get();                                 // rethrows any exception from the background
                    reloadFromDatabase();
                } catch (Exception ex) {
                    System.out.println("Could not refresh: " + ex.getCause().getMessage());
                }
            }
        }.execute();
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
        readingArea.setText(news.getDescription());
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
        refreshButton.setEnabled(false); // Prevent spam-clicking while loading

        new SwingWorker<List<News>, Void>() {
            @Override
            protected List<News> doInBackground() throws Exception {
                // Heavy network lifting happens here, safely in the background
                return newsService.fetchAll("RSS.json");
            }

            @Override
            protected void done() {
                refreshButton.setEnabled(true); // Re-enable the button
                try {
                    // get() retrieves the return value from doInBackground()
                    showNews(get()); 
                } catch (Exception ex) {
                    // get() throws an ExecutionException if doInBackground threw an error
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
}