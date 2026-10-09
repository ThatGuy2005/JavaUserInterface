
package com.main.javauserinterface;

import InnerComponents.News;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

public class NewsWidget extends JPanel {
    

    private final News news;

    public NewsWidget(News news, Consumer<News> onClick) {
        this.news = news;

        setLayout(new BorderLayout(0, 2));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY), // divider line
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));               // padding
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setAlignmentX(Component.LEFT_ALIGNMENT);

        // HTML in a JLabel gives automatic word wrapping at a fixed width
        JLabel title = new JLabel("<html><body style='width:" + WIDTH + "px'><b>"
                + escape(news.getTitle()) + "</b></body></html>");
        add(title, BorderLayout.CENTER);

        // Optional secondary line, adapt to your getters
        // JLabel meta = new JLabel(news.getSource());
        // meta.setForeground(Color.GRAY);
        // meta.setFont(meta.getFont().deriveFont(11f));
        // add(meta, BorderLayout.SOUTH);

        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { onClick.accept(news); }
            @Override public void mouseEntered(MouseEvent e) { setBackground(HOVER); }
            @Override public void mouseExited(MouseEvent e) {
                // Moving onto a child label also fires mouseExited, so ignore that
                if (!contains(e.getPoint())) setBackground(NORMAL);
            }
        });
    }

    // Keeps BoxLayout from stretching the widget vertically
    @Override
    public Dimension getMaximumSize() {
        return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
    }

    public News getNews() { return news; }

    private static String escape(String s) {
        return s == null ? "" : s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
    
    private static final int WIDTH = 260;
    private static final Color NORMAL = UIManager.getColor("Panel.background");
    private static final Color HOVER = new Color(225, 235, 250);
}
