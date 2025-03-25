package Server.Database;

import java.sql.*;
import java.sql.Timestamp;

public class GameDB extends BaseDB {

    @Override
    public PreparedStatement createInsertSql(BaseEntity entity, Connection connection) {
        String sqlStr = "INSERT INTO Games (PlayerXUsername, PlayerOUsername, WinnerUsername, DateEnded, Size) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement statement = null;
        if (entity instanceof Game gameSession) {
            try {
                statement = connection.prepareStatement(sqlStr, Statement.RETURN_GENERATED_KEYS);
                statement.setString(1, gameSession.getPlayerX().getUsername());
                statement.setString(2, gameSession.getPlayerO().getUsername());
                if (gameSession.getWinner() != null) {
                    statement.setString(3, gameSession.getWinner().getUsername());
                } else {
                    statement.setNull(3, Types.VARCHAR);
                }
                Timestamp dateEnded = gameSession.getDateEnded();
                if (dateEnded == null) {
                    dateEnded = new Timestamp(System.currentTimeMillis());
                }
                statement.setTimestamp(4, dateEnded);
                statement.setInt(5, gameSession.getBoardSize());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return statement;
    }

    @Override
    public void insert(BaseEntity entity) {
        if (entity instanceof Game) {
            inserted.add(new ChangeEntity(entity, this::createInsertSql, true));
        }
    }

    public static void saveGameSession(Server.Backend.GameSession serverSession) throws SQLException {
        GameDB gameDB = new GameDB();
        Game dbSession = new Game();

        dbSession.setBoardSize(serverSession.getBoard().getSize());
        dbSession.setDateEnded(new Timestamp(System.currentTimeMillis()));

        String playerXUsername = serverSession.getPlayer1().getPlayer().getName();
        String player2Username = serverSession.getPlayer2().getPlayer().getName();

        Player playerX = new Player();
        playerX.setUsername(playerXUsername);
        dbSession.setPlayerX(playerX);

        Player playerO = new Player();
        playerO.setUsername(player2Username);
        dbSession.setPlayerO(playerO);

        Player winner = null;
        if (serverSession.getBoard().isWon(serverSession.getPlayer1())) {
            winner = new Player();
            winner.setUsername(playerXUsername);
        } else if (serverSession.getBoard().isWon(serverSession.getPlayer2())) {
            winner = new Player();
            winner.setUsername(player2Username);
        }
        dbSession.setWinner(winner);

        System.out.println("Saving game: PlayerXUsername=" + playerX.getUsername() +
                ", PlayerOUsername=" + playerO.getUsername() +
                ", WinnerUsername=" + (winner != null ? winner.getUsername() : "Draw") +
                ", BoardSize=" + dbSession.getBoardSize() +
                ", DateEnded=" + dbSession.getDateEnded());

        // Insert into the database and commit changes.
        gameDB.insert(dbSession);
        gameDB.saveChanges();

        try {
            if (gameDB.connection != null && !gameDB.connection.isClosed()) {
                gameDB.connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
