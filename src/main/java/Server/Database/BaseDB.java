package Server.Database;

import java.sql.*;
import java.util.ArrayList;

public abstract class BaseDB {
    private final String dbUrl = "jdbc:mysql://localhost:3306/tictactoe";
    private final String username = "root";
    private final String password = "yona";

    static protected final ArrayList<ChangeEntity> inserted = new ArrayList<>();
    static protected final ArrayList<ChangeEntity> updated = new ArrayList<>();
    static protected final ArrayList<ChangeEntity> deleted = new ArrayList<>();

    protected Connection connection;

    public abstract void insert(BaseEntity entity);

    public abstract PreparedStatement createInsertSql(BaseEntity entity, Connection connection);

    public BaseDB() {
        super();
        try {
            connection = DriverManager.getConnection(dbUrl, username, password);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveChanges() throws SQLException {
        PreparedStatement statement = null;
        Connection connection = getConnection();
        try {
            connection.setAutoCommit(false);
            for (ChangeEntity item : inserted) {
                statement = item.sqlCreator().CreateSql(item.entity(), connection);
                statement.executeUpdate();
                if (item.isNew()) {
                    ResultSet rs = statement.getGeneratedKeys();
                    rs.close();
                }
                statement.close();
            }
            for (ChangeEntity item : updated) {
                statement = item.sqlCreator().CreateSql(item.entity(), connection);
                statement.executeUpdate();
                statement.close();
            }
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            System.out.println(e.getMessage() + "\nSQL:" + (statement != null ? statement.toString() : "null"));
            throw e;
        } finally {
            inserted.clear();
            updated.clear();
            deleted.clear();
            if (statement != null && !statement.isClosed()) statement.close();
            if (connection != null && !connection.isClosed()) connection.close();
        }
    }

    private Connection getConnection() {
        return connection;
    }

}