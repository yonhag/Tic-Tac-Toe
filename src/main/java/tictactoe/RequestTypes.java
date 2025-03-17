package tictactoe;

public enum RequestTypes {
    Signup(0),
    Login(1),
    EnterQueue(2);

    private final int value;

    RequestTypes(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
