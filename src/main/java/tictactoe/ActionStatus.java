package tictactoe;

public enum ActionStatus {
    Success(0),
    InvalidMove(1),
    NotYourTurn(2);

    private final int value;

    ActionStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
