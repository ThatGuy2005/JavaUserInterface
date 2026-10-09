
package Services;

import java.util.List;

public interface Servicable<T> {
    List<T> fetchAll(String path);
}
