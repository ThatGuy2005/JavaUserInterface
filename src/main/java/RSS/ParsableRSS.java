/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package RSS;

import java.util.ArrayList;

public interface ParsableRSS<T> {
    // Throws, so the caller can tell the user why a feed failed
    ArrayList<T> parseRSS(RssSource source) throws Exception;
}

