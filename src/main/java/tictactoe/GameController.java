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
import org.json.simple.parser.ParseException;

import java.io.*;

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

    private SocketManager server;

    public void startGame(Player player, SocketManager socket) {
        this.player = player;
        boardSize = player.getSize();
        isMyTurn = player.getSymbol() == 'X';    // Assume player 'X' starts the game.

        statusLabel.setText(isMyTurn ? "Your Turn!" : "Opponent's Turn");

        server = socket;
        createBoard();

        startListening();
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
    private void updateServerOnMove(int x, int y) throws IOException {
        JSONObject json = new JSONObject();
        json.put("X", x);
        json.put("Y", y);
        server.sendJSON(json);
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
            JSONObject json;
            try {
                while ((json = server.getJSON()) != null) {
                    if (json.containsKey("Status")) {
                        handleMyMoveResponse(json);
                    } else {
                        handleEnemyMoveUpdate(json);
                    }
                }
            } catch (IOException e) {
                System.err.println("Error in listener thread: " + e.getMessage());
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    /**
     * Handles server responses to your own moves.
     * Expects a message with a "Status" key.
     */
    private void handleMyMoveResponse(JSONObject json) {
        if ((Long) json.get("Status") != ActionStatus.Success.getValue()) {
            System.err.println("Move not successful!");
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
        new Scene(fxmlLoader.load());
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
