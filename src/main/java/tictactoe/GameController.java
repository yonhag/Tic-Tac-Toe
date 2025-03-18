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

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

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
    private static final int serverPort = 8000;

    public void startGame(Player player, Socket socket) throws IOException {
        this.player = player;
        boardSize = player.getSize();
        playerSymbol = player.getSymbol();
        // Assume player 'X' starts the game.
        isMyTurn = playerSymbol == 'X';
        statusLabel.setText(isMyTurn ? "Your Turn!" : "Opponent's Turn");

        // Set up the socket and I/O streams.
        server = socket;
        writer = new PrintWriter(server.getOutputStream(), true);
        reader = new BufferedReader(new InputStreamReader(server.getInputStream()));

        createBoard();

        // Start the centralized listener thread.
        startListening();
    }

    private void createBoard() {
        buttons = new Button[boardSize][boardSize];
        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                Button button = new Button();
                button.setPrefSize(100, 100);
                // Each button’s event handler only sends the move;
                // the UI will be updated later when the server responds.
                button.setOnAction(event -> handleButtonClick(button));
                button.setUserData(new int[]{row, col});
                buttons[row][col] = button;
                gameBoard.add(button, col, row);
            }
        }
    }

    private void handleButtonClick(Button button) {
        if (!isMyTurn)
            return;

        if (!button.getText().isEmpty())
            return;

        int[] coordinates = (int[]) button.getUserData();
        int x = coordinates[0];
        int y = coordinates[1];

        // Prevent further moves until the server responds.
        isMyTurn = false;
        updateServerOnMove(x, y);
    }

    public void updateBoard(String board) {
        Platform.runLater(() -> {
            for (int x = 0; x < boardSize; x++) {
                for (int y = 0; y < boardSize; y++) {
                    buttons[x][y].setText(formatCell(board.charAt(x * boardSize + y)));
                }
            }
        });
    }

    private String formatCell(char c) {
        return c == '-' ? "" : String.valueOf(c);
    }

    /**
     * Sends a move to the server.
     */
    private void updateServerOnMove(int x, int y) {
        JSONObject json = new JSONObject();
        json.put("X", x);
        json.put("Y", y);

        if (writer != null) {
            writer.println(json);
            System.out.println("Message sent: " + json);
        } else {
            System.err.println("Writer is not initialized.");
        }
    }

    private void disableBoard() {
        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                buttons[row][col].setDisable(true);
            }
        }
    }

    /**
     * Starts a single thread that continuously reads messages from the server
     * and dispatches them to the appropriate handler.
     */
    private void startListening() {
        new Thread(() -> {
            String message;
            try {
                while ((message = reader.readLine()) != null) {
                    System.out.println("Received: " + message);
                    JSONObject json = (JSONObject) JSONValue.parse(message);
                    // Dispatch based on the presence of a "Status" key.
                    if (json.containsKey("Status")) {
                        handleMyMoveResponse(json);
                    } else {
                        handleEnemyMoveUpdate(json);
                    }
                }
            } catch (IOException e) {
                System.err.println("Error in listener thread: " + e.getMessage());
            }
        }).start();
    }

    /**
     * Handles server responses to your own moves.
     * Expects a message with a "Status" key.
     */
    private void handleMyMoveResponse(JSONObject json) {
        System.out.println("My move response: " + json);
        // Check if the move was successful.
        if ((Long) json.get("Status") != ActionStatus.Success.getValue()) {
            System.err.println("Move not successful!");
            // Optionally, re-enable the board to allow another move.
            Platform.runLater(() -> {
                statusLabel.setText("Your Turn");
                isMyTurn = true;
            });
            return;
        }
        long state = (Long) json.get("State");
        Platform.runLater(() -> {
            updateBoard((String) json.get("Board"));
            if (state == GameStates.GameStillGoing.getValue()) {
                statusLabel.setText("Opponent's Turn");
            } else {
                // Game over conditions:
                if (state == GameStates.EnemyWin.getValue()) {
                    statusLabel.setText("You Lost!");
                } else if (state == GameStates.Draw.getValue()) {
                    statusLabel.setText("It's a Draw!");
                } else {
                    statusLabel.setText("You Won!");
                }
                disableBoard();
                // Return to menu after a delay.
                new Thread(() -> {
                    try {
                        Thread.sleep(5000);
                        Platform.runLater(() -> {
                            try {
                                returnToMenu();
                            } catch (IOException | InterruptedException e) {
                                e.printStackTrace();
                            }
                        });
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        });
    }

    /**
     * Handles server notifications of the enemy's move.
     * Expects a message without a "Status" key.
     */
    private void handleEnemyMoveUpdate(JSONObject json) {
        System.out.println("Enemy move update: " + json);
        long state = (Long) json.get("State");
        Platform.runLater(() -> {
            updateBoard((String) json.get("Board"));
            if (state == GameStates.GameStillGoing.getValue()) {
                statusLabel.setText("Your Turn");
                isMyTurn = true;
            } else {
                // Game over conditions:
                if (state == GameStates.EnemyWin.getValue()) {
                    statusLabel.setText("You Lost!");
                } else if (state == GameStates.Draw.getValue()) {
                    statusLabel.setText("It's a Draw!");
                } else {
                    statusLabel.setText("You Won!");
                }
                disableBoard();
                // Return to menu after a delay.
                new Thread(() -> {
                    try {
                        Thread.sleep(5000);
                        Platform.runLater(() -> {
                            try {
                                returnToMenu();
                            } catch (IOException | InterruptedException e) {
                                e.printStackTrace();
                            }
                        });
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        });
    }

    /**
     * Returns to the menu after game end.
     */
    private void returnToMenu() throws IOException, InterruptedException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("menu.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        MenuController menuController = fxmlLoader.getController();

        menuController.setParameters(server, player.getName());

        /*Stage stage = new Stage();
        stage.setTitle("Tic Tac Toe Menu");
        stage.setScene(scene);
        stage.show();
        */

        // Close the current game window.
        Thread.sleep(5000);
        ((Stage) statusLabel.getScene().getWindow()).close();
    }
}
