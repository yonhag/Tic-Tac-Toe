package tictactoeserver;

import org.json.simple.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Objects;

public class Player {
    private final int desiredSize;
    private final String playerName;
    private Socket socket;

    public Player(int size, String name, Socket socket) {
        desiredSize = size;
        playerName = name;
        this.socket = socket;
    }

    public Socket getSocket() {
        return socket;
    }

    public int getSize() {
        return desiredSize;
    }

    public String getName() {
        return playerName;
    }

    public void sendMessage(JSONObject message) throws IOException {
        if (socket == null || socket.isClosed()) {
            throw new IOException("Socket is not connected for player: " + playerName);
        }

        OutputStream outputStream = socket.getOutputStream();
        PrintWriter writer = new PrintWriter(outputStream, true);
        writer.println(message.toJSONString());
        System.out.println("Sent to " + playerName + ": " + message);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Player player = (Player) o;
        return desiredSize == player.desiredSize && playerName.equals(player.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerName, desiredSize);
    }
}
