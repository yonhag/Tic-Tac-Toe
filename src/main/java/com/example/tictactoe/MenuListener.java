package com.example.tictactoe;

public interface MenuListener {
    void onMenuSubmitted(String playerName, int boardSize);
    void onOpponentFound();
}
