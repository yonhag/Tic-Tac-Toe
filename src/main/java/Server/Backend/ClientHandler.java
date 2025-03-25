package Server.Backend;

import Server.Database.PlayerDB;
import Shared.Protocol.MessageType;
import Shared.Protocol.ProtocolManager;
import Shared.Player.Player;
import Shared.Protocol.BoardMove;
import Shared.SocketManager;

import java.io.IOException;
import java.sql.SQLException;

public class ClientHandler implements Runnable {
    private final SocketManager socket;
    private final ServerConnection serverConnection;
    private GameSession session;
    private Player player;

    public ClientHandler(SocketManager socket, ServerConnection serverConnection) {
        this.socket = socket;
        this.session = null;
        this.serverConnection = serverConnection;
    }

    public void sendMessage(ProtocolManager msg) {
        System.out.println("Message Sent to " + player + ": " + msg);
        socket.sendMessage(msg);
    }

    public ProtocolManager receiveMessage() throws IOException, ClassNotFoundException {
        ProtocolManager protocol = socket.readMessage();
        System.out.println("Message Received from " + player + ": " + protocol);
        return protocol;
    }

    @Override
    public void run() {
        try {
            ProtocolManager message;
            boolean loginFlag = true;
            do {
                try {
                    message = receiveMessage();
                    if (message != null) {
                        if (message.getType() == MessageType.LOGIN) {
                            handleLogin(message);
                            loginFlag = false;
                        } else if (message.getType() == MessageType.DISCONNECT) {
                            handleDisconnect();
                            loginFlag = false;
                        }
                    }
                } catch (IOException | ClassNotFoundException e) {
                    loginFlag = false;
                }
            } while (loginFlag);

            boolean dataFlag = true;
            do {
                try {
                    message = receiveMessage();
                    if (message != null && message.getType() == MessageType.MOVE) {
                        handleMove(message);
                    } else if (message != null && message.getType() == MessageType.DISCONNECT) {
                        handleDisconnect();
                        dataFlag = false;
                    }
                } catch (IOException | ClassNotFoundException e) {
                    dataFlag = false;
                }
            } while (dataFlag);

        } catch (Exception e) {
            sendMessage(new ProtocolManager(MessageType.DISCONNECT, ""));
            System.out.println("Error");
            e.printStackTrace();
        }
    }

    private void handleLogin(ProtocolManager message) throws IOException {
        this.player = (Player) message.getData();
        if (!PlayerDB.playerExists(player.getName())) {
            PlayerDB.savePlayer(player);
            System.out.println("Created new user: " + player.getName());
        } else {
            System.out.println("User already exists: " + player.getName());
        }
        /*
        if (!PlayerDB.playerExists(player.getName())) {
            sendMessage(new ProtocolManager(MessageType.LOGIN_FAILED, "User not found. Please sign up."));
            return;
        }
        */
        serverConnection.matchPlayer(player.getSize(), this);
    }


    private void handleDisconnect() throws IOException {
        System.out.println("Disconnected");
        this.socket.close();
    }

    private void handleMove(ProtocolManager message) throws SQLException {
        System.out.println("Processing move...");
        BoardMove boardMove = (BoardMove) message.getData();
        this.session.makeMove(this, boardMove.getX(), boardMove.getY());
    }

    public void setSession(GameSession session) {
        this.session = session;
    }

    public Player getPlayer() {
        return player;
    }
}