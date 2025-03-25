package Client;

import Shared.Player.Player;
import Shared.Player.PlayerSymbol;
import Shared.Protocol.BoardMove;
import Shared.Protocol.GameState;
import Shared.Protocol.MessageType;
import Shared.Protocol.ProtocolManager;
import Shared.SocketManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.IOException;

public class GameController {
    // FXML controls.
    @FXML
    private GridPane gameBoard;
    @FXML
    private Label statusLabel;

    private int boardSize;
    private Button[][] buttons;

    private Player player;
    private boolean isMyTurn;
    private SocketManager server;

    // Field to store the pending move made by the local player.
    private BoardMove pendingMove = null;

    public void startGame(Player player, SocketManager server) {
        this.player = player;
        this.server = server;
        this.boardSize = player.getSize();
        // 'X' always starts.
        isMyTurn = (player.getSymbol() == PlayerSymbol.X);
        statusLabel.setText(isMyTurn ? "Your Turn!" : "Opponent's Turn");
        createBoard();
        startListening();
    }

    private void createBoard() {
        buttons = new Button[boardSize][boardSize];
        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                Button button = new Button();
                button.setPrefSize(100, 100);
                final int r = row;
                final int c = col;
                button.setOnAction(_ -> {
                    try {
                        handleButtonClick(r, c, button);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                buttons[row][col] = button;
                gameBoard.add(button, col, row);
            }
        }
    }

    // When a cell is clicked, record the pending move and update the UI immediately.
    private void handleButtonClick(int row, int col, Button button) throws IOException {
        // Disallow moves if it's not our turn, a move is pending, or the cell is already taken.
        if (!isMyTurn || pendingMove != null || !button.getText().isEmpty())
            return;
        // Record the pending move and update the UI.
        pendingMove = new BoardMove(row, col);
        button.setText(player.getSymbol().toString());

        // Send the move to the server.
        ProtocolManager moveMessage = new ProtocolManager(MessageType.MOVE, pendingMove);
        server.sendMessage(moveMessage);
    }

    private void startListening() {
        new Thread(() -> {
            try {
                ProtocolManager message;
                while ((message = server.readMessage()) != null) {
                    switch (message.getType()) {
                        // For VALID_MOVE we just update the status;
                        // the pending move will be cleared in GAME_STATUS.
                        case VALID_MOVE:
                            Platform.runLater(() -> statusLabel.setText("Move accepted. Waiting for opponent..."));
                            break;
                        // If the move is rejected, revert the pending move.
                        case INVALID_MOVE:
                            Platform.runLater(() -> {
                                statusLabel.setText("Invalid move. Try again!");
                                if (pendingMove != null) {
                                    int x = pendingMove.getX();
                                    int y = pendingMove.getY();
                                    buttons[x][y].setText("");
                                    pendingMove = null;
                                }
                                isMyTurn = true;
                            });
                            break;
                        // When receiving a MOVE, update the board if it's the opponent’s move.
                        case MOVE:
                            BoardMove move = (BoardMove) message.getData();
                            Platform.runLater(() -> {
                                if (!(pendingMove != null && pendingMove.getX() == move.getX() && pendingMove.getY() == move.getY())) {
                                    updateBoardCell(move.getX(), move.getY(), getOpponentSymbol());
                                }
                            });
                            break;
                        // In GAME_STATUS, decide whose turn it is.
                        case GAME_STATUS:
                            GameState state = (GameState) message.getData();
                            Platform.runLater(() -> {
                                if (state != GameState.STILL_GOING) {
                                    statusLabel.setText("Game Over: " + state);
                                    disableBoard();
                                    // Return to menu after a short delay.
                                    new Thread(() -> {
                                        try {
                                            Thread.sleep(5000);
                                            Platform.runLater(() -> {
                                                try {
                                                    returnToMenu();
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }
                                            });
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }).start();
                                } else {
                                    // If we have a pending move, it means our move was just confirmed.
                                    if (pendingMove != null) {
                                        pendingMove = null;
                                        isMyTurn = false; // Now it's opponent's turn.
                                        statusLabel.setText("Move accepted. Waiting for opponent...");
                                    } else {
                                        // Otherwise, it's now our turn because the opponent moved.
                                        isMyTurn = true;
                                        statusLabel.setText("Your Turn!");
                                    }
                                }
                            });
                            break;
                        case DISCONNECT:
                            // Platform.runLater(() -> statusLabel.setText("Disconnected from server."));
                            return;
                        default:
                            break;
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Error in listener thread: " + e.getMessage());
            }
        }).start();
    }

    // Update a specific board cell with a symbol.
    private void updateBoardCell(int row, int col, PlayerSymbol symbol) {
        buttons[row][col].setText(symbol.toString());
    }

    // Returns the opponent's symbol.
    private PlayerSymbol getOpponentSymbol() {
        return player.getSymbol() == PlayerSymbol.X ? PlayerSymbol.O : PlayerSymbol.X;
    }

    // Disable all cells when the game ends.
    private void disableBoard() {
        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                buttons[row][col].setDisable(true);
            }
        }
    }

    // Return to the menu after game over.
    private void returnToMenu() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("menu.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        MenuController menuController = fxmlLoader.getController();
        menuController.setUsername(player.getName());
        Stage stage = new Stage();
        stage.setTitle("Tic Tac Toe Menu");
        stage.setScene(scene);
        stage.show();
        // Close the game window.
        ((Stage) statusLabel.getScene().getWindow()).close();
    }
}
