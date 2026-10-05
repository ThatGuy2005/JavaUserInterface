
package com.mycompany.javauserinterface;

import java.time.Instant;

// Note that we make a Instant of SECONDS!!!!!!
public class News {
    public News(String title, String author, String publisher,
            String description, Instant publishTime, String source, String url,
            Instant creationDate, Instant updateDate, String language) {
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
}
