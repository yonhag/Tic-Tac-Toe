package tictactoe;

import javafx.fxml.FXML;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class Menu {

    @FXML private TextField nameField;
    @FXML private Spinner<Integer> boardSizeSpinner;

    private Boolean isOpponentFound;

    private Socket server;
        private PrintWriter writer;
        private BufferedReader reader;

    public void setSocket(InetAddress ip, int port) throws IOException {
        server = new Socket();
        SocketAddress socketAddress = new InetSocketAddress(ip, port);
        server.connect(socketAddress);

        // Creating streams for use
        InputStream inputStream = server.getInputStream();
        reader = new BufferedReader(new InputStreamReader(inputStream));
        writer = new PrintWriter(server.getOutputStream(), true);
        isOpponentFound = false;
    }

    @FXML
    private void handleSubmit() throws IOException {
        String playerName = nameField.getText().trim();
        int boardSize = boardSizeSpinner.getValue();

        if (playerName.isEmpty()) {
            nameField.setPromptText("Name is required!");
            nameField.setStyle("-fx-border-color: red;");
            return;
        }

        if (server.isConnected()) {
            JSONObject j = new JSONObject();
            j.put("Player_Name", playerName);
            j.put("Board_Size", boardSize);

            writer.println(j.toString());
        }
    }

    public void onOpponentFound() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }
}
