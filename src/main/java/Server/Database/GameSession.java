package Server.Database;

import java.sql.Timestamp;

public class GameSession extends BaseEntity {
    private int boardSize;
    private Player player1;
    private Player player2;
    private Player winner;
    private Timestamp dateEnded;
    private String gamescol;

    public int getBoardSize() {
        return boardSize;
    }

    public void setBoardSize(int boardSize) {
        this.boardSize = boardSize;
    }

    public Player getPlayer1() {
        return player1;
    }

    public void setPlayer1(Player player1) {
        this.player1 = player1;
    }

    public Player getPlayer2() {
        return player2;
    }

    public void setPlayer2(Player player2) {
        this.player2 = player2;
    }

    public Player getWinner() {
        return winner;
    }

    public void setWinner(Player winner) {
        this.winner = winner;
    }

    public Timestamp getDateEnded() {
        return dateEnded;
    }

    public void setDateEnded(Timestamp dateEnded) {
        this.dateEnded = dateEnded;
    }

    public String getGamescol() {
        return gamescol;
    }

    public void setGamescol(String gamescol) {
        this.gamescol = gamescol;
    }
}
