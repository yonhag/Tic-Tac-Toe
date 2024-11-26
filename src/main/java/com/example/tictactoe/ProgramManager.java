package com.example.tictactoe;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.List;

public class ProgramManager extends Application implements MenuListener {
    private Queue<Player> playerQueue;
    private List<GamePair> games;


    @Override
    public void start(Stage stage) throws IOException {
        playerQueue = new LinkedList<>();
        games = new LinkedList<>();
        openMenuWindow();
        openMenuWindow();
    }

    private Menu openMenuWindow() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("menu.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        Menu menuController = fxmlLoader.getController();
        menuController.setMenuListener(this);

        Stage stage = new Stage();
        stage.setTitle("Tic Tac Toe Menu");
        stage.setScene(scene);
        stage.show();

        return menuController;
    }

    private GameController openGameWindow(Player playerAgainst, String playerSign) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("game.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        GameController gameController = fxmlLoader.getController();
        gameController.startGame(playerAgainst, playerSign);

        Stage stage = new Stage();
        stage.setTitle("Tic Tac Toe Game Against " + playerAgainst.getName());
        stage.setScene(scene);
        stage.show();

        return gameController;
    }

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void onMenuSubmitted(String playerName, int boardSize) {
        Player newPlayer = new Player(boardSize, playerName);

        for (Player player : playerQueue) {
            if (player.getSize() == boardSize) {
                playerQueue.remove(player);
                startGame(newPlayer, player);
                return;
            }
        }

        playerQueue.add(newPlayer);
    }

    private void startGame(Player player1, Player player2) {
        try {
            GameController player1Game = openGameWindow(player1, "X");
            GameController player2Game = openGameWindow(player2, "O");
            Game game = new Game(player1.getSize(), player1Game, player2Game);

            // Add the paired games to the list
            games.add(new GamePair(player1Game, player2Game));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onOpponentFound() {
        // This method can be left empty or used for additional logic if needed
    }
}
