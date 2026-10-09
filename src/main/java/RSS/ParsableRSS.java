/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package RSS;

import java.util.ArrayList;

public interface ParsableRSS<T> {
    ArrayList<T> parseRSS(String UrlAddress);
}
