package Client;

import Shared.Protocol.MessageType;
import Shared.Protocol.ProtocolManager;
import Shared.Player.Player;
import Shared.SocketManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Spinner;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class MenuController {
    @FXML
    private Spinner<Integer> boardSizeSpinner;

    private String username;
    private Player player;
    private SocketManager server;

    private final static InetAddress serverIP = InetAddress.ofLiteral("127.0.0.1");
    private final static int port = 8000;

    public void setUsername(String username) {
        this.username = username;
    }

    @FXML
    private void handleSubmit() throws IOException {
        int boardSize = boardSizeSpinner.getValue();
        if (username == null || username.isEmpty()) {
            System.out.println("Username is missing.");
            return;
        }
        server = new SocketManager(new Socket(serverIP, port));
        player = new Player(username, boardSize);
        ProtocolManager loginMessage = new ProtocolManager(MessageType.LOGIN, player);
        server.sendMessage(loginMessage);
        // Listen for the server to start the game.
        new Thread(() -> {
            try {
                handleServerMessages();
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Error receiving server message: " + e.getMessage());
            }
        }).start();
    }

    private void handleServerMessages() throws IOException, ClassNotFoundException {
        ProtocolManager message;
        while ((message = server.readMessage()) != null) {
            switch (message.type()) {
                case START_GAME:
                    player.setSymbol((Shared.Player.PlayerSymbol) message.data());
                    Platform.runLater(() -> {
                        try {
                            openGameWindow();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                    return;
                    /*
                case LOGIN_FAILED:
                    Platform.runLater(() -> {
                        try {
                            showLoginMenu();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                    */
                case DISCONNECT:
                    System.out.println("Disconnected from server.");
                    break;
            }
        }
    }

    public void showLoginMenu() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("login.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        Stage stage = new Stage();
        stage.setTitle("Error - Log In Again");
        stage.setScene(scene);
        stage.show();

        ((Stage) boardSizeSpinner.getScene().getWindow()).close();
    }

    private void openGameWindow() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("game.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        GameController gameController = fxmlLoader.getController();
        gameController.startGame(player, server);

        Stage stage = new Stage();
        stage.setTitle("Tic Tac Toe - Game");
        stage.setScene(scene);
        stage.show();

        // Close the menu window.
        ((Stage) boardSizeSpinner.getScene().getWindow()).close();
    }
}