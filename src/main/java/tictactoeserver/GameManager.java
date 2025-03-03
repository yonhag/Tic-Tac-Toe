package tictactoeserver;

import java.io.*;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import tictactoe.ActionStatus;

public class GameManager {
    private char[][] board;
    private int[] lastTurn;
    private final int size;
    private GamePlayer lastPlayed;
    private boolean isOver;
    private int turns;

    private GamePlayer playerX;
    private GamePlayer playerO;

    private static final char EMPTY = '-';
    private static final char PLAYER_X = 'X';
    private static final char PLAYER_O = 'O';
    public GameManager(int size, GamePlayer playerX, GamePlayer playerO) throws IOException {
        this.playerX = playerX;
        this.playerO = playerO;

        this.isOver = false;

        this.lastPlayed = playerO; // Since PlayerX Starts
        this.size = size;
        board = new char[size][size];
        lastTurn = new int[2];
        turns = 0;

        initializeBoard();

        while(!isOver) {
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
            GamePlayer opponent = player == playerX ? playerX : playerO;
            JSONObject response = new JSONObject();
            response.put("State", GameStates.YouWin.getValue());
            opponent.sendMessage(response.toJSONString());
        }
    }

    private synchronized void handlePlayerRequest(GamePlayer player, JSONObject request) throws IOException {
        // Check if it is the player's turn
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

        // Generate board string
        StringBuilder boardString = new StringBuilder();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                boardString.append(board[i][j]);
            }
        }
        response.put("Board", boardString.toString());

        // Send the response back to the other player
        GamePlayer opponent = player.equals(playerX) ? playerO : playerX;
        response.put("State", getGameState(player).getValue());
        player.sendMessage(response.toJSONString());

        System.out.println(response);

        response.remove("Status");
        response.put("State", getGameState(opponent).getValue());
        opponent.sendMessage(response.toJSONString());


    }

    private void initializeBoard() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                board[i][j] = EMPTY;
            }
        }
        lastTurn[0] = -999;
        lastTurn[1] = -999;
    }

    public synchronized void onTurn(GamePlayer player, int x, int y) {
        if (player.equals(playerX))
            board[x][y] = PLAYER_X;
        else
            board[x][y] = PLAYER_O;

        lastPlayed = player;
        lastTurn[0] = x;
        lastTurn[1] = y;
    }

    public synchronized boolean isMyTurn(GamePlayer player) {
        return !player.equals(lastPlayed);
    }

    private GameStates getGameState(GamePlayer player) {
        GamePlayer winner = getGameWinner();
        if (winner == null)
            if (isDraw())
                return GameStates.Draw;
            else
                return GameStates.GameStillGoing;

        if (winner.equals(player))
            return GameStates.YouWin;
        return GameStates.EnemyWin;
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

            if (firstSymbol == EMPTY)
                continue;

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
    /*
    Returns the player who won any column, or null if none did
     */
    private GamePlayer checkColumns() {
        for (int col = 0; col < size; col++) {
            char firstSymbol = board[0][col];

            if (firstSymbol == EMPTY) // Skip empty columns
                continue;

            GamePlayer columnWinner = getPlayerOfSymbol('X');

            boolean win = true;
            for (int row = 1; row < size; row++) {
                if (board[row][col] != firstSymbol) {
                    win = false;
                    break;
                }
            }

            if (win) return columnWinner; // Return the winning player
        }
        return null; // No winner in any column
    }

    /*
    Returns the player who won any diagonal, or null if none did
     */
    private GamePlayer checkDiagonals() {
        char mainSymbol = board[0][0];

        if (mainSymbol == EMPTY)
            return null;

        GamePlayer diagonalWinner = getPlayerOfSymbol(mainSymbol);

        for (int i = 1; i < size; i++) {
            if (board[i][i] != mainSymbol) {
                diagonalWinner = null;
                break;
            }
        }

        char antiSymbol = board[0][size - 1];

        if (antiSymbol == EMPTY)
            return null;

        GamePlayer antiDiagonalWinner = mainSymbol == 'X' ? playerX : playerO;

        for (int i = 1; i < size; i++) {
            if (board[i][size - i - 1] != antiSymbol) {
                antiDiagonalWinner = null;
                break;
            }
        }

        return diagonalWinner != null ? diagonalWinner : antiDiagonalWinner;
    }

    private boolean isDraw() {
        System.out.println(turns);
        System.out.println(size * size);
        return turns == size * size;
    }

    private GamePlayer getPlayerOfSymbol(char mainSymbol) {
        return mainSymbol == 'X' ? playerX : playerO;
    }
}
