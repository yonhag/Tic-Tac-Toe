package Shared.Player;

import java.io.Serializable;

public class Player implements Serializable {

    private String name;
    private int size;
    private PlayerSymbol symbol;
    private String password;

    public Player(String name, int size) {
        this.name = name;
        this.size = size;
    }

    public Player() {}

    public String getName() {
        return name;
    }

    public int getSize() {
        return size;
    }

    public PlayerSymbol getSymbol() {
        return symbol;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void setSymbol(PlayerSymbol symbol) {
        this.symbol = symbol;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "Player{" +
                "name='" + name + '\'' +
                ", size=" + size +
                '}';
    }
}

