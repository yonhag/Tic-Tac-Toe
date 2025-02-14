package tictactoe;

import java.util.Objects;

public class Player {
    private final int size;
    private final String name;
    private final char symbol;

    public Player(int size, String name, char symbol) {
        this.size = size;
        this.symbol = symbol;
        this.name = name;
    }

    public int getSize() {
        return size;
    }

    public String getName() {
        return name;
    }

    public char getSymbol() {
        return symbol;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Player player = (Player) o;
        return size == player.size && name.equals(player.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, size);
    }
}
