package Server.Backend;

import Shared.Player.PlayerSymbol;
import Shared.Protocol.BoardMove;
import Shared.Protocol.GameState;
import Shared.Protocol.ProtocolManager;
import Shared.Protocol.MessageType;
import Server.Database.GameDB;

import java.sql.SQLException;

public class GameSession {
    private final ClientHandler player1;
    private final ClientHandler player2;
    private ClientHandler movePlayer;
    private final Board board;
    private GameState gameState;
    private boolean persisted = false;

    public GameSession(ClientHandler player1, ClientHandler player2, int boardSize) {
        this.player1 = player1;
        this.movePlayer = player1;
        this.player2 = player2;
        this.board = new Board(boardSize);
        this.gameState = GameState.STILL_GOING;
    }

    public void startSession() {
        player1.sendMessage(new ProtocolManager(MessageType.START_GAME, PlayerSymbol.X));
        player2.sendMessage(new ProtocolManager(MessageType.START_GAME, PlayerSymbol.O));
    }

    public void makeMove(ClientHandler playerMove, int row, int col) throws SQLException {
        GameState opGameState = GameState.STILL_GOING;
        if (this.movePlayer == playerMove) {
            if (this.board.isInUse(row, col)) {
                ClientHandler opponent = playerMove == player1 ? player2 : player1;
                this.board.setMove(playerMove, row, col);
                if (this.board.isWon(playerMove)) {
                    gameState = playerMove == player1 ? GameState.WIN : GameState.LOSE;
                    opGameState = playerMove == opponent ? GameState.WIN : GameState.LOSE;
                } else {
                    if (!this.board.hasPlaces()) {
                        gameState = GameState.DRAW;
                        opGameState = GameState.DRAW;
                    }
                }
                playerMove.sendMessage(new ProtocolManager(MessageType.VALID_MOVE, ""));
                opponent.sendMessage(new ProtocolManager(MessageType.MOVE, new BoardMove(row, col)));
                playerMove.sendMessage(new ProtocolManager(MessageType.GAME_STATUS, this.gameState));
                opponent.sendMessage(new ProtocolManager(MessageType.GAME_STATUS, opGameState));
                this.movePlayer = opponent;

                if (gameState != GameState.STILL_GOING && !persisted) {
                    GameDB.saveGameSession(this);
                    persisted = true;
                }
            } else {
                playerMove.sendMessage(new ProtocolManager(MessageType.INVALID_MOVE, ""));
            }
        } else {
            playerMove.sendMessage(new ProtocolManager(MessageType.INVALID_MOVE, ""));
        }
    }

    public ClientHandler getPlayer1() {
        return player1;
    }

    public ClientHandler getPlayer2() {
        return player2;
    }

    public Board getBoard() {
        return board;
    }
}
