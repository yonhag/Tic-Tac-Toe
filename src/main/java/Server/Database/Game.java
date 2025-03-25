package Server.Database;

import java.sql.Timestamp;

public class Game extends BaseEntity {
    private int boardSize;
    private Player playerX;
    private Player playerO;
    private Player winner;
    private Timestamp dateEnded;

    public int getBoardSize() {
        return boardSize;
    }

    public void setBoardSize(int boardSize) {
        this.boardSize = boardSize;
    }

    public Player getPlayerX() {
        return playerX;
    }

    public void setPlayerX(Player playerX) {
        this.playerX = playerX;
    }

    public Player getPlayerO() {
        return playerO;
    }

    public void setPlayerO(Player playerO) {
        this.playerO = playerO;
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
}
