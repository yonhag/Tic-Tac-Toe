package Shared;

import Shared.Protocol.ProtocolManager;
import java.io.*;
import java.net.Socket;

public class SocketManager {
    private final Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public SocketManager(Socket socket) throws IOException {
        if (socket == null) {
            throw new IllegalArgumentException("Socket cannot be null.");
        }
        this.socket = socket;
        if (socket.isConnected()) {
            this.out = new ObjectOutputStream(socket.getOutputStream());
            this.out.flush();
            this.in = new ObjectInputStream(socket.getInputStream());
        }
    }

    public void sendMessage(ProtocolManager message) {
        try {
            out.writeObject(message);
            out.flush();
            System.out.println("Sent: " + message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ProtocolManager readMessage() throws IOException, ClassNotFoundException {
        ProtocolManager message = (ProtocolManager) in.readObject();
        System.out.println("Received: " + message);
        return message;
    }

    public void close() throws IOException {
        socket.close();
    }
}
