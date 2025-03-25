package Server.Backend;

public class MainServer {
    public static void main(String[] args) {
        ServerConnection server = new ServerConnection(8000);
        server.start();
    }
}
