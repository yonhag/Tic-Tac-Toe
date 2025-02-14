package tictactoeserver;

import java.io.*;
import java.net.Socket;

public class GamePlayer {

    public String playerName;
    private char playerSign;
    private Socket playerSocket;

    public GamePlayer(String playerName, char playerSign, Socket playerSocket) {
        this.playerName = playerName;
        this.playerSign = playerSign;
        this.playerSocket = playerSocket;
    }

    public GamePlayer(Player player, char playerSign) {
        this(player.getName(), playerSign, player.getSocket());
    }

    public char getPlayerSign() {
        return playerSign;
    }

    public Socket getPlayerSocket() {
        return playerSocket;
    }

    public void sendMessage(String message) throws IOException {
        OutputStream outputStream = playerSocket.getOutputStream();
        PrintWriter writer = new PrintWriter(outputStream, true);
        writer.println(message);
    }

    public String receiveMessage() throws IOException {
        InputStream inputStream = playerSocket.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        return reader.readLine();
    }

    @Override
    public String toString() {
        return "GamePlayer{" +
                "playerSign=" + playerSign +
                ", playerSocket=" + playerSocket +
                '}';
    }
}
