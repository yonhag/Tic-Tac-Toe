package tictactoe;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class Menu {

    @FXML private TextField nameField;
    @FXML private Spinner<Integer> boardSizeSpinner;

    private Boolean isOpponentFound;

    private Socket server;
        private PrintWriter writer;
        private BufferedReader reader;

    public void setSocket(InetAddress ip, int port) throws IOException {
        server = new Socket();
        SocketAddress socketAddress = new InetSocketAddress(ip, port);
        server.connect(socketAddress);

        // Creating streams for use
        InputStream inputStream = server.getInputStream();
        reader = new BufferedReader(new InputStreamReader(inputStream));
        writer = new PrintWriter(server.getOutputStream(), true);
        isOpponentFound = false;

        // Start a separate thread to continuously listen for messages from the server
        new Thread(() -> {
            try {
                String serverMessage;
                while ((serverMessage = reader.readLine()) != null) {
                    System.out.println("Server: " + serverMessage); // Process or log the received message
                    handleServerMessage(serverMessage);
                }
            } catch (IOException e) {
                System.err.println("Error reading server messages: " + e.getMessage());
            }
        }).start();
    }

    /**
     * Handles incoming server messages.
     * You can modify this function to process different message types.
     */
    private void handleServerMessage(String message) {
        // Example JSON parsing (modify based on your server response structure)
        try {
            JSONObject json = (JSONObject) new JSONParser().parse(message);
            // Sign means that this is a game start message
            if (json.containsKey("Symbol")) {
                Player player = new Player(Math.toIntExact((Long) json.get("Size")),
                        nameField.getText(),
                        json.get("Symbol").toString().charAt(0));
                String opponentName = (String) json.get("Opponent");

                Platform.runLater(() -> {
                    try {
                        openGameWindow(player, opponentName, server);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
        } catch (ParseException e) {
            System.err.println("Invalid server message format: " + message);
        }
    }

    private void openGameWindow(Player player, String opponentName, Socket socket) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("game.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        GameController gameController = fxmlLoader.getController();
        gameController.startGame(player, socket);

        Stage stage = new Stage();
        stage.setTitle("Tic Tac Toe Game Against " + opponentName);
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    private void handleSubmit() throws IOException {
        String playerName = nameField.getText().trim();
        int boardSize = boardSizeSpinner.getValue();

        if (playerName.isEmpty()) {
            nameField.setPromptText("Name is required!");
            nameField.setStyle("-fx-border-color: red;");
            return;
        }

        if (server.isConnected()) {
            JSONObject j = new JSONObject();
            j.put("Player_Name", playerName);
            j.put("Board_Size", boardSize);

            writer.println(j.toString());
        }
    }

    public void onOpponentFound() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }
}
