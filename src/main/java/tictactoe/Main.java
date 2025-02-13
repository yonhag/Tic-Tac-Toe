package tictactoe;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.InetAddress;

public class Main extends Application {
    private final int serverPort = 8000;

    private Menu openMenuWindow() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("menu.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        Menu menuController = fxmlLoader.getController();
        menuController.setSocket(InetAddress.getLocalHost(), serverPort);

        Stage stage = new Stage();
        stage.setTitle("Tic Tac Toe Menu");
        stage.setScene(scene);
        stage.show();

        return menuController;
    }

    private GameController openGameWindow(Player playerAgainst, String playerSign, int serverPort) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("game.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        GameController gameController = fxmlLoader.getController();
        gameController.startGame(playerAgainst, playerSign, InetAddress.getLocalHost(), serverPort);

        Stage stage = new Stage();
        stage.setTitle("Tic Tac Toe Game Against " + playerAgainst.getName());
        stage.setScene(scene);
        stage.show();

        return gameController;
    }

    @Override
    public void start(Stage stage) throws Exception {
        openMenuWindow();
    }
}
