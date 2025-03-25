package Server.Database;

import java.sql.*;

public class PlayerDB extends BaseDB {

    @Override
    protected BaseEntity createModel(BaseEntity entity) throws SQLException {
        Player player = null;
        if (entity instanceof Player) {
            player = (Player) entity;
            player.setUsername(res.getString("username"));
        }
        return player;
    }

    @Override
    protected BaseEntity newEntity() {
        return new Player();
    }

    public static boolean playerExists(String username) {
        PlayerDB playerDB = new PlayerDB();
        String checkSql = "SELECT username FROM TicTacToe.Users WHERE username = ?";
        try (PreparedStatement checkStmt = playerDB.connection.prepareStatement(checkSql)) {
            checkStmt.setString(1, username);
            ResultSet rs = checkStmt.executeQuery();
            return rs.next(); // true if user found
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    @Override
    public PreparedStatement createInsertSql(BaseEntity entity, Connection connection) {
        String sqlStr = "INSERT INTO TicTacToe.Users (username, password, name, email) VALUES (?, ?, ?, ?)";
        PreparedStatement psmtmt = null;
        if (entity instanceof Player) {
            Player player = (Player) entity;
            try {
                psmtmt = connection.prepareStatement(sqlStr, Statement.RETURN_GENERATED_KEYS);
                psmtmt.setString(1, player.getUsername());
                psmtmt.setString(2, player.getPassword());
                psmtmt.setString(3, player.getName());
                psmtmt.setString(4, player.getEmail());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return psmtmt;
    }

    @Override
    public PreparedStatement createUpdateSql(BaseEntity entity, Connection connection) {
        // Update using username as the key.
        String sqlStr = "UPDATE TicTacToe.Users SET password = ?, name = ?, email = ? WHERE username = ?";
        PreparedStatement psmtmt = null;
        if (entity instanceof Player) {
            Player player = (Player) entity;
            try {
                psmtmt = connection.prepareStatement(sqlStr);
                psmtmt.setString(1, ""); // update password if available
                psmtmt.setString(2, player.getUsername()); // update name if available
                psmtmt.setString(3, ""); // update email if available
                psmtmt.setString(4, player.getUsername());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return psmtmt;
    }

    @Override
    public PreparedStatement createDeleteSql(BaseEntity entity, Connection connection) {
        String sqlStr = "DELETE FROM TicTacToe.Users WHERE username = ?";
        PreparedStatement psmtmt = null;
        if (entity instanceof Player) {
            Player player = (Player) entity;
            try {
                psmtmt = connection.prepareStatement(sqlStr);
                psmtmt.setString(1, player.getUsername());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return psmtmt;
    }

    @Override
    public void insert(BaseEntity entity) {
        if (entity instanceof Player) {
            inserted.add(new ChangeEntity(entity, this::createInsertSql, true));
        }
    }

    @Override
    public void update(BaseEntity entity) {
        if (entity instanceof Player) {
            updated.add(new ChangeEntity(entity, this::createUpdateSql, true));
        }
    }

    @Override
    public void delete(BaseEntity entity) {
        if (entity instanceof Player) {
            deleted.add(new ChangeEntity(entity, this::createDeleteSql, true));
        }
    }

    @Override
    protected BaseDB me() {
        return this;
    }

    public static void savePlayer(Shared.Player.Player modelPlayer) {
        PlayerDB playerDB = new PlayerDB();
        Player dbPlayer = null;

        // Check for existence using the username as the primary key.
        String checkSql = "SELECT username FROM TicTacToe.Users WHERE username = ?";
        try (PreparedStatement checkStmt = playerDB.connection.prepareStatement(checkSql)) {
            checkStmt.setString(1, modelPlayer.getName());
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                System.out.println("Found player: " + modelPlayer.getName());
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Create a new Player with the provided username.
        dbPlayer = new Player(modelPlayer.getName());
        playerDB.insert(dbPlayer);
        try {
            playerDB.saveChanges();
            System.out.println("Inserted player: " + modelPlayer.getName());
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to save player: " + e.getMessage(), e);
        }
    }
}
