
package Services;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public interface Servicable<T> {
    List<T> loadStored() throws SQLException;
    RefreshResult refresh(String path) throws IOException;
}
