package tictactoeserver;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.List;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
/*
    Controls the menu of the program, sets up games and disconnects them.
    Is not a part of the game.
    Implements the matchmaking.
 */
public class  ProgramManager {
    private final Queue<Player> playerQueue;
    private final List<GameManager> games;
    private final DatabaseHandler db;

    public ProgramManager(DatabaseHandler db) throws Exception {
        playerQueue = new LinkedList<>();
        games = new LinkedList<>();
        this.db = db;
    }

    public void addUser(Socket socket, String username) {
        new Thread(() -> {
            try {
                // Create the BufferedReader without a try-with-resources block to avoid closing the stream
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String input = in.readLine();

                System.out.println("ProgramManager: " + input);

                // Parse the received JSON string
                JSONObject json = (JSONObject) new org.json.simple.parser.JSONParser().parse(input);

                // Handle the client request using the same open socket
                handleClient(socket, json);

            } catch (IOException | ParseException e) {
                System.err.println("Error handling user input for " + username + ": " + e.getMessage());
            }
        }).start();
    }


    private void handleClient(Socket socket, JSONObject json) {
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
            GameManager manager = new GameManager(
                    playerX.getSize(),
                    new GamePlayer(playerX, 'X'),
                    new GamePlayer(playerO, 'O'),
                    db
            );

            // Add the paired games to the list
            games.add(manager);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
