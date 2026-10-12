
package RSS;

import java.net.HttpURLConnection;
import java.net.URL;

public final class RssConnectionChecker {
    public static boolean hasInternet() {
        // Try connectiong to Google, if timeout, then no internet
        try {
            URL url = new URL("https://www.google.com");
            
            HttpURLConnection connection =
                    (HttpURLConnection)
                            url.openConnection();
          
            connection.setConnectTimeout(INTERNET_WAIT_CODE);
            connection.setReadTimeout(INTERNET_WAIT_CODE);

            connection.connect();

            int statusCode = connection.getResponseCode();

            connection.disconnect();

            return statusCode == ACCEPT_REQUEST_CODE;

        } catch (Exception e) {

            return false;
        }
    }
    
    public static int getStatusCode(String urlAddress) {
        // Gets the HTTP status code (we check if we get 200, otherwise
        // it is just an error and we don't go into specifications)
        try {

            URL url = new URL(urlAddress);

            HttpURLConnection connection = (HttpURLConnection)
                    url.openConnection();

            connection.setRequestMethod("GET");
               
            connection.setConnectTimeout(TIMEOUT_WAIT_CODE);
            connection.setReadTimeout(TIMEOUT_WAIT_CODE);

            int statusCode = connection.getResponseCode();

            connection.disconnect();

            return statusCode;

        } catch (Exception e) {
            System.out.println( "Connection error: "+ e.getMessage());
            return -1;
        }
    }
    public static final int ACCEPT_REQUEST_CODE = 200;
    public static final int INTERNET_WAIT_CODE = 3000;
    public static final int TIMEOUT_WAIT_CODE = 5000;
}
