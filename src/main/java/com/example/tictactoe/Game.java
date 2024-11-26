package com.example.tictactoe;

import java.io.*;
import java.net.*;

public class Game implements GameListener {
    private char[][] board;
    private int[] lastTurn;
    private int size;
    private GameController player1;
    private GameController player2;
    private Player lastPlayed;

    private int port;

    private static final char EMPTY = '-';
    private static final char PLAYER_X = 'X';
    private static final char PLAYER_O = 'O';

    public Game(int size, GameController player1, GameController player2) throws IOException {
        this.player1 = player1;
        this.player2 = player2;

        this.player1.setGameListener(this);
        this.player2.setGameListener(this);

        this.lastPlayed = player2.getPlayer();
        this.size = size;
        board = new char[size][size];
        lastTurn = new int[2];

        initializeBoard();
        initializeServer();
    }

    private void initializeServer() throws IOException {

    }

    private void initializeBoard() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                board[i][j] = EMPTY;
            }
        }
        lastTurn[0] = -999;
        lastTurn[1] = -999;
    }

    public void onTurn(Player player, int x, int y) {
        if (player.equals(player1.getPlayer()))
            board[x][y] = PLAYER_X;
        else
            board[x][y] = PLAYER_O;

        lastPlayed = player;
        lastTurn[0] = x;
        lastTurn[1] = y;
    }

    public int[] getLastTurn() {
        System.out.println(lastTurn);
        return lastTurn;
    }

    public boolean isMyTurn(Player player) {
        System.out.println(player.getName());
        return !player.equals(lastPlayed);
    }
}
