package Server.Backend;

public class BoardHandler {
    private final int size;
    private final ClientHandler[][] board;

    public BoardHandler(int size) {
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
        return isWonRows(clientHandler) || isWonCols(clientHandler) || isWonDiags(clientHandler);
    }

    private boolean isWonDiags(ClientHandler clientHandler) {
        return isWonMainDiag(clientHandler) || isWonSecDiag(clientHandler);
    }

    private boolean isWonMainDiag(ClientHandler clientHandler) {
        for (int i = 0; i < size; i++) {
            if (this.board[i][i] != clientHandler) {
                return false;
            }
        }
        return true;
    }

    private boolean isWonSecDiag(ClientHandler clientHandler) {
        for (int i = 0; i < size; i++) {
            if (this.board[i][size - i - 1] != clientHandler) {
               return false;
            }
        }
        return true;
    }

    private boolean isWonCols(ClientHandler clientHandler) {
        boolean isWon;
        for (int i = 0; i < size; i++) {
            isWon = true;
            for (int j = 0; j < size; j++) {
                if (this.board[i][j] != clientHandler) {
                    isWon = false;
                    break;
                }
            }
            if (isWon) {
                return true;
            }
        }
        return false;
    }

    private boolean isWonRows(ClientHandler clientHandler) {
        boolean isWon;
        for (int i = 0; i < size; i++) {
            isWon = true;
            for (int j = 0; j < size; j++) {
                if (this.board[j][i] != clientHandler) {
                    isWon = false;
                    break;
                }
            }
            if (isWon) {
                return true;
            }
        }
        return false;
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
