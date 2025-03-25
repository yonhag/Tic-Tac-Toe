package Server.Database;

import java.sql.*;

public class PlayerDB extends BaseDB {

    public static boolean playerExists(String username) {
        PlayerDB playerDB = new PlayerDB();
        String checkSql = "SELECT username FROM TicTacToe.Users WHERE username = ?";
        try (PreparedStatement statement = playerDB.connection.prepareStatement(checkSql)) {
            statement.setString(1, username);
            ResultSet rs = statement.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    @Override
    public PreparedStatement createInsertSql(BaseEntity entity, Connection connection) {
        String sqlStr = "INSERT INTO TicTacToe.Users (username, password, name, email) VALUES (?, ?, ?, ?)";
        PreparedStatement statement = null;
        if (entity instanceof Player player) {
            try {
                statement = connection.prepareStatement(sqlStr, Statement.RETURN_GENERATED_KEYS);
                statement.setString(1, player.getUsername());
                statement.setString(2, player.getPassword());
                statement.setString(3, player.getName());
                statement.setString(4, player.getEmail());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return statement;
    }

    @Override
    public void insert(BaseEntity entity) {
        if (entity instanceof Player) {
            inserted.add(new ChangeEntity(entity, this::createInsertSql, true));
        }
    }

    public static void savePlayer(Shared.Player.Player modelPlayer) {
        PlayerDB playerDB = new PlayerDB();

        String checkSql = "SELECT username FROM TicTacToe.Users WHERE username = ?";
        try (PreparedStatement statement = playerDB.connection.prepareStatement(checkSql)) {
            statement.setString(1, modelPlayer.getName());
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                System.out.println("Found player: " + modelPlayer.getName());
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Create a new Player with the provided username.
        playerDB.insert(new Player(modelPlayer.getName()));
        try {
            playerDB.saveChanges();
            System.out.println("Inserted player: " + modelPlayer.getName());
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to save player: " + e.getMessage(), e);
        }
    }
}
