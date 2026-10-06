
package com.main.javauserinterface;

import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class NewsWidget extends JPanel {
    public NewsWidget(News news) {
        
        this.news = news;
        this.title = new JLabel();
        this.layout = new FlowLayout();
        this.title.setText(this.news.getTitle());
        this.setLayout(layout);
        this.add(this.title);
    }
    
    private JLabel title;
    private FlowLayout layout;
    private News news;
}
