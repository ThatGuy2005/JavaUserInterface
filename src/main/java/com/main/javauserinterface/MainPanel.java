package com.main.javauserinterface;


import InnerComponents.News;
import RSS.RssConnectionChecker;
import Repositories.NewsRepository;
import Services.NewsService;
import Services.RefreshResult;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.awt.image.BufferedImage;
import java.net.URI;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.HyperlinkEvent;

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
        
        // Open the links of the article in the browser
        readingArea.addHyperlinkListener(e -> {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED
                    && e.getURL() != null) {
                openInBrowser(e.getURL().toString());
            }
        });

        showWelcome();

        // Top: toolbar setup
        this.refreshButton = new JButton("Refresh");
        // When the refreash is pressed, then data should be loaded from the RSS
        refreshButton.addActionListener(e -> refreshCallback());
        this.searchButton = new JButton("Search");
        searchButton.addActionListener(e -> searchCallback());
        this.searchInput = new JTextField(50);
        searchInput.addActionListener(e -> searchCallback());
        
        // Search while typing, after a short pause so every key press
        // does not start a query
        searchTimer = new Timer(300, e -> searchCallback());
        searchTimer.setRepeats(false);
        searchInput.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { searchTimer.restart(); }
            public void removeUpdate(DocumentEvent e) { searchTimer.restart(); }
            public void changedUpdate(DocumentEvent e) { searchTimer.restart(); }
        });
        
        // 1. Initialize the loading label and hide it
        this.loadingLabel = new JLabel("Loading...");
        this.loadingLabel.setForeground(java.awt.Color.GRAY);
        this.loadingLabel.setIconTextGap(6);
        File gif = new File(LOADING_GIF);
        if (gif.isFile()) {
            this.loadingLabel.setIcon(new ImageIcon(gif.getPath()));
        }
        this.loadingLabel.setVisible(false);
        
        // Shows the result of the last refresh or search
        this.statusLabel = new JLabel(" ");
        this.statusLabel.setForeground(java.awt.Color.GRAY);

        // 2. Use BorderLayout to push the label to the right
        JPanel upperPanel = new JPanel(new BorderLayout());
        
        JPanel leftControls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftControls.add(this.refreshButton);
        leftControls.add(this.searchButton);
        leftControls.add(this.searchInput);
        
        JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightControls.add(this.statusLabel);
        rightControls.add(this.loadingLabel);

        upperPanel.add(leftControls, BorderLayout.WEST);
        upperPanel.add(rightControls, BorderLayout.EAST);
        
        // Bottom of the list: Add / Edit / Delete / Clear DB
        this.addButton = new JButton("Add");
        addButton.addActionListener(e -> addCallback());
        this.editButton = new JButton("Edit");
        editButton.addActionListener(e -> editCallback());
        this.deleteButton = new JButton("Delete");
        deleteButton.addActionListener(e -> deleteCallback());
        this.clearButton = new JButton("Clear DB");
        clearButton.addActionListener(e -> clearCallback());
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);
        
        JPanel masterPanel = new JPanel(new BorderLayout());
        JScrollPane listScroll = new JScrollPane(newsListPanel);
        listScroll.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        masterPanel.add(listScroll, BorderLayout.CENTER);
        masterPanel.add(buttonPanel, BorderLayout.SOUTH);

        JSplitPane splitPane = new JSplitPane(
        JSplitPane.HORIZONTAL_SPLIT,
        masterPanel,
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
        
        // On start only the saved news are shown, the RSS is read when
        // the Refresh button is pressed
        reloadFromDatabase();
        if (shownCount == 0) {
            statusLabel.setText("No saved news. Press Refresh to download.");
        }
    }
    
    // ---------- showing the data ----------
    
    private List<News> loadCurrent() throws SQLException {
        // Everything, or only the news that match the search box
        String text = searchInput.getText().trim();
        return text.isEmpty() ? newsService.loadStored()
                : newsService.search(text);
    }
    
    private void reloadFromDatabase() {
        try {
            showNews(loadCurrent());
        } catch (SQLException e) {
            showError("Could not read the database", e);
        }
    }

    private void showNews(List<News> news) {
        shownCount = news.size();
        newsListPanel.removeAll();
        boolean selectionStillShown = false;
        for (News n : news) {
            NewsWidget widget = new NewsWidget(n, this::select);
            if (isSelected(n)) {
                widget.setSelected(true);
                selectionStillShown = true;
            }
            newsListPanel.add(widget);
        }
        newsListPanel.revalidate();
        newsListPanel.repaint();
        
        if (!selectionStillShown) {
            selectedNews = null;
            showWelcome();
        }
        updateButtons();
    }
    
    private boolean isSelected(News n) {
        return selectedNews != null
                && selectedNews.getSource().equals(n.getSource())
                && selectedNews.getUrl().equals(n.getUrl());
    }
    
    private void select(News news) {
        selectedNews = news;
        for (java.awt.Component c : newsListPanel.getComponents()) {
            if (c instanceof NewsWidget w) {
                w.setSelected(isSelected(w.getNews()));
            }
        }
        showArticle(news);
        updateButtons();
    }
    
    private void updateButtons() {
        editButton.setEnabled(selectedNews != null);
        deleteButton.setEnabled(selectedNews != null);
    }
    
    private void showWelcome() {
        String welcome = "";
        File image = new File(WELCOME_IMAGE);
        if (image.isFile()) {
            welcome = "<img src='" + image.toURI() + "' width='200'/><br/>";
        }
        readingArea.setText("<html><body style='text-align:center;'>"
                + welcome + "<i>Select a news article...</i></body></html>");
    }

    private void showArticle(News news) {
        String description = news.getDescription() == null ? ""
                : news.getDescription();
        
        // The cover is shown above the text, so images inside the text
        // are removed
        description = description.replaceAll("(?i)<img[^>]*>", "");
        
        // Build the HTML
        StringBuilder html = new StringBuilder("<html><body>");
        html.append("<h2>").append(escape(news.getTitle())).append("</h2>");
        
        File cover = coverFile(news.getImagePath());
        if (cover != null) {
            html.append("<img src='").append(cover.toURI()).append("' width='")
                    .append(imageWidth(cover)).append("' /><br/>");
        }
        
        html.append("<p><b>Source:</b> ").append(escape(orUnknown(news.getSource())))
            .append(" &bull; <b>Author:</b> ").append(escape(orUnknown(news.getAuthor())))
            .append(" &bull; <b>Publisher:</b> ").append(escape(orUnknown(news.getPublisher())))
            .append("<br/><b>Published:</b> ").append(news.getPublishTime() == null
                    ? "unknown" : news.getPublishTime().toLocalDateTime().format(DATE_FORMAT))
            .append(" &bull; <b>Language:</b> ").append(escape(orUnknown(news.getLanguage())))
            .append("<br/><b>URL:</b> <a href=\"").append(escape(news.getUrl()))
            .append("\">").append(escape(news.getUrl())).append("</a></p><hr/>")
            .append(description)
            .append("</body></html>");
 
        readingArea.setText(html.toString());
        readingArea.setCaretPosition(0);
    }
    
    private static File coverFile(String imagePath) {
        // The cover if it is a readable image, otherwise the default one
        File file = new File(imagePath == null ? News.DEFAULT_COVER : imagePath);
        if (imageWidth(file) < 0) {
            file = new File(News.DEFAULT_COVER);
        }
        return imageWidth(file) < 0 ? null : file;
    }
    
    private static int imageWidth(File file) {
        // Real width but at most 400 px, -1 if it is not a readable image
        try {
            BufferedImage image = file.isFile() ? ImageIO.read(file) : null;
            return image == null ? -1 : Math.min(400, image.getWidth());
        } catch (IOException e) {
            return -1;
        }
    }
    
    private static String orUnknown(String s) {
        return s == null || s.isBlank() ? "unknown" : s;
    }
    
    private static String escape(String s) {
        return s == null ? "" : s.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;");
    }
    
    private static void openInBrowser(String url) {
        try {
            if (Desktop.isDesktopSupported()
                    && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(URI.create(url));
            }
        } catch (Exception e) {
            // opening the browser is only a convenience
        }
    }
    
    // ---------- search ----------
    
    private void searchCallback() {
        searchTimer.stop();
        searchButton.setEnabled(false);
        // Only the newest search may update the list
        final int id = ++searchId;
        
        new SwingWorker<List<News>, Void>() {
            @Override
            protected List<News> doInBackground() throws Exception {
                return loadCurrent();
            }
            @Override
            protected void done() {
                if (id != searchId) {
                    return;
                }
                searchButton.setEnabled(true);
                try {
                    List<News> found = get();
                    showNews(found);
                    statusLabel.setText(searchInput.getText().trim().isEmpty()
                            ? " " : found.size() + " result(s)");
                } catch (Exception ex) {
                    // get() throws an ExecutionException 
                    // if doInBackground threw an error
                    showError("Could not load search results", ex.getCause() != null
                            ? (Exception) ex.getCause() : ex);
                }
            }
        }.execute();
    }
    
    // ---------- refresh ----------
    
    private void refreshCallback() {
        refreshButton.setEnabled(false); 
        clearButton.setEnabled(false);
        loadingLabel.setVisible(true); // <-- Show loading
        statusLabel.setText(" ");

        new SwingWorker<RefreshResult, Void>() {
            @Override
            protected RefreshResult doInBackground() throws Exception {
                // Download, parse and save the new news in the background
                return newsService.refresh(RSS_PATH);
            }

            @Override
            protected void done() {
                refreshButton.setEnabled(true); 
                clearButton.setEnabled(true);
                loadingLabel.setVisible(false); // <-- Hide loading
                
                // Show what is now in the database
                reloadFromDatabase();
                try {
                    RefreshResult result = get();
                    statusLabel.setText("Added " + result.getAdded() + " new item(s)");
                    if (!result.getErrors().isEmpty()) {
                        JOptionPane.showMessageDialog(MainPanel.this,
                                "Some sources could not be loaded:\n\n- "
                                        + String.join("\n- ", result.getErrors()),
                                "Refresh finished with errors",
                                JOptionPane.WARNING_MESSAGE);
                    }
                } catch (Exception ex) {
                    showError("Could not refresh news", ex.getCause() != null
                            ? (Exception) ex.getCause() : ex);
                }
            }
        }.execute();
    }
    
    // ---------- Add / Edit / Delete / Clear DB ----------
    
    private void addCallback() {
        News news = NewsDialog.show(owner(), null);
        if (news == null) {
            return;
        }
        try {
            newsService.add(news);
            selectedNews = news;
            reloadFromDatabase();
            if (selectedNews != null) {
                showArticle(selectedNews);
            }
        } catch (SQLException e) {
            showError(isDuplicate(e) ? "This source and URL already exist."
                    : "Could not add the news", e);
        }
    }
    
    private void editCallback() {
        News old = selectedNews;
        if (old == null) {
            return;
        }
        News edited = NewsDialog.show(owner(), old);
        if (edited == null) {
            return;
        }
        try {
            newsService.update(old, edited);
            selectedNews = edited;
            reloadFromDatabase();
            if (selectedNews != null) {
                showArticle(selectedNews);
            }
        } catch (SQLException e) {
            showError(isDuplicate(e)
                    ? "Another news already has this source and URL."
                    : "Could not update the news", e);
        }
    }
    
    private void deleteCallback() {
        News news = selectedNews;
        if (news == null) {
            return;
        }
        int answer = JOptionPane.showConfirmDialog(this,
                "Delete \"" + news.getTitle() + "\"?", "Delete news",
                JOptionPane.YES_NO_OPTION);
        if (answer != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            newsService.delete(news);
            selectedNews = null;
            reloadFromDatabase();
        } catch (SQLException e) {
            showError("Could not delete the news", e);
        }
    }
    
    private void clearCallback() {
        int answer = JOptionPane.showConfirmDialog(this, "Are you sure?",
                "Clear database", JOptionPane.YES_NO_OPTION);
        if (answer != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            newsService.clearAll();
            selectedNews = null;
            reloadFromDatabase();
            statusLabel.setText("Database cleared");
        } catch (SQLException e) {
            showError("Could not clear the database", e);
        }
    }
    
    private static boolean isDuplicate(SQLException e) {
        String m = e.getMessage();
        return m != null && (m.contains("UNIQUE") || m.contains("PRIMARY KEY"));
    }
    
    private Window owner() {
        return SwingUtilities.getWindowAncestor(this);
    }
    
    private void showError(String message, Exception e) {
        JOptionPane.showMessageDialog(this, message + "\n"
                + RssConnectionChecker.explain(e), "Error",
                JOptionPane.ERROR_MESSAGE);
    }
    
    private static final String RSS_PATH = "res/rss.json";
    private static final String LOADING_GIF = "res/images/loading.gif";
    private static final String WELCOME_IMAGE = "res/images/welcome.png";
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    private final NewsService newsService;
    private final JPanel newsListPanel = new JPanel();
    private final JEditorPane readingArea = new JEditorPane();
    private JButton refreshButton;
    private JButton searchButton;
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton clearButton;
    private JTextField searchInput;
    private JLabel loadingLabel;
    private JLabel statusLabel;
    private Timer searchTimer;
    private News selectedNews;
    private int shownCount;
    private int searchId;
}
