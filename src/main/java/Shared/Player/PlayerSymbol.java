package Shared.Player;

import java.io.Serializable;

public enum PlayerSymbol implements Serializable {
    X("X"), O("O");

    private final String symbol;

    PlayerSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }

    @Override
    public String toString() {
        return symbol;
    }
}
