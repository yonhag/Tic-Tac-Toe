package com.example.tictactoe;

public class GamePair {
    private final GameController player1Game;
    private final GameController player2Game;

    public GamePair(GameController player1Game, GameController player2Game) {
        this.player1Game = player1Game;
        this.player2Game = player2Game;
    }

    public GameController getPlayerGame(Player player) {
        if (player1Game.getPlayer().equals(player)) {
            return player1Game;
        } else if (player2Game.getPlayer().equals(player)) {
            return player2Game;
        }
        return null;
    }

    public GameController getOpponentGame(Player player) {
        if (player1Game.getPlayer().equals(player)) {
            return player2Game;
        } else if (player2Game.getPlayer().equals(player)) {
            return player1Game;
        }
        return null;
    }
}
