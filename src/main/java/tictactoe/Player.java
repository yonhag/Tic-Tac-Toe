package tictactoe;

import java.util.Objects;

public class Player {
    private final int desiredSize;
    private final String playerName;

    public Player(int size, String name) {
        desiredSize = size;
        playerName = name;
    }

    public int getSize() {
        return desiredSize;
    }

    public String getName() {
        return playerName;
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
