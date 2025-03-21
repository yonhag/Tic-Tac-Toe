package tictactoeserver;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.ServerSocket;

public class AccountManager {

    private final DatabaseHandler db;
    private ServerSocket serverSocket;
    private final int serverPort = 8000;
    private final ProgramManager pm;

    public AccountManager() throws Exception {
        db = new DatabaseHandler();
        pm = new ProgramManager(db);
        startServer();
    }

    private void startServer() throws Exception {
        try {
            serverSocket = new ServerSocket(serverPort);
            System.out.println("Server started on port: " + serverPort);

            while (true) {
                SocketManager clientSocket = new SocketManager(serverSocket.accept());
                System.out.println("New client connected: " + clientSocket.getInetAddress());

                new Thread(() -> {
                    try {
                        handleClient(clientSocket);
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

    private void handleClient(SocketManager socket) throws IOException, ParseException {
        String line = socket.getData();
        System.out.println("Received: " + line);

        RequestTypes type = RequestTypes.getRequestType(line);

        line = line.substring(1);
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject)parser.parse(line);

        String username = null;

        switch (type) {
            case Signup -> username = signup(socket, json);
            case Login -> username = login(socket, json);
            default -> socket.sendJSON(getAccountSystemResponse(false));
        }

        if (username != null) {
            pm.addUser(socket, username);
        }
    }

    private String signup(SocketManager socket, JSONObject json) throws IOException {
        String username = (String)json.get("Username");
        String password = (String)json.get("Password");
        String name = (String)json.get("Name");
        String email = (String)json.get("Email");

        boolean status = db.createUser(username, password, name, email);
        socket.sendJSON(getAccountSystemResponse(status));
        if (status)
            return username;
        return null;
    }

    private String login(SocketManager socket, JSONObject json) throws IOException {
        boolean status = db.validateLogin((String)json.get("Username"), (String)json.get("Password"));
        socket.sendJSON(getAccountSystemResponse(status));
        if (status)
            return (String)json.get("Username");
        return null;
    }

    private void stop() throws Exception {
        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
        }
    }

    private JSONObject getAccountSystemResponse(boolean isSuccessful) {
        JSONObject j = new JSONObject();
        j.put("Status", isSuccessful);
        return j;
    }
}

