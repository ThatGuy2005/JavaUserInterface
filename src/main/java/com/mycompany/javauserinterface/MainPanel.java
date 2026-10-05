
package com.mycompany.javauserinterface;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import javax.swing.*;

// Note to self. Create an abstract class so that
// you can store both news and event or maybe an
// interface is better for creating the custom widgets 
public class MainPanel extends JPanel {
    public MainPanel() {
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
    private ArrayList<News> news;
}
