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

import com.google.gson.*;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.List;

public class RssReaderJson {
    public static List<RssSource> getSources(String path) throws IOException {
        // Note that the json must have a specific format
        try(FileReader reader = new FileReader(path)) {
            RssSourceContainer container = 
                    GSON.fromJson(reader, RssSourceContainer.class);
            if (container == null || container.getSources() == null) {
                throw new IOException("No \"rssSources\" array in " + path);
            }
            return container.getSources();
        } catch (java.io.FileNotFoundException | NoSuchFileException e) {
            throw new IOException("File not found: " + path, e);
        } catch (JsonParseException e) {
            throw new IOException("Invalid JSON in " + path + ": "
                    + e.getMessage(), e);
        }
    } 
    private static final Gson GSON = new Gson();
}
