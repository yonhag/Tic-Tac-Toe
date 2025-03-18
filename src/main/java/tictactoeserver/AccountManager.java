package tictactoeserver;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class AccountManager {

    private DatabaseHandler db;
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
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        String line = reader.readLine();
        System.out.println("Received: " + line);

        RequestTypes type = RequestTypes.getRequestType(line);

        line = line.substring(1);
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject)parser.parse(line);

        String username = null;

        switch (type) {
            case Signup -> username = signup(socket, json);
            case Login -> username = login(socket, json);
            default -> new PrintWriter(socket.getOutputStream(), true).println(getAccountSystemResponse(false));
        }

        if (username != null) {
            pm.addUser(socket, username);
        }
    }

    private String signup(Socket socket, JSONObject json) throws IOException {
        PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

        String username = (String)json.get("Username");
        String password = (String)json.get("Password");
        String name = (String)json.get("Name");
        String email = (String)json.get("Email");

        boolean status = db.createUser(username, password, name, email);
        writer.println(getAccountSystemResponse(status).toJSONString());
        if (status)
            return username;
        return null;
    }

    private String login(Socket socket, JSONObject json) throws IOException {
        PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

        boolean status = db.validateLogin((String)json.get("Username"), (String)json.get("Password"));
        writer.println(getAccountSystemResponse(status).toJSONString());
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

