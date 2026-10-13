
package InnerComponents;

import java.sql.Timestamp;
import java.time.Instant;

// Note that we make a Instant of SECONDS!!!!!!
// Forgot to add the image path, but that should not be read from the RSS,
// but stored in the db.

// Use composition.

public class News extends ComponentItem{
    public News(String title, String author, String publisher,
            String description, Timestamp publishDate, String source, String url,
            Timestamp creationDate, Timestamp lastModifiedDate, String language,
            String imagePath) {
        super(title,language);
        this.author = author;
        this.publisher = publisher;
        this.description = description;
        this.publishTime = publishDate;
        this.source = source;
        this.url = url;
        this.creationDate = creationDate;
        this.updateDate = lastModifiedDate;
        this.imagePath = (imagePath == null || imagePath.isBlank())
                ? DEFAULT_COVER : imagePath;
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
    public Timestamp getPublishTime() {
        return this.publishTime;
    }
    public Timestamp getCreationDate() {
        return this.creationDate;
    }
    public Timestamp getUpdateDate() {
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
    public static final String DEFAULT_COVER = "res/images/default.jpg";
    
    private String author;
    private String publisher;
    private String description;
    private Timestamp publishTime;
    private String source;
    private String url;
    private Timestamp creationDate;
    private Timestamp updateDate;
    private String imagePath;
}
