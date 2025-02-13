package tictactoe;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import static java.lang.Thread.sleep;

/*
    Controls the game window itself.
*/
public class GameController {
    @FXML private GridPane gameBoard;
    @FXML private Label statusLabel;

    private int boardSize;
    private Button[][] buttons;

    private Player player;
    private Boolean isMyTurn;
    private String playerSymbol;

    private Socket server;
        private PrintWriter writer;
        private BufferedReader reader;

    public void startGame(Player player1, String symbol, InetAddress ip, int port) throws IOException {
        player = player1;
        boardSize = player1.getSize();
        playerSymbol = symbol;
        isMyTurn = symbol.equals("X");
        statusLabel.setText(isMyTurn ? "Your Turn!" : "Opponent's Turn");

        // Creating the socket
        server = new Socket();
        SocketAddress socketAddress = new InetSocketAddress(ip, port);
        server.connect(socketAddress);

        // Creating streams for use
        InputStream inputStream = server.getInputStream();
        reader = new BufferedReader(new InputStreamReader(inputStream));
        writer = new PrintWriter(server.getOutputStream(), true);

        createBoard();
    }

    private void createBoard() {
        buttons = new Button[boardSize][boardSize];
        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                Button button = new Button();
                button.setPrefSize(100, 100);
                button.setOnAction(_ -> {
                    try {
                        handleButtonClick(button);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
                button.setUserData(new int[]{row, col});
                buttons[row][col] = button;
                gameBoard.add(button, col, row);
            }
        }
    }

    private void handleButtonClick(Button button) throws IOException {
        if (!isMyTurn)
            return;

        if (!button.getText().isEmpty()) return;

        button.setText(playerSymbol);
        int[] coordinates = (int[]) button.getUserData();
        int x = coordinates[0];
        int y = coordinates[1];

        sendMove(x, y);


    }

    public void updateBoard(int x, int y) {
        buttons[x][y].setText(playerSymbol.equals("X") ? "O" : "X");

        if (checkWin()) {
            statusLabel.setText("Opponent Won!");
            disableBoard();
        } else if (isDraw()) {
            statusLabel.setText("It's a Draw!");
            disableBoard();
        } else {
            isMyTurn = true;
            statusLabel.setText("Your turn!");
        }
    }

    private void sendMove(int x, int y) {
        JSONObject json = new JSONObject();
        json.put("X", x);
        json.put("Y", y);

        // Sending the message
        if (writer != null) {
            writer.println(json.toString());
            System.out.println("Message sent: " + json);
        } else {
            System.err.println("Writer is not initialized.");
        }
    }

    private boolean checkWin() {
        return checkRows() || checkColumns() || checkDiagonals();
    }

    private boolean checkRows() {
        for (int row = 0; row < boardSize; row++) {
            boolean win = true;
            for (int col = 1; col < boardSize; col++) {
                if (!buttons[row][col].getText().equals(buttons[row][col - 1].getText()) ||
                        buttons[row][col].getText().isEmpty()) {
                    win = false;
                    break;
                }
            }
            if (win) return true;
        }
        return false;
    }

    private boolean checkColumns() {
        for (int col = 0; col < boardSize; col++) {
            boolean win = true;
            for (int row = 1; row < boardSize; row++) {
                if (!buttons[row][col].getText().equals(buttons[row - 1][col].getText()) ||
                        buttons[row][col].getText().isEmpty()) {
                    win = false;
                    break;
                }
            }
            if (win) return true;
        }
        return false;
    }

    private boolean checkDiagonals() {
        String mainSymbol = buttons[0][0].getText();
        boolean winMainDiagonal = !mainSymbol.isEmpty(); // Ensure the first cell is not empty

        for (int i = 1; i < boardSize; i++) {
            if (!buttons[i][i].getText().equals(mainSymbol)) {
                winMainDiagonal = false;
                break;
            }
        }

        String antiSymbol = buttons[0][boardSize - 1].getText();
        boolean winAntiDiagonal = !antiSymbol.isEmpty(); // Ensure the first cell is not empty

        for (int i = 1; i < boardSize; i++) {
            if (!buttons[i][boardSize - i - 1].getText().equals(antiSymbol)) {
                winAntiDiagonal = false;
                break;
            }
        }

        return winMainDiagonal || winAntiDiagonal;
    }

    private boolean isDraw() {
        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                if (buttons[row][col].getText().isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    public Player getPlayer() {
        return player;
    }

    private void disableBoard() {
        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                buttons[row][col].setDisable(true);
            }
        }
    }

    public void startListeningForOpponent() throws IOException {
            String message;
            while ((message = reader.readLine()) != null) {
                System.out.println("Received: " + message);
                handleServerMessage(message);
            }
    }

    public void handleServerMessage(String message) {
        JSONObject json = (JSONObject)JSONValue.parse(message);

        switch((GameStates)json.get("State"))
        {
            case GameStates.GameStillGoing:
                isMyTurn = true;
                statusLabel.setText("Your Turn");
                break;
            case GameStates.Tie:
                statusLabel.setText("It's a Draw!");
                disableBoard();
                break;
            case GameStates.EnemyWin:
                statusLabel.setText("Enemy won!");
                disableBoard();
                break;
            case GameStates.YouWin:
                statusLabel.setText("You won!");
                disableBoard();
                break;
        }
    }
}
