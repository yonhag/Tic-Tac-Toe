package com.example.tictactoe;

import javafx.fxml.FXML;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class Menu {

    @FXML private TextField nameField;
    @FXML private Spinner<Integer> boardSizeSpinner;

    private MenuListener listener;

    public void setMenuListener(MenuListener listener) {
        this.listener = listener;
    }

    @FXML
    private void handleSubmit() {
        String playerName = nameField.getText().trim();
        int boardSize = boardSizeSpinner.getValue();

        if (playerName.isEmpty()) {
            nameField.setPromptText("Name is required!");
            nameField.setStyle("-fx-border-color: red;");
            return;
        }

        if (listener != null) {
            listener.onMenuSubmitted(playerName, boardSize);
        }
    }

    public void onOpponentFound() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }
}
