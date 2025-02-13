package tictactoeserver;

import java.io.*;
import java.net.*;
import java.util.stream.Collectors;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class GameManager {
    private char[][] board;
    private int[] lastTurn;
    private int size;
    private GamePlayer lastPlayed;

    private GamePlayer player1;
    private GamePlayer player2;

    private static final char EMPTY = '-';
    private static final char PLAYER_X = 'X';
    private static final char PLAYER_O = 'O';

    public GameManager(int size, GamePlayer player1, GamePlayer player2) throws IOException {
        this.player1 = player1;
        this.player2 = player2;

        this.lastPlayed = player2; // Player2 is always O
        this.size = size;
        board = new char[size][size];
        lastTurn = new int[2];

        initializeBoard();

        // Start listening to both players in separate threads
        new Thread(() -> listenToPlayer(player1)).start();
        new Thread(() -> listenToPlayer(player2)).start();
    }

    private void listenToPlayer(GamePlayer player) {
        try {
            while (true) {
                String message = player.receiveMessage();
                Object j = JSONValue.parse(message);

                if (j instanceof JSONObject) {
                    handlePlayerRequest(player, (JSONObject) j);
                }
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
            int x = ((Long) request.get("x")).intValue();
            int y = ((Long) request.get("y")).intValue();

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

        // Send the response back to the player
        player.sendMessage(response.toJSONString());
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
        if (player.equals(player1))
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
