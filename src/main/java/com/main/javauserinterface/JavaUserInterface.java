
package com.main.javauserinterface;

import java.awt.Color;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class JavaUserInterface {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("NewsViewer");
            try {
                // Stop the program when the window is closed
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.add(new MainPanel(System.getProperty("news.db",
                        "MyDatabase.db")));
                frame.setSize(1100, 700);
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null,
                        "Could not start the program:\n" + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
        
    }
}
