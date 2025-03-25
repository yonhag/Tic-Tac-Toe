package Server.Backend;

public class Board {
    private final int size;
    private final ClientHandler[][] board;

    public Board(int size) {
        this.size = size;
        this.board = new ClientHandler[this.size][this.size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                this.board[i][j] = null;
            }
        }
    }

    public void setMove(ClientHandler clientHandler, int x, int y) {
        board[x][y] = clientHandler;
    }

    public boolean isWon(ClientHandler clientHandler) {
        boolean winning;
        // check rows
        for (int i = 0; i < size; i++) {
            winning = true;
            for (int j = 0; j < size; j++) {
                if (this.board[j][i] != clientHandler) {
                    winning = false;
                    break;
                }
            }
            if (winning) {
                return true;
            }
        }
        // check cols
        for (int i = 0; i < size; i++) {
            winning = true;
            for (int j = 0; j < size; j++) {
                if (this.board[i][j] != clientHandler) {
                    winning = false;
                    break;
                }
            }
            if (winning) {
                return true;
            }
        }
        // check diagonals
        winning = true;
        for (int i = 0; i < size; i++) {
            if (this.board[i][i] != clientHandler) {
                winning = false;
                break;
            }
        }
        if (winning) {
            return true;
        }

        winning = true;
        for (int i = 0; i < size; i++) {
            if (this.board[i][size - i - 1] != clientHandler) {
                winning = false;
                break;
            }
        }
        return winning;
    }

    public boolean hasPlaces() {
        boolean hasPlaces = false;
        for (int i = 0; !hasPlaces && i < size; i++) {
            for (int j = 0; !hasPlaces && j < size; j++) {
                if (isInUse(i, j)) hasPlaces = true;
            }
        }
        return hasPlaces;
    }

    public boolean isInUse(int row, int col) {
        return this.board[row][col] == null;
    }

    public int getSize() {
        return size;
    }
}
