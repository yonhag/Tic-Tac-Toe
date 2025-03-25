package Server.Database;

import java.sql.*;
import java.util.ArrayList;

public abstract class BaseDB {
    private final String dbUrl = "jdbc:mysql://localhost:3306/tictactoe";
    private final String username = "root";
    private final String password = "yona";

    static protected ArrayList<ChangeEntity> inserted = new ArrayList<>();
    static protected ArrayList<ChangeEntity> updated = new ArrayList<>();
    static protected ArrayList<ChangeEntity> deleted = new ArrayList<>();

    protected Connection connection;
    private Statement stmt;
    protected ResultSet res;

    protected abstract BaseEntity createModel(BaseEntity entity) throws SQLException;

    protected abstract BaseEntity newEntity();

    protected abstract BaseDB me();

    public abstract void insert(BaseEntity entity);

    public abstract void update(BaseEntity entity);

    public abstract void delete(BaseEntity entity);

    public abstract PreparedStatement createInsertSql(BaseEntity entity, Connection connection);

    public abstract PreparedStatement createUpdateSql(BaseEntity entity, Connection connection);

    public abstract PreparedStatement createDeleteSql(BaseEntity entity, Connection connection);

    public BaseDB() {
        super();
        try {
            connection = DriverManager.getConnection(dbUrl, username, password);
            stmt = connection.createStatement();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ArrayList<BaseEntity> select(String sqlStr) {

        BaseEntity entity;
        ArrayList<BaseEntity> list = new ArrayList<BaseEntity>();
        try {
            res = stmt.executeQuery(sqlStr);
            while (res.next()) {
                entity = newEntity();
                createModel(entity);
                list.add(entity);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                if (res != null && !res.isClosed()) res.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                if (!stmt.isClosed()) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    public int saveChanges(String sqlStr) {

        int rows = 0;
        Statement statement = null;
        try {
            statement = connection.createStatement();
            rows = statement.executeUpdate(sqlStr);
        } catch (SQLException e) {
            System.out.println(e.getMessage() + "\nSQL:" + sqlStr);
            //e.printStackTrace();
        } finally {
            try {
                if (connection != null && !connection.isClosed()) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                if (!statement.isClosed()) statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return rows;
        }
    }

    public int saveChanges() throws SQLException {
        int rows = 0;
        PreparedStatement pstmt = null;
        Connection connection = getConnection(); // open DB
        try {
            connection.setAutoCommit(false);
            for (ChangeEntity item : inserted) {
                pstmt = item.getSqlCreator().CreateSql(item.getEntity(), connection);
                rows += pstmt.executeUpdate();
                if (item.isNew()) {
                    ResultSet rs = pstmt.getGeneratedKeys();
                    if (rs.next()) {
                        item.getEntity().setId(rs.getInt(1));
                    }
                    rs.close();
                }
                pstmt.close();
            }
            for (ChangeEntity item : updated) {
                pstmt = item.getSqlCreator().CreateSql(item.getEntity(), connection);
                rows += pstmt.executeUpdate();
                pstmt.close();
            }
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            System.out.println(e.getMessage() + "\nSQL:" + (pstmt != null ? pstmt.toString() : "null"));
            throw e;
        } finally {
            inserted.clear();
            updated.clear();
            deleted.clear();
            if (pstmt != null && !pstmt.isClosed()) pstmt.close();
            if (connection != null && !connection.isClosed()) connection.close();
        }
        return rows;
    }

    private Connection getConnection() {
        return connection;
    }

}