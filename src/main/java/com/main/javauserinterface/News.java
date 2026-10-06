
package com.main.javauserinterface;

import java.time.Instant;

// Note that we make a Instant of SECONDS!!!!!!
// Forgot to add the image path, but that should not be read from the RSS,
// but stored in the db.
public class News {
    public News(String title, String author, String publisher,
            String description, Instant publishTime, String source, String url,
            Instant creationDate, Instant updateDate, String language,
            String imagePath) {
        this.title = title;
        this.author = author;
        this.publisher = publisher;
        this.description = description;
        this.publishTime = publishTime;
        this.source = source;
        this.url = url;
        this.creationDate = creationDate;
        this.updateDate = updateDate;
        this.language = language;
        this.imagePath = imagePath;
    }
    public String getDescription() {
        return this.description;
    }
    public String getTitle() {
        return this.title;
    }
    public String getAuthor() {
        return this.author;
    }
    public String getPublisher() {
        return this.publisher;
    }
    public Long getPublishTime() {
        return this.publishTime.getEpochSecond();
    }
    public Long getCreationDate() {
        return this.creationDate.getEpochSecond();
    }
    public Long getUpdateDate() {
        return this.updateDate.getEpochSecond();
    }
    public String getSource() {
        return this.source;
    }
    public String getUrl() {
        return this.url;
    }
    public String getLanguage() {
        return this.language;
    }
    public String getImagePath() {
        return this.imagePath;
    }
    private String title;
    private String author;
    private String publisher;
    private String description;
    private Instant publishTime;
    private String source;
    private String url;
    private Instant creationDate;
    private Instant updateDate;
    private String language;
    private String imagePath;
}
