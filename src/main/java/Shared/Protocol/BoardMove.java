package Shared.Protocol;

import java.io.Serializable;

public class BoardMove implements Serializable {
    private final int x;
    private final int y;

    public BoardMove(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    @Override
    public String toString() {
        return "BoardMove{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}

