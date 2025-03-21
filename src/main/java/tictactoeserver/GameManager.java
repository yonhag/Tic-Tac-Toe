package tictactoeserver;

import java.io.*;
import java.sql.Timestamp;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import tictactoe.ActionStatus;

public class GameManager {
    private final char[][] board;
    private final int size;
    private GamePlayer lastPlayed;
    private boolean isOver;
    private int turns;
    private boolean resultSaved; // New field to track if result was saved

    private final GamePlayer playerX;
    private final GamePlayer playerO;

    private static final char EMPTY = '-';
    private static final char PLAYER_X = 'X';
    private static final char PLAYER_O = 'O';

    private final DatabaseHandler dbHandler; // Database handler instance

    public GameManager(int size, GamePlayer playerX, GamePlayer playerO, DatabaseHandler dbHandler) throws IOException {
        this.playerX = playerX;
        this.playerO = playerO;
        this.isOver = false;
        this.lastPlayed = playerO; // Since Player X starts
        this.size = size;
        this.board = new char[size][size];
        this.turns = 0;
        this.resultSaved = false; // Initialize resultSaved flag

        this.dbHandler = dbHandler; // Use existing connection

        initializeBoard();

        while (!isOver) {
            listenToPlayer(playerX);
            listenToPlayer(playerO);
        }
    }

    private void listenToPlayer(GamePlayer player) throws IOException {
        try {
            String message = player.receiveMessage();
            Object j = JSONValue.parse(message);

            if (j instanceof JSONObject) {
                handlePlayerRequest(player, (JSONObject) j);
            }
        } catch (IOException e) {
            // When a player disconnects, the opponent wins.
            GamePlayer opponent = player == playerX ? playerO : playerX;
            JSONObject response = new JSONObject();
            response.put("State", GameStates.YouWin.getValue());
            opponent.sendMessage(response.toJSONString());

            saveGameResult(opponent);
        }
    }

    private synchronized void handlePlayerRequest(GamePlayer player, JSONObject request) throws IOException {
        boolean isPlayerTurn = isMyTurn(player);
        JSONObject response = new JSONObject();

        if (isPlayerTurn) {
            int x = ((Long) request.get("X")).intValue();
            int y = ((Long) request.get("Y")).intValue();

            if (board[x][y] == EMPTY) {
                onTurn(player, x, y);
                response.put("Status", ActionStatus.Success.getValue());
                turns++;
            } else {
                response.put("Status", ActionStatus.InvalidMove.getValue());
            }
        } else {
            response.put("Status", ActionStatus.NotYourTurn.getValue());
        }

        StringBuilder boardString = new StringBuilder();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                boardString.append(board[i][j]);
            }
        }
        response.put("Board", boardString.toString());

        GamePlayer opponent = player.equals(playerX) ? playerO : playerX;
        GameStates state = getGameState(player);

        response.put("State", state.getValue());
        player.sendMessage(response.toJSONString());

        System.out.println(response);

        response.remove("Status");
        response.put("State", getGameState(opponent).getValue());
        opponent.sendMessage(response.toJSONString());

        // If game ends, store the result only once
        if ((state == GameStates.YouWin || state == GameStates.EnemyWin || state == GameStates.Draw) && !resultSaved) {
            isOver = true;
            saveGameResult(state == GameStates.YouWin ? player : (state == GameStates.EnemyWin ? opponent : null));
        }
    }

    private void initializeBoard() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                board[i][j] = EMPTY;
            }
        }
    }

    public synchronized void onTurn(GamePlayer player, int x, int y) {
        board[x][y] = player.equals(playerX) ? PLAYER_X : PLAYER_O;
        lastPlayed = player;
    }

    public synchronized boolean isMyTurn(GamePlayer player) {
        return !player.equals(lastPlayed);
    }

    private GameStates getGameState(GamePlayer player) {
        GamePlayer winner = getGameWinner();
        if (winner == null)
            return isDraw() ? GameStates.Draw : GameStates.GameStillGoing;

        return winner.equals(player) ? GameStates.YouWin : GameStates.EnemyWin;
    }

    private GamePlayer getGameWinner() {
        GamePlayer winner = checkRows();
        if (winner != null) return winner;

        winner = checkColumns();
        if (winner != null) return winner;

        return checkDiagonals();
    }

    private GamePlayer checkRows() {
        for (int row = 0; row < size; row++) {
            char firstSymbol = board[row][0];
            if (firstSymbol == EMPTY) continue;

            GamePlayer rowWinner = getPlayerOfSymbol(firstSymbol);
            boolean win = true;
            for (int col = 1; col < size; col++) {
                if (board[row][col] != firstSymbol) {
                    win = false;
                    break;
                }
            }

            if (win) return rowWinner;
        }
        return null;
    }

    private GamePlayer checkColumns() {
        for (int col = 0; col < size; col++) {
            char firstSymbol = board[0][col];
            if (firstSymbol == EMPTY) continue;

            GamePlayer columnWinner = getPlayerOfSymbol(firstSymbol);
            boolean win = true;
            for (int row = 1; row < size; row++) {
                if (board[row][col] != firstSymbol) {
                    win = false;
                    break;
                }
            }

            if (win) return columnWinner;
        }
        return null;
    }

    private GamePlayer checkDiagonals() {
        char mainSymbol = board[0][0];
        if (mainSymbol == EMPTY) return null;

        GamePlayer diagonalWinner = getPlayerOfSymbol(mainSymbol);
        for (int i = 1; i < size; i++) {
            if (board[i][i] != mainSymbol) {
                diagonalWinner = null;
                break;
            }
        }

        char antiSymbol = board[0][size - 1];
        if (antiSymbol == EMPTY) return null;

        GamePlayer antiDiagonalWinner = getPlayerOfSymbol(antiSymbol);
        for (int i = 1; i < size; i++) {
            if (board[i][size - i - 1] != antiSymbol) {
                antiDiagonalWinner = null;
                break;
            }
        }

        return diagonalWinner != null ? diagonalWinner : antiDiagonalWinner;
    }

    private boolean isDraw() {
        return turns == size * size;
    }

    private GamePlayer getPlayerOfSymbol(char symbol) {
        return symbol == PLAYER_X ? playerX : playerO;
    }

    /**
     * Saves the game result in the database only once.
     */
    private synchronized void saveGameResult(GamePlayer winner) {
        if (resultSaved) {
            return;
        }
        resultSaved = true;

        String playerXUsername = playerX.getPlayerName();
        String playerOUsername = playerO.getPlayerName();
        String winnerUsername = winner != null ? winner.getPlayerName() : null;
        Timestamp dateEnded = new Timestamp(System.currentTimeMillis());

        boolean success = dbHandler.saveGameResult(playerXUsername, playerOUsername, winnerUsername, dateEnded);
        if (success) {
            System.out.println("Game result saved successfully.");
        } else {
            System.out.println("Failed to save game result.");
        }
    }
}
