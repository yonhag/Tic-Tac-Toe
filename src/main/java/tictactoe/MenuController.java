package tictactoe;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Spinner;
import javafx.stage.Stage;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.*;

public class MenuController {

    @FXML private Spinner<Integer> boardSizeSpinner;
    private String username;

    private boolean hasSent = false;

    private SocketManager server;

    public void setParameters(SocketManager server, String username) {
        this.username = username;
        this.server = server;

        // Start a separate thread to continuously listen for messages from the server
        new Thread(() -> {
            try {
                handleServerMessage(server.getJSON());
            } catch (IOException | ParseException e) {
                System.err.println("Error reading server message: " + e.getMessage());
            }
        }).start();
    }

    /**
     * Handles incoming server messages.
     * You can modify this function to process different message types.
     */
    private void handleServerMessage(JSONObject message) {
        System.out.println(message);

        if (message.containsKey("Symbol")) {
            Player player = new Player(
                    Math.toIntExact((Long) message.get("Size")),
                    username,
                    message.get("Symbol").toString().charAt(0)
            );

            String opponentName = (String) message.get("Opponent");

            Platform.runLater(() -> {
                try {
                    openGameWindow(player, opponentName, server);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    private void openGameWindow(Player player, String opponentName, SocketManager socket) throws IOException {
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

        if (server.isConnected() && !hasSent) {
            JSONObject j = new JSONObject();
            j.put("Player_Name", username);
            j.put("Board_Size", boardSize);

            server.sendJSON(j);
            hasSent = true;
        }
    }
}
