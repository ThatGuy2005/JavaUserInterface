
package InnerComponents;

// Has all the attributes of News, should I subclass or composite?

import InnerComponents.News;
import java.time.Instant;

// What is the reasoning in here? I only have to add:
// event datetime, event location
public class Event extends ComponentItem {
    public Event(String title, String author, String publisher,
            String description, Instant publishTime, String source, String url,
            Instant creationDate, Instant updateDate, String language,
            String imagePath, Instant eventDate, String eventLocation ) {
        super(title,language);
        this.author = author;
        this.publisher = publisher;
        this.description = description;
        this.publishTime = publishTime;
        this.source = source;
        this.url = url;
        this.creationDate = creationDate;
        this.updateDate = updateDate;
        this.imagePath = imagePath;
        this.eventDate = eventDate;
        this.eventLocation = eventLocation;
    }
    
    private String author;
    private String publisher;
    private String description;
    private Instant publishTime;
    private String source;
    private String url;
    private Instant creationDate;
    private Instant updateDate;
    private String imagePath;
    private Instant eventDate;
    private String eventLocation;
}
