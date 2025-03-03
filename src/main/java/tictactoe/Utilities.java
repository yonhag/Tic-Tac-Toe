package tictactoe;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.InetAddress;

public class Utilities {
    public final static int serverPort = 8000;

    public static void openMenuWindow(String username) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Utilities.class.getResource("menu.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        MenuController menuController = fxmlLoader.getController();
        menuController.setParameters(InetAddress.getLocalHost(), serverPort, username);

        Stage stage = new Stage();
        stage.setTitle("Tic Tac Toe Menu");
        stage.setScene(scene);
        stage.show();
    }
}
