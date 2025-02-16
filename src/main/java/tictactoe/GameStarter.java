package tictactoe;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.InetAddress;

public class GameStarter extends Application {
    public static final int serverPort = 8000;

    private void openMenuWindow() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("menu.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        MenuController menuController = fxmlLoader.getController();
        menuController.setSocket(InetAddress.getLocalHost(), serverPort);

        Stage stage = new Stage();
        stage.setTitle("Tic Tac Toe Menu");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void start(Stage stage) throws Exception {

    }
}
