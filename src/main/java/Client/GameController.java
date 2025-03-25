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
                button.setOnAction(_ -> handleButtonClick(r, c, button));
                buttons[row][col] = button;
                gameBoard.add(button, col, row);
            }
        }
    }

    // When a cell is clicked, record the pending move and update the UI immediately.
    private void handleButtonClick(int row, int col, Button button) {
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
                    switch (message.type()) {
                        case VALID_MOVE:
                            Platform.runLater(() -> statusLabel.setText("Move accepted. Waiting for opponent..."));
                            break;
                        case INVALID_MOVE:
                            Platform.runLater(() -> {
                                statusLabel.setText("Invalid move. Try again!");
                                if (pendingMove != null) {
                                    int x = pendingMove.x();
                                    int y = pendingMove.y();
                                    buttons[x][y].setText("");
                                    pendingMove = null;
                                }
                                isMyTurn = true;
                            });
                            break;
                        case MOVE:
                            BoardMove move = (BoardMove) message.data();
                            Platform.runLater(() -> {
                                if (!(pendingMove != null && pendingMove.x() == move.x() && pendingMove.y() == move.y())) {
                                    updateBoardCell(move.x(), move.y(), getOpponentSymbol());
                                }
                            });
                            break;
                        case GAME_STATUS:
                            GameState state = (GameState) message.data();
                            Platform.runLater(() -> {
                                if (state != GameState.STILL_GOING) {
                                    statusLabel.setText("Game Over: " + state);
                                    disableBoard();
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
                                    if (pendingMove != null) {
                                        pendingMove = null;
                                        isMyTurn = false;
                                        statusLabel.setText("Move accepted. Waiting for opponent...");
                                    } else {
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

    private void updateBoardCell(int row, int col, PlayerSymbol symbol) {
        buttons[row][col].setText(symbol.toString());
    }

    private PlayerSymbol getOpponentSymbol() {
        return player.getSymbol() == PlayerSymbol.X ? PlayerSymbol.O : PlayerSymbol.X;
    }

    private void disableBoard() {
        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                buttons[row][col].setDisable(true);
            }
        }
    }

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
