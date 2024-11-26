package com.example.tictactoe;

import javafx.fxml.FXML;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

import static java.lang.Thread.sleep;

public class GameController {
    @FXML private GridPane gameBoard;
    @FXML private Label statusLabel;

    private int boardSize;
    private Button[][] buttons;

    private Player player;
    private Boolean isMyTurn;
    private String playerSymbol;
    private GameListener listener;

    public void setGameListener(GameListener listener) {
        this.listener = listener;
        if (!isMyTurn)
            startListeningForOpponent();

    }

    public void startGame(Player player1, String symbol) {
        player = player1;
        boardSize = player1.getSize();
        playerSymbol = symbol;
        isMyTurn = symbol.equals("X");
        statusLabel.setText(isMyTurn ? "Your Turn!" : "Opponent's Turn");

        createBoard();
    }

    private void createBoard() {
        buttons = new Button[boardSize][boardSize];
        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                Button button = new Button();
                button.setPrefSize(100, 100);
                button.setOnAction(_ -> handleButtonClick(button));
                button.setUserData(new int[]{row, col});
                buttons[row][col] = button;
                gameBoard.add(button, col, row);
            }
        }
    }

    private void handleButtonClick(Button button) {
        if (!isMyTurn)
            return;

        if (!button.getText().isEmpty()) return;

        button.setText(playerSymbol);
        int[] coordinates = (int[]) button.getUserData();
        int x = coordinates[0];
        int y = coordinates[1];

        listener.onTurn(player, x, y);

        if (checkWin()) {
            statusLabel.setText("You won!");
            disableBoard();
        } else if (isDraw()) {
            statusLabel.setText("It's a Draw!");
            disableBoard();
        } else {
            listener.onTurn(player, x, y);
            isMyTurn = false;
            statusLabel.setText("Opponent's turn");
            startListeningForOpponent();
        }
    }

    public void updateBoard(int x, int y) {
        buttons[x][y].setText(playerSymbol.equals("X") ? "O" : "X");

        if (checkWin()) {
            statusLabel.setText("Opponent Won!");
            disableBoard();
        } else if (isDraw()) {
            statusLabel.setText("It's a Draw!");
            disableBoard();
        } else {
            isMyTurn = true;
            statusLabel.setText("Your turn!");
        }
    }

    private boolean checkWin() {
        return checkRows() || checkColumns() || checkDiagonals();
    }

    private boolean checkRows() {
        for (int row = 0; row < boardSize; row++) {
            boolean win = true;
            for (int col = 1; col < boardSize; col++) {
                if (!buttons[row][col].getText().equals(buttons[row][col - 1].getText()) ||
                        buttons[row][col].getText().isEmpty()) {
                    win = false;
                    break;
                }
            }
            if (win) return true;
        }
        return false;
    }

    private boolean checkColumns() {
        for (int col = 0; col < boardSize; col++) {
            boolean win = true;
            for (int row = 1; row < boardSize; row++) {
                if (!buttons[row][col].getText().equals(buttons[row - 1][col].getText()) ||
                        buttons[row][col].getText().isEmpty()) {
                    win = false;
                    break;
                }
            }
            if (win) return true;
        }
        return false;
    }

    private boolean checkDiagonals() {
        String mainSymbol = buttons[0][0].getText();
        boolean winMainDiagonal = !mainSymbol.isEmpty(); // Ensure the first cell is not empty

        for (int i = 1; i < boardSize; i++) {
            if (!buttons[i][i].getText().equals(mainSymbol)) {
                winMainDiagonal = false;
                break;
            }
        }

        String antiSymbol = buttons[0][boardSize - 1].getText();
        boolean winAntiDiagonal = !antiSymbol.isEmpty(); // Ensure the first cell is not empty

        for (int i = 1; i < boardSize; i++) {
            if (!buttons[i][boardSize - i - 1].getText().equals(antiSymbol)) {
                winAntiDiagonal = false;
                break;
            }
        }

        return winMainDiagonal || winAntiDiagonal;
    }

    private boolean isDraw() {
        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                if (buttons[row][col].getText().isEmpty()) {
                    return false;
                }
            }
        }
        return true;
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

    private void getOthersTurn() {
        while(!listener.isMyTurn(player)) {
            try {
                sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        int[] lastTurn = listener.getLastTurn();
        int x = lastTurn[0];
        int y = lastTurn[1];

        Platform.runLater(() -> {
            updateBoard(x, y);
            isMyTurn = true;
        });
    }

    public void startListeningForOpponent() {
        Thread opponentTurnThread = new Thread(this::getOthersTurn);
        opponentTurnThread.setDaemon(true); // Ensures the thread stops when the application exits
        opponentTurnThread.start();
    }
}
