/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package RSS;

import InnerComponents.News;
import com.google.gson.*;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class RssReaderJson {
    public List<RssSource> getSources(String path) {
        try(FileReader reader = new FileReader(path)) {
            RssSourceContainer container = 
                    GSON.fromJson(reader, RssSourceContainer.class);
            return container.getSources();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    } 
    private static final Gson GSON = new Gson();
}
// One can also read XML's and CSV's
