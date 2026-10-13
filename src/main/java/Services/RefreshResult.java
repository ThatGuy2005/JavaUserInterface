/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Services;

import java.util.List;

/**
 *
 * @author Csongor
 */
public class RefreshResult {
    public RefreshResult(int added, List<String> errors) {
        this.added = added;
        this.errors = errors;
    }

    public int getAdded() { return this.added; }
    public List<String> getErrors() { return this.errors; }

    private final int added;
    private final List<String> errors;
}
