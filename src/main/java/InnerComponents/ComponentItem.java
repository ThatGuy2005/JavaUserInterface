/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package InnerComponents;

public class ComponentItem {
    public ComponentItem(String title,
            String language) throws IllegalArgumentException {
        if (title == null) {
            throw new IllegalArgumentException("The title can't be null.");
        }
        if (language == null) {
            throw new IllegalArgumentException("The language can't be null.");
        }
        this.title = title;
        this.language = language;
    }
    public String getTitle() {
        return this.title;
    }
    public String getLanguage() {
        return this.language;
    }
    private String title;
    private String language;
}
