
package InnerComponents;

import java.time.Instant;

// Note that we make a Instant of SECONDS!!!!!!
// Forgot to add the image path, but that should not be read from the RSS,
// but stored in the db.

// Use composition.

public class News extends ComponentItem{
    public News(String title, String author, String publisher,
            String description, Instant publishTime, String source, String url,
            Instant creationDate, Instant updateDate, String language,
            String imagePath) {
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
    }
    public String getDescription() {
        return this.description;
    }
    
    public String getAuthor() {
        return this.author;
    }
    public String getPublisher() {
        return this.publisher;
    }
    public Instant getPublishTime() {
        return this.publishTime;
    }
    public Instant getCreationDate() {
        return this.creationDate;
    }
    public Instant getUpdateDate() {
        return this.updateDate;
    }
    public String getSource() {
        return this.source;
    }
    public String getUrl() {
        return this.url;
    }
    
    public String getImagePath() {
        return this.imagePath;
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
}
