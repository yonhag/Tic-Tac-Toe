package tictactoeserver;

import java.io.*;
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
    private final Queue<Player> playerQueue;
    private final List<GameManager> games;
    private ServerSocket serverSocket;
    private final DatabaseHandler db;
    private final int serverPort = 8000;

    public ProgramManager() throws Exception {
        playerQueue = new LinkedList<>();
        games = new LinkedList<>();
        db = new DatabaseHandler();
        startServer();
    }

    private void startServer() throws Exception {
        try {
            serverSocket = new ServerSocket(serverPort);
            System.out.println("Server started on port: " + serverPort);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());

                new Thread(() -> {
                    try {
                        determineRequestType(clientSocket);
                    } catch (IOException | ParseException e) {
                        throw new RuntimeException(e);
                    }
                }).start();
            }
        } catch (IOException e) {
            stop();
            System.err.println("Error starting server: " + e.getMessage());
        }
    }

    private void determineRequestType(Socket socket) throws IOException, ParseException {
        InputStream inputStream = socket.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        String line = reader.readLine();
        System.out.println("Received: " + line);

        RequestTypes type = RequestTypes.getRequestType(line);

        line = line.substring(1);
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject)parser.parse(line);

        switch (type) {
            case Signup -> signup(socket, json);
            case Login -> login(socket, json);
            case EnterQueue -> handleClient(socket, json);
            default -> throw new IOException();
        }
    }

    private void signup(Socket socket, JSONObject json) throws IOException {
        OutputStream outputStream = socket.getOutputStream();
        PrintWriter writer = new PrintWriter(outputStream, true);

        String username = (String)json.get("username");
        String password = (String)json.get("password");
        String name = (String)json.get("name");
        String email = (String)json.get("username");

        boolean status = db.createUser(username, password, name, email);
        writer.println(getAccountSystemResponse(status).toJSONString());
    }

    private void login(Socket socket, JSONObject json) throws IOException {
        OutputStream outputStream = socket.getOutputStream();
        PrintWriter writer = new PrintWriter(outputStream, true);

        boolean status = db.validateLogin((String)json.get("username"), (String)json.get("password"));
        writer.println(getAccountSystemResponse(status).toJSONString());
    }

    /*
    Function that adds the player to queue
    */
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

    private void stop() throws Exception {
        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
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

    private JSONObject getAccountSystemResponse(boolean isSuccessful) {
        JSONObject j = new JSONObject();
        j.put("Status", isSuccessful);
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
