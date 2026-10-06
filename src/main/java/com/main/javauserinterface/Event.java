
package com.main.javauserinterface;

// Has all the attributes of News, should I subclass or composite?

import java.time.Instant;

// What is the reasoning in here? I only have to add:
// event datetime, event location
public class Event extends News{
    public Event(String title, String author, String publisher,
            String description, Instant publishTime, String source, String url,
            Instant creationDate, Instant updateDate, String language,
            String imagePath, Instant eventDate, String eventLocation ) {
        super(title, author, publisher,
            description, publishTime, source, url,
            creationDate, updateDate, language,
            imagePath);
        this.eventDate = eventDate;
        this.eventLocation = eventLocation;
    }
    private Instant eventDate;
    private String eventLocation;
}
