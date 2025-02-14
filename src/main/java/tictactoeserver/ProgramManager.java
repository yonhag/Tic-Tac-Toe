package tictactoeserver;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;
import java.util.List;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
/*
    Controls the menu of the program, sets up games and disconnects them.
    Is not a part of the game.
    Implements the matchmaking.
 */
public class  ProgramManager {
    private Queue<Player> playerQueue;
    private List<GameManager> games;
    private ServerSocket serverSocket;
    private final int serverPort = 8000;

    public ProgramManager() throws IOException {
        playerQueue = new LinkedList<>();
        games = new LinkedList<>();

        startServer();
    }

    private void startServer() {
        try {
            serverSocket = new ServerSocket(serverPort);
            System.out.println("Server started on port: " + serverPort);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());

                new Thread(() -> handleClient(clientSocket)).start();
            }
        } catch (IOException e) {
            System.err.println("Error starting server: " + e.getMessage());
        }
    }

    /*
    Function that adds the player to queue
    */
    private void handleClient(Socket clientSocket) {
        try (
                InputStream inputStream = clientSocket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                OutputStream outputStream = clientSocket.getOutputStream();
                PrintWriter writer = new PrintWriter(outputStream, true)
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("Received: " + line);

                try {
                    JSONParser parser = new JSONParser();
                    JSONObject json = (JSONObject)parser.parse(line);

                    String playerName = (String) json.get("Player_Name");
                    int gameSize = ((Long) json.get("Board_Size")).intValue();

                    onMenuSubmitted(new Player(gameSize, playerName, clientSocket));
                } catch (ParseException e) {
                    System.out.println("Invalid JSON format: " + line);
                    System.err.println("Failed to parse JSON: " + e.getMessage());
                }
            }
            System.out.println("Finished handling client");
        } catch (IOException e) {
            System.err.println("Error handling client: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing client socket: " + e.getMessage());
            }
        }
    }

    public void stop() throws Exception {
        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
        }
    }

    public void onMenuSubmitted(Player newPlayer) throws IOException {
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

    public JSONObject getMatchStartedJson(int gameSize, char Symbol, String opponentName) {
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
                    new GamePlayer(playerO, 'O')
            );

            // Add the paired games to the list
            games.add(manager);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
