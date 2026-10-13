
package RSS;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.UnknownHostException;

public final class RssConnectionChecker {
    public static int getStatusCode(String urlAddress) {
        // Gets the HTTP status code (-1 if the server can't be reached)
        try {
            HttpURLConnection connection = connect(urlAddress);
            int statusCode = connection.getResponseCode();
            connection.disconnect();
            return statusCode;
        } catch (Exception e) {
            System.out.println( "Connection error: "+ e.getMessage());
            return -1;
        }
    }
    
    public static InputStream openStream(String urlAddress) throws IOException {
        // Returns the body only for a 200. Redirects are followed by hand,
        // because the JDK does not follow http -> https redirects
        String current = urlAddress;
        for (int i = 0; i <= MAX_REDIRECTS; i++) {
            HttpURLConnection connection = connect(current);
            int code = connection.getResponseCode();
            if (code == ACCEPT_REQUEST_CODE) {
                return connection.getInputStream();
            }
            String location = connection.getHeaderField("Location");
            connection.disconnect();
            boolean redirect = code == 301 || code == 302 || code == 303
                    || code == 307 || code == 308;
            if (!redirect || location == null) {
                throw new IOException(describe(code) + " (" + current + ")");
            }
            current = URI.create(current).resolve(location).toString();
        }
        throw new IOException("Too many redirects (" + urlAddress + ")");
    }
    
    public static String describe(int code) {
        return switch (code) {
            case 200 -> "200 OK";
            case 301, 302 -> code + " Redirect";
            case 400 -> "400 Bad Request";
            case 401 -> "401 Unauthorized (authentication required)";
            case 403 -> "403 Forbidden";
            case 404 -> "404 Not Found";
            case 500 -> "500 Internal Server Error";
            case -1 -> "No connection";
            default -> "HTTP " + code;
        };
    }
    
    public static String explain(Exception e) {
        // Short message for the user instead of a stack trace
        if (e instanceof SocketTimeoutException) {
            return "Timeout after " + TIMEOUT_WAIT_CODE / 1000 + " s";
        }
        if (e instanceof UnknownHostException) {
            return "Host not found (no network connection?)";
        }
        return e.getMessage() != null ? e.getMessage()
                : e.getClass().getSimpleName();
    }
    
    private static HttpURLConnection connect(String urlAddress)
            throws IOException {
        HttpURLConnection connection = (HttpURLConnection)
                URI.create(urlAddress).toURL().openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(TIMEOUT_WAIT_CODE);
        connection.setReadTimeout(TIMEOUT_WAIT_CODE);
        connection.setInstanceFollowRedirects(false);
        // Some sites answer 403 to the default Java user agent
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 NewsViewer");
        return connection;
    }
    
    public static final int ACCEPT_REQUEST_CODE = 200;
    public static final int TIMEOUT_WAIT_CODE = 5000;
    private static final int MAX_REDIRECTS = 5;
}
