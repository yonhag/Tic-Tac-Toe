package com.example.tictactoe;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.List;

public class ProgramManager extends Application implements MenuListener, GameListener {
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

    private Game openGameWindow(Player playerAgainst, String playerSign) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("game.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        Game gameController = fxmlLoader.getController();
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
            Game player1Game = openGameWindow(player1, "X");
            Game player2Game = openGameWindow(player2, "O");

            // Set the same listener for both games
            player1Game.setGameListener(this);
            player2Game.setGameListener(this);

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

    public void onTurn(Player player, int x, int y) {
        Game playerGame = findOpponent(player);
        playerGame.updateBoard(x, y);
    }

    public Game findOpponent(Player targetPlayer) {
        for (GamePair pair : games) {
            Game game = pair.getOpponentGame(targetPlayer);
            if (game != null)
                return game;
        }
        return null;
    }
}
