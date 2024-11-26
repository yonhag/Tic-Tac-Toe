package com.example.tictactoe;

public interface GameListener {
    void onTurn(Player player, int x, int y);
    int[] getLastTurn();
    boolean isMyTurn(Player player);
}
