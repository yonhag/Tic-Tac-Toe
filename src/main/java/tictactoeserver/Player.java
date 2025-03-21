package tictactoeserver;

import org.json.simple.JSONObject;

import java.io.IOException;
import java.util.Objects;

public class Player {
    private final int desiredSize;
    private final String playerName;
    private SocketManager socket;

    public Player(int size, String name, SocketManager socket) {
        desiredSize = size;
        playerName = name;
        this.socket = socket;
    }

    public SocketManager getSocket() {
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

        socket.sendJSON(message);
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
