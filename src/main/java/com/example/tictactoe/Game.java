package com.example.tictactoe;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class Game {
    @FXML private Label statusLabel;
    @FXML private Button button0, button1, button2, button3, button4, button5, button6, button7, button8;

    private String currentPlayer = "X";
    private Button[] buttons;

    @FXML
    public void initialize() {
        // Store buttons in an array for easier access
        buttons = new Button[]{button0, button1, button2, button3, button4, button5, button6, button7, button8};

        // Add event listeners to each button
        for (Button button : buttons) {
            button.setOnAction(event -> handleButtonClick(button));
        }
    }

    private void handleButtonClick(Button button) {
        // If the button is already clicked, ignore
        if (!button.getText().isEmpty()) return;

        // Set the button's text to the current player's symbol
        button.setText(currentPlayer);

        // Check for a win or draw
        if (checkWin()) {
            statusLabel.setText("Player " + currentPlayer + " Wins!");
            disableButtons();
        } else if (isDraw()) {
            statusLabel.setText("It's a Draw!");
        } else {
            // Switch player
            currentPlayer = currentPlayer.equals("X") ? "O" : "X";
            statusLabel.setText("Player " + currentPlayer + "'s Turn");
        }
    }

    private boolean checkWin() {
        // Winning combinations (3 in a row)
        int[][] winCombinations = {
                {0, 1, 2}, {3, 4, 5}, {6, 7, 8}, // Rows
                {0, 3, 6}, {1, 4, 7}, {2, 5, 8}, // Columns
                {0, 4, 8}, {2, 4, 6}             // Diagonals
        };

        // Checking if player has matching combinations
        for (int[] combo : winCombinations) {
            if (buttons[combo[0]].getText().equals(currentPlayer) &&
                    buttons[combo[1]].getText().equals(currentPlayer) &&
                    buttons[combo[2]].getText().equals(currentPlayer)) {
                return true;
            }
        }
        return false;
    }

    private boolean isDraw() {
        for (Button button : buttons) {
            if (button.getText().isEmpty()) return false;
        }
        return true;
    }

    private void disableButtons() {
        for (Button button : buttons) {
            button.setDisable(true);
        }
    }
}
