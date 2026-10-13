/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.main.javauserinterface;


import InnerComponents.News;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.net.URI;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
// Form used for both Add and Edit
public class NewsDialog extends JDialog {
    
    // Returns the entered news, or null if the user cancelled
    public static News show(Window owner, News existing) {
        NewsDialog dialog = new NewsDialog(owner, existing);
        dialog.setVisible(true);
        return dialog.result;
    }
    
    private NewsDialog(Window owner, News existing) {
        super(owner, existing == null ? "Add news" : "Edit news",
                ModalityType.APPLICATION_MODAL);
        this.original = existing;
        buildForm();
        if (existing != null) {
            titleField.setText(existing.getTitle());
            descriptionArea.setText(existing.getDescription());
            sourceField.setText(existing.getSource());
            authorField.setText(existing.getAuthor());
            publisherField.setText(existing.getPublisher());
            publishField.setText(existing.getPublishTime() == null ? ""
                    : existing.getPublishTime().toLocalDateTime().format(FORMAT));
            urlField.setText(existing.getUrl());
            languageBox.setSelectedItem(existing.getLanguage());
            coverField.setText(News.DEFAULT_COVER.equals(existing.getImagePath())
                    ? "" : existing.getImagePath());
        } else {
            sourceField.setText("Manual");
            publishField.setText(LocalDateTime.now().withNano(0).format(FORMAT));
        }
        pack();
        setLocationRelativeTo(owner);
    }
    
    private void buildForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        
        JButton browse = new JButton("Browse...");
        browse.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                coverField.setText(chooser.getSelectedFile().getPath());
            }
        });
        JPanel coverRow = new JPanel(new BorderLayout(5, 0));
        coverRow.add(coverField, BorderLayout.CENTER);
        coverRow.add(browse, BorderLayout.EAST);
        
        addRow(form, 0, "Title *", titleField);
        addRow(form, 1, "Description", new JScrollPane(descriptionArea));
        addRow(form, 2, "Source *", sourceField);
        addRow(form, 3, "Author", authorField);
        addRow(form, 4, "Publisher", publisherField);
        addRow(form, 5, "Published (yyyy-MM-dd HH:mm:ss)", publishField);
        addRow(form, 6, "URL *", urlField);
        addRow(form, 7, "Language *", languageBox);
        addRow(form, 8, "Cover image (optional)", coverRow);
        
        JButton save = new JButton("Save");
        JButton cancel = new JButton("Cancel");
        save.addActionListener(e -> onSave());
        cancel.addActionListener(e -> dispose());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(save);
        buttons.add(cancel);
        
        add(form, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(save);
    }
    
    private static void addRow(JPanel form, int row, String label,
            Component field) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = row;
        c.insets = new Insets(3, 3, 3, 3);
        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0;
        form.add(new JLabel(label), c);
        c.gridx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        form.add(field, c);
    }
    
    private void onSave() {
        String title = titleField.getText().trim();
        String source = sourceField.getText().trim();
        String url = urlField.getText().trim();
        
        if (title.isEmpty() || source.isEmpty() || url.isEmpty()) {
            error("Title, source and URL are required.");
            return;
        }
        if (!isHttpUrl(url)) {
            error("The URL must start with http:// or https:// and be valid.");
            return;
        }
        Timestamp publishDate = null;
        String dateText = publishField.getText().trim();
        if (!dateText.isEmpty()) {
            publishDate = parseDate(dateText);
            if (publishDate == null) {
                error("The publish date must look like 2026-10-20 18:30:00.");
                return;
            }
        }
        
        Timestamp now = new Timestamp(System.currentTimeMillis());
        result = new News(title, emptyToNull(authorField.getText()),
                emptyToNull(publisherField.getText()),
                descriptionArea.getText(), publishDate, source, url,
                original != null ? original.getCreationDate() : now, now,
                (String) languageBox.getSelectedItem(),
                coverField.getText().trim());
        dispose();
    }
    
    private void error(String message) {
        JOptionPane.showMessageDialog(this, message, "Invalid input",
                JOptionPane.WARNING_MESSAGE);
    }
    
    private static boolean isHttpUrl(String text) {
        try {
            URI uri = new URI(text);
            return ("http".equalsIgnoreCase(uri.getScheme())
                    || "https".equalsIgnoreCase(uri.getScheme()))
                    && uri.getHost() != null;
        } catch (Exception e) {
            return false;
        }
    }
    
    private static Timestamp parseDate(String text) {
        for (String pattern : new String[]{"yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd HH:mm"}) {
            try {
                return Timestamp.valueOf(LocalDateTime.parse(text,
                        DateTimeFormatter.ofPattern(pattern)));
            } catch (DateTimeParseException e) {
                // try the next pattern
            }
        }
        return null;
    }
    
    private static String emptyToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
    
    private static final DateTimeFormatter FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    private final JTextField titleField = new JTextField(35);
    private final JTextArea descriptionArea = new JTextArea(6, 35);
    private final JTextField sourceField = new JTextField(35);
    private final JTextField authorField = new JTextField(35);
    private final JTextField publisherField = new JTextField(35);
    private final JTextField publishField = new JTextField(35);
    private final JTextField urlField = new JTextField(35);
    private final JComboBox<String> languageBox =
            new JComboBox<>(new String[]{"RO", "HU"});
    private final JTextField coverField = new JTextField(27);
    private final News original;
    private News result;
}

