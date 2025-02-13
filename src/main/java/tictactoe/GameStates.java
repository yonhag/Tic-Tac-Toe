package tictactoe;

public enum GameStates {
    GameStillGoing(0),
    YouWin(1),
    EnemyWin(2),
    Tie(3);

    private final int value;

    GameStates(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
