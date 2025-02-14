package tictactoeserver;

import java.io.*;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class GameManager {
    private char[][] board;
    private int[] lastTurn;
    private int size;
    private GamePlayer lastPlayed;
    private boolean isOver;

    private GamePlayer playerX;
    private GamePlayer playerO;

    private static final char EMPTY = '-';
    private static final char PLAYER_X = 'X';
    private static final char PLAYER_O = 'O';

    public GameManager(int size, GamePlayer playerX, GamePlayer playerO) throws IOException {
        this.playerX = playerX;
        this.playerO = playerO;

        this.isOver = false;

        this.lastPlayed = playerO; // Player2 is always O
        this.size = size;
        board = new char[size][size];
        lastTurn = new int[2];

        initializeBoard();

        while(!isOver) {
            listenToPlayer(playerX);
            listenToPlayer(playerO);
        }
    }

    private void listenToPlayer(GamePlayer player) {
        try {
            String message = player.receiveMessage();
            Object j = JSONValue.parse(message);

            if (j instanceof JSONObject) {
                handlePlayerRequest(player, (JSONObject) j);
            }
        } catch (IOException e) {
            System.out.println("Error listening to player: " + player.playerName + ", " + e.getMessage());
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
                // Process the turn
                onTurn(player, x, y);
                response.put("State", "Success");
            } else {
                response.put("State", "InvalidMove");
            }
        } else {
            response.put("State", "NotYourTurn");
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
        player.sendMessage(response.toJSONString());
        response.remove("State");
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

    public synchronized int[] getLastTurn() {
        return lastTurn;
    }

    public synchronized boolean isMyTurn(GamePlayer player) {
        return !player.equals(lastPlayed);
    }
}
