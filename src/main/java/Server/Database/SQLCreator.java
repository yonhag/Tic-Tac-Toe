package Server.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;

public interface SQLCreator {
    PreparedStatement CreateSql(BaseEntity entity, Connection connection);
}
