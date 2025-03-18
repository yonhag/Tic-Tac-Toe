package tictactoe;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Spinner;
import javafx.stage.Stage;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.*;

public class MenuController {

    @FXML private Spinner<Integer> boardSizeSpinner;
    private String username;

    private Socket server;
        private PrintWriter writer;
        private BufferedReader reader;

    public void setParameters(Socket server, String username) throws IOException {
        this.username = username;
        this.server = server;

        // Creating streams for use
        reader = new BufferedReader(new InputStreamReader(server.getInputStream()));
        writer = new PrintWriter(server.getOutputStream(), true);

        // Start a separate thread to continuously listen for messages from the server
        new Thread(() -> {
            try {
                String message = reader.readLine(); // read exactly one line
                if (message != null) {
                    // handle or process the single message
                    handleServerMessage(message);
                }
            } catch (IOException e) {
                System.err.println("Error reading server message: " + e.getMessage());
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

            System.out.println(json);

            if (json.containsKey("Symbol")) {
                Player player = new Player(
                        Math.toIntExact((Long) json.get("Size")),
                        username,
                        json.get("Symbol").toString().charAt(0)
                );

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

        // Closing menu window as it is not needed anymore
        ((Stage) boardSizeSpinner.getScene().getWindow()).close();
    }

    @FXML
    private void handleSubmit() throws IOException {
        int boardSize = boardSizeSpinner.getValue();

        if (server.isConnected()) {
            JSONObject j = new JSONObject();
            j.put("Player_Name", username);
            j.put("Board_Size", boardSize);

            writer.println(j.toString());
        }
    }

    public void onOpponentFound() {
        Stage stage = (Stage) boardSizeSpinner.getScene().getWindow();
        stage.close();
    }
}
