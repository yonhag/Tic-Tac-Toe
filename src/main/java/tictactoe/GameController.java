package tictactoe;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

import static java.lang.Thread.sleep;
import static tictactoe.GameStarter.serverPort;

/*
    Controls the game window itself.
*/
public class GameController {
    @FXML private GridPane gameBoard;
    @FXML private Label statusLabel;

    private int boardSize;
    private Button[][] buttons;

    private Player player;
    private Boolean isMyTurn;
    private char playerSymbol;

    private Socket server;
        private PrintWriter writer;
        private BufferedReader reader;

    public void startGame(Player player, Socket socket) throws IOException {
        this.player = player;
        boardSize = player.getSize();
        playerSymbol = player.getSymbol();
        isMyTurn = playerSymbol == 'X';
        statusLabel.setText(isMyTurn ? "Your Turn!" : "Opponent's Turn");

        // Creating the socket
        server = socket;

        // Creating streams for use
        InputStream inputStream = server.getInputStream();
        reader = new BufferedReader(new InputStreamReader(inputStream));
        writer = new PrintWriter(server.getOutputStream(), true);

        createBoard();

        if (!isMyTurn) {
            new Thread(() -> {
                try {
                    startListeningForOpponent();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    private void createBoard() {
        buttons = new Button[boardSize][boardSize];
        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                Button button = new Button();
                button.setPrefSize(100, 100);
                button.setOnAction(_ -> {
                    try {
                        handleButtonClick(button);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
                button.setUserData(new int[]{row, col});
                buttons[row][col] = button;
                gameBoard.add(button, col, row);
            }
        }
    }

    private void handleButtonClick(Button button) throws IOException {
        if (!isMyTurn)
            return;

        if (!button.getText().isEmpty()) return;

        int[] coordinates = (int[]) button.getUserData();
        int x = coordinates[0];
        int y = coordinates[1];

        new Thread(() -> {
            if (updateServerOnMove(x, y)) {
                button.setText(String.valueOf(playerSymbol));
                isMyTurn = false;
                statusLabel.setText("Opponent's Turn");
                try {
                    startListeningForOpponent();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    public void updateBoard(String board) {
        Platform.runLater(() -> {
            for (int x = 0; x < player.getSize(); x++) {
                for (int y = 0; y < player.getSize(); y++) {
                    buttons[x][y].setText(String.valueOf(board.charAt(x + y)));
                }
            }
        });
    }

    private boolean updateServerOnMove(int x, int y) {
        JSONObject json = new JSONObject();
        json.put("X", x);
        json.put("Y", y);

        // Sending the message
        if (writer != null) {
            writer.println(json);
            System.out.println("Message sent: " + json);
            try {
                // Read server response
                JSONObject responseMessage = (JSONObject) new JSONParser().parse(reader.readLine());
                System.out.println(responseMessage);
                if ((Long)responseMessage.get("Status") != ActionStatus.Success.getValue()) {
                    return false;
                }
            } catch (IOException e) {
                System.err.println("Error reading server response: " + e.getMessage());
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        } else {
            System.err.println("Writer is not initialized.");
        }

        return false;
    }

    public Player getPlayer() {
        return player;
    }

    private void disableBoard() {
        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                buttons[row][col].setDisable(true);
            }
        }
    }

    public void startListeningForOpponent() throws IOException {
            String message;
            while ((message = reader.readLine()) != null) {
                System.out.println("Received: " + message);
                handleServerMessage(message);
            }
    }

    public void handleServerMessage(String message) throws IOException {
        JSONObject json = (JSONObject) JSONValue.parse(message);
        System.out.println(json.get("State"));

        Platform.runLater(() -> {
            if ((Long) json.get("State") == GameStates.GameStillGoing.getValue()) {
                isMyTurn = true;
                statusLabel.setText("Your Turn");
                updateBoard((String) json.get("Board"));
            } else {
                switch ((GameStates) json.get("State")) {
                    case GameStates.EnemyWin -> statusLabel.setText("You Lost!");
                    case GameStates.Draw -> statusLabel.setText("It's a Draw!");
                    default -> statusLabel.setText("You won!");
                }
                disableBoard();

                Platform.runLater(() -> {
                    try {
                        returnToMenu();
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                });
            }
        });
    }

    private void returnToMenu() throws IOException, InterruptedException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("menu.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        MenuController menuController = fxmlLoader.getController();

        // Possibly a new socket, or the same. Depends on your design.
        menuController.setSocket(InetAddress.getLocalHost(), serverPort);

        Stage stage = new Stage();
        stage.setTitle("Tic Tac Toe Menu");
        stage.setScene(scene);
        stage.show();

        // Closing current window
        Thread.sleep(5000);
        ((Stage) statusLabel.getScene().getWindow()).close();
    }
}
