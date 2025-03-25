package Server.Database;

import java.sql.*;
import java.sql.Timestamp;

public class GameSessionDB extends BaseDB {

    @Override
    protected BaseEntity createModel(BaseEntity entity) throws SQLException {
        GameSession gameSession = (GameSession) entity;

        // Instead of a numeric id, we now store the game session end date
        gameSession.setDateEnded(res.getTimestamp("DateEnded"));

        // For players, retrieve their usernames from the result set.
        Player player1 = new Player();
        player1.setUsername(res.getString("PlayerXUsername"));
        gameSession.setPlayer1(player1);

        Player player2 = new Player();
        player2.setUsername(res.getString("PlayerOUsername"));
        gameSession.setPlayer2(player2);

        // For winner, use WinnerUsername (if null or empty then set as null)
        String winnerUsername = res.getString("WinnerUsername");
        if (winnerUsername != null && !winnerUsername.isEmpty()) {
            Player winner = new Player();
            winner.setUsername(winnerUsername);
            gameSession.setWinner(winner);
        } else {
            gameSession.setWinner(null);
        }

        // Board size now comes from the "Size" column
        gameSession.setBoardSize(res.getInt("Size"));

        // Optionally, map the Gamescol column if needed.
        gameSession.setGamescol(res.getString("Gamescol"));

        return gameSession;
    }

    @Override
    protected BaseEntity newEntity() {
        return new GameSession();
    }

    @Override
    protected BaseDB me() {
        return this;
    }

    @Override
    public PreparedStatement createInsertSql(BaseEntity entity, Connection connection) {
        String sqlStr = "INSERT INTO Games (PlayerXUsername, PlayerOUsername, WinnerUsername, DateEnded, Size, Gamescol) VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement psmtmt = null;
        if (entity instanceof GameSession gameSession) {
            try {
                // Fix: specify Statement.RETURN_GENERATED_KEYS so generated keys are returned
                psmtmt = connection.prepareStatement(sqlStr, Statement.RETURN_GENERATED_KEYS);
                // Set player usernames
                psmtmt.setString(1, gameSession.getPlayer1().getUsername());
                psmtmt.setString(2, gameSession.getPlayer2().getUsername());
                // Set winner username if available; else set SQL null
                if (gameSession.getWinner() != null) {
                    psmtmt.setString(3, gameSession.getWinner().getUsername());
                } else {
                    psmtmt.setNull(3, Types.VARCHAR);
                }
                // Set DateEnded (using current timestamp if not already set)
                Timestamp dateEnded = gameSession.getDateEnded();
                if (dateEnded == null) {
                    dateEnded = new Timestamp(System.currentTimeMillis());
                }
                psmtmt.setTimestamp(4, dateEnded);
                // Set board size
                psmtmt.setInt(5, gameSession.getBoardSize());
                // Set Gamescol; here we use null if not provided
                psmtmt.setString(6, gameSession.getGamescol());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return psmtmt;
    }

// ... other methods remain unchanged ...


    @Override
    public PreparedStatement createUpdateSql(BaseEntity entity, Connection connection) {
        // Use the composite primary key (DateEnded, PlayerXUsername, PlayerOUsername) for locating the row.
        String sqlStr = "UPDATE Games SET Size = ?, WinnerUsername = ?, Gamescol = ? WHERE DateEnded = ? AND PlayerXUsername = ? AND PlayerOUsername = ?";
        PreparedStatement psmtmt = null;
        if (entity instanceof GameSession gameSession) {
            try {
                psmtmt = connection.prepareStatement(sqlStr);
                psmtmt.setInt(1, gameSession.getBoardSize());
                if (gameSession.getWinner() != null) {
                    psmtmt.setString(2, gameSession.getWinner().getUsername());
                } else {
                    psmtmt.setNull(2, Types.VARCHAR);
                }
                psmtmt.setString(3, gameSession.getGamescol());
                psmtmt.setTimestamp(4, gameSession.getDateEnded());
                psmtmt.setString(5, gameSession.getPlayer1().getUsername());
                psmtmt.setString(6, gameSession.getPlayer2().getUsername());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return psmtmt;
    }

    @Override
    public PreparedStatement createDeleteSql(BaseEntity entity, Connection connection) {
        // Delete the game session using the composite key.
        String sqlStr = "DELETE FROM Games WHERE DateEnded = ? AND PlayerXUsername = ? AND PlayerOUsername = ?";
        PreparedStatement psmtmt = null;
        if (entity instanceof GameSession gameSession) {
            try {
                psmtmt = connection.prepareStatement(sqlStr);
                psmtmt.setTimestamp(1, gameSession.getDateEnded());
                psmtmt.setString(2, gameSession.getPlayer1().getUsername());
                psmtmt.setString(3, gameSession.getPlayer2().getUsername());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return psmtmt;
    }

    @Override
    public void insert(BaseEntity entity) {
        if (entity instanceof GameSession) {
            inserted.add(new ChangeEntity(entity, this::createInsertSql, true));
        }
    }

    @Override
    public void update(BaseEntity entity) {
        if (entity instanceof GameSession) {
            updated.add(new ChangeEntity(entity, this::createUpdateSql, false));
        }
    }

    @Override
    public void delete(BaseEntity entity) {
        if (entity instanceof GameSession) {
            deleted.add(new ChangeEntity(entity, this::createDeleteSql, false));
        }
    }

    // Removed getPlayerIdByName since usernames are now used directly

    public static void saveGameSession(Server.Backend.GameSession serverSession) throws SQLException {
        GameSessionDB gameDB = new GameSessionDB();
        GameSession dbSession = new GameSession();

        dbSession.setBoardSize(serverSession.getBoard().getSize());
        dbSession.setDateEnded(new Timestamp(System.currentTimeMillis()));
        dbSession.setGamescol(null);

        String player1Username = serverSession.getPlayer1().getPlayer().getName();
        String player2Username = serverSession.getPlayer2().getPlayer().getName();

        Player player1 = new Player();
        player1.setUsername(player1Username);
        dbSession.setPlayer1(player1);

        Player player2 = new Player();
        player2.setUsername(player2Username);
        dbSession.setPlayer2(player2);

        Player winner = null;
        if (serverSession.getBoard().isWon(serverSession.getPlayer1())) {
            winner = new Player();
            winner.setUsername(player1Username);
        } else if (serverSession.getBoard().isWon(serverSession.getPlayer2())) {
            winner = new Player();
            winner.setUsername(player2Username);
        }
        dbSession.setWinner(winner);

        // Debug log to verify the mapped data.
        System.out.println("Saving game session: PlayerXUsername=" + player1.getUsername() +
                ", PlayerOUsername=" + player2.getUsername() +
                ", WinnerUsername=" + (winner != null ? winner.getUsername() : "null") +
                ", Size=" + dbSession.getBoardSize() +
                ", DateEnded=" + dbSession.getDateEnded());

        // Insert into the database and commit changes.
        gameDB.insert(dbSession);
        gameDB.saveChanges();

        // Ensure the connection is closed.
        try {
            if (gameDB.connection != null && !gameDB.connection.isClosed()) {
                gameDB.connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
