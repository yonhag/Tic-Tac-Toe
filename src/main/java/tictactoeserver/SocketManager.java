package tictactoeserver;


import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class SocketManager {
    private final Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;

    public SocketManager(Socket socket) throws IOException {
        if (socket == null) throw new IllegalArgumentException("Socket cannot be null.");
        this.socket = socket;
        if (socket.isConnected()) {
            this.writer = new PrintWriter(socket.getOutputStream(), true);
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }
    }

    public void connect(InetAddress serverIP, int serverPort) throws IOException {
        socket.connect(new InetSocketAddress(serverIP, serverPort));
        this.writer = new PrintWriter(socket.getOutputStream(), true);
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public void close() throws IOException {
        socket.close();
    }

    public void sendData(String data) {
        writer.println(data);
        System.out.println("Sent: " + data);
    }

    public String getData() throws IOException {
        String data = reader.readLine();
        System.out.println("Received: " + data);
        return data;
    }

    public void sendJSON(JSONObject data) {
        writer.println(data);
        System.out.println("Sent: " + data);
    }

    public JSONObject getJSON() throws IOException, ParseException {
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject)parser.parse(reader.readLine());

        System.out.println("Received: " + json);

        return json;
    }

    public boolean isClosed() {
        return socket.isClosed();
    }

    public boolean isConnected() {
        return socket.isConnected();
    }

    public InetAddress getInetAddress() {
        return socket.getInetAddress();
    }
}
