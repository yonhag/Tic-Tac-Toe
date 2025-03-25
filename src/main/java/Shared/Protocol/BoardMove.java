package Shared.Protocol;

import java.io.Serializable;

public record BoardMove(int x, int y) implements Serializable {

    @Override
    public String toString() {
        return "BoardMove{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}

