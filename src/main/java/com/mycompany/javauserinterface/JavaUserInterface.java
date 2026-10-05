
package com.mycompany.javauserinterface;

import java.awt.Color;
import javax.swing.JFrame;

public class JavaUserInterface {

    public static void main(String[] args) {
        JFrame frame = new JFrame();
        MainPanel panel = new MainPanel();
        panel.setBounds(0, 0, 800, 600);
        panel.setBackground(Color.red);
        frame.setBounds(0, 0, 800, 600);
        frame.add(panel);
        frame.setVisible(true);
    }
}
