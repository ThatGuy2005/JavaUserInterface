package com.main.javauserinterface;

import InnerComponents.News;
import RSS.NewsRSSParser;
import RSS.ParsableRSS;
import RSS.RssCommunicator;
import RSS.RssConnectionChecker;
import Services.NewsService;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

public class MainPanel extends JPanel {
    public MainPanel() throws Exception{
        this.news = new NewsService().fetchAll("RSS.json");
        
        setLayout(new BorderLayout());

        // Left: list of news buttons
        newsListPanel.setLayout(new BoxLayout(newsListPanel, BoxLayout.PAGE_AXIS));

        // Right: reading area
        readingArea.setContentType("text/html");
        readingArea.setEditable(false);
        readingArea.setText("<html><body><i>Select a news article...</i></body></html>");

        // Top: toolbar
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refreshCallback());
        JPanel upperPanel = new JPanel(new FlowLayout());
        upperPanel.add(refreshButton);
        upperPanel.add(new JTextField(50));

        // Split pane with independent scrolling
        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(newsListPanel),
                new JScrollPane(readingArea));
        splitPane.setResizeWeight(0.33);

        add(upperPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);

        showNews(this.news);
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
    
    private JButton createNewsButton(News news) {
        JButton button = new JButton(news.getTitle());
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.addActionListener(e -> {
            readingArea.setText(news.getDescription());
            readingArea.setCaretPosition(0);
        });
        return button;
    }

    private void refreshCallback() {
        try {
            showNews(newsService.fetchAll("RSS.json"));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Could not refresh news: " + ex.getMessage(),
                    "Network Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    private final NewsService newsService = new NewsService();
    private final JPanel newsListPanel = new JPanel();
    private final JEditorPane readingArea = new JEditorPane();
    private List<News> news;
}