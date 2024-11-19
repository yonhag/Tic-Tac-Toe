package com.example.tictactoe;

public class GamePair {
    private final Game player1Game;
    private final Game player2Game;

    public GamePair(Game player1Game, Game player2Game) {
        this.player1Game = player1Game;
        this.player2Game = player2Game;
    }

    public Game getPlayerGame(Player player) {
        if (player1Game.getPlayer().equals(player)) {
            return player1Game;
        } else if (player2Game.getPlayer().equals(player)) {
            return player2Game;
        }
        return null;
    }

    public Game getOpponentGame(Player player) {
        if (player1Game.getPlayer().equals(player)) {
            return player2Game;
        } else if (player2Game.getPlayer().equals(player)) {
            return player1Game;
        }
        return null;
    }
}
