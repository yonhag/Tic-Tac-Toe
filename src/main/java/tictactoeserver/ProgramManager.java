package tictactoeserver;

import java.io.*;
import java.util.LinkedList;
import java.util.Queue;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
/*
    Controls the menu of the program, sets up games and disconnects them.
    Is not a part of the game.
    Implements the matchmaking.
 */
public class  ProgramManager {
    private final Queue<Player> playerQueue;
    private final DatabaseHandler db;

    public ProgramManager(DatabaseHandler db) {
        playerQueue = new LinkedList<>();
        this.db = db;
    }

    public void addUser(SocketManager socket, String username) {
        new Thread(() -> {
            try {
                handleClient(socket, socket.getJSON());
            } catch (IOException | ParseException e) {
                System.err.println("Error handling user input for " + username + ": " + e.getMessage());
            }
        }).start();
    }


    private void handleClient(SocketManager socket, JSONObject json) {
        String playerName = (String) json.get("Player_Name");
        int gameSize = ((Long) json.get("Board_Size")).intValue();
        try {
            onMenuSubmitted(new Player(gameSize, playerName, socket));
            System.out.println("Finished handling client");
        } catch (IOException e) {
            System.err.println("Error handling client: " + e.getMessage());
        }
    }

    private void onMenuSubmitted(Player newPlayer) throws IOException {
        for (Player player : playerQueue) {
            if (player.getSize() == newPlayer.getSize()) {
                System.out.println("Opponent found!");
                playerQueue.remove(player);

                // Sending both players the start notification
                newPlayer.sendMessage(getMatchStartedJson(newPlayer.getSize(), 'X', player.getName()));
                player.sendMessage(getMatchStartedJson(newPlayer.getSize(), 'O', newPlayer.getName()));
                startGame(newPlayer, player);
                return;
            }
        }

        System.out.println("Player added to queue: " + newPlayer.getName() +
                ", Board size: " + newPlayer.getSize());
        playerQueue.add(newPlayer);
    }

    private JSONObject getMatchStartedJson(int gameSize, char Symbol, String opponentName) {
        JSONObject j = new JSONObject();
        j.put("Symbol", String.valueOf(Symbol));
        j.put("State", 0);
        j.put("Size", gameSize);
        j.put("Opponent", opponentName);
        return j;
    }

    private void startGame(Player playerX, Player playerO) {
        try {
            new GameManager(
                    playerX.getSize(),
                    new GamePlayer(playerX, 'X'),
                    new GamePlayer(playerO, 'O'),
                    db
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
