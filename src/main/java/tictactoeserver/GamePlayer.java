package tictactoeserver;

import java.io.*;

public class GamePlayer {

    public String playerName;
    private final char playerSign;
    private final SocketManager playerSocket;

    public GamePlayer(String playerName, char playerSign, SocketManager playerSocket) {
        this.playerName = playerName;
        this.playerSign = playerSign;
        this.playerSocket = playerSocket;
    }

    public GamePlayer(Player player, char playerSign) {
        this(player.getName(), playerSign, player.getSocket());
    }

    public String getPlayerName() { return playerName; }

    public void sendMessage(String message) throws IOException {
        playerSocket.sendData(message);
    }

    public String receiveMessage() throws IOException {
        return playerSocket.getData();
    }

    @Override
    public String toString() {
        return "GamePlayer{" +
                "playerSign=" + playerSign +
                ", playerSocket=" + playerSocket +
                '}';
    }
}
