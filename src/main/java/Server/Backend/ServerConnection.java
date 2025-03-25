package Server.Backend;

import Shared.Protocol.ProtocolManager;
import Shared.Protocol.MessageType;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerConnection {
    private int port;
    private ExecutorService pool;

    private Map<Integer, ClientHandler> waitingPlayers;

    public ServerConnection(int port) {
        this.port = port;
        pool = Executors.newFixedThreadPool(20);
        waitingPlayers = new ConcurrentHashMap<>();
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started on port " + port);
            while(true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(clientSocket, this);
                pool.execute(handler);
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void matchPlayer(int boardSize, ClientHandler handler) throws IOException {
        if (waitingPlayers.containsKey(boardSize)) {
            ClientHandler opponent = waitingPlayers.remove(boardSize);
            GameSession session = new GameSession(opponent, handler, boardSize);
            handler.setSession(session);
            opponent.setSession(session);
            session.startSession();
        } else {
            waitingPlayers.put(boardSize, handler);
            handler.sendMessage(new ProtocolManager(MessageType.WAIT_FOR_PLAYER, "please wait"));
        }
    }

}
