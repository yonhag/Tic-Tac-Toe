package tictactoe;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class Utilities {
    public final static int serverPort = 8000;
    public final static InetAddress serverIP = InetAddress.ofLiteral("127.0.0.1");

    public static void openMenuWindow(Socket socket, String username) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Utilities.class.getResource("menu.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        MenuController menuController = fxmlLoader.getController();
        menuController.setParameters(socket, username);

        Stage stage = new Stage();
        stage.setTitle("Tic Tac Toe Menu");
        stage.setScene(scene);
        stage.show();
    }
}
