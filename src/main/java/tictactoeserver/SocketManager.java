package tictactoeserver;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class SocketManager {
    Socket socket;

    public SocketManager(Socket socket) {
        this.socket = socket;
    }

    private void sendData(String data) throws IOException {
        PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
        writer.println(data);
    }

    private String getData() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        return reader.readLine();
    }

    private void sendJSON(JSONObject data) throws IOException {
        PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
        writer.println(data);
    }

    private JSONObject getJSON() throws IOException, ParseException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject)parser.parse(reader.readLine());

        return json;
    }
}
