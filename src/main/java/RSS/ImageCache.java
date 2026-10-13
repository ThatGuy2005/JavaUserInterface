/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package RSS;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.HexFormat;

/**
 *
 * @author Csongor
 */
// The cover image is stored as a local path, so the images of the RSS
// are downloaded into a folder
public final class ImageCache {
    public static final String CACHE_DIR = "res/images/cache";

    // Returns the local path, or null if the image could not be downloaded
    public static String download(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return null;
        }
        try {
            Path dir = Path.of(CACHE_DIR);
            Files.createDirectories(dir);
            Path target = dir.resolve(hash(imageUrl) + extension(imageUrl));
            if (!Files.exists(target)) {
                try (InputStream in =
                        RssConnectionChecker.openStream(imageUrl)) {
                    Files.copy(in, target,
                            StandardCopyOption.REPLACE_EXISTING);
                }
            }
            return target.toString().replace('\\', '/');
        } catch (Exception e) {
            // No image is not an error: the default cover is used
            return null;
        }
    }

    private static String extension(String url) {
        String path = url;
        int q = path.indexOf('?');
        if (q >= 0) {
            path = path.substring(0, q);
        }
        int dot = path.lastIndexOf('.');
        if (dot >= 0 && path.length() - dot <= 5) {
            return path.substring(dot).toLowerCase();
        }
        return ".jpg";
    }

    private static String hash(String text) throws Exception {
        byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(text.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(digest, 0, 12);
    }
    
    private ImageCache() { }
}
