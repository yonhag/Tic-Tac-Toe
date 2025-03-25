package Client;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button; import javafx.scene.control.PasswordField; import javafx.scene.control.TextField; import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {
    @FXML
    private TextField loginUsername;
    @FXML
    private PasswordField loginPassword;
    @FXML
    private Button loginButton;

    @FXML
    private TextField signupFullName;
    @FXML
    private TextField signupEmail;
    @FXML
    private TextField signupUsername;
    @FXML
    private PasswordField signupPassword;
    @FXML
    private Button signupButton;

    public void initialize() {
        loginButton.setOnAction(_ -> {
            try {
                handleLogin();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        signupButton.setOnAction(_ -> {
            try {
                handleSignup();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void handleLogin() throws IOException {
        String username = loginUsername.getText().trim();
        String password = loginPassword.getText().trim();
        if (username.isEmpty() || password.isEmpty()) {
            System.out.println("Please enter both username and password.");
            return;
        }
        openMenuWindow(username);
        ((Stage) loginButton.getScene().getWindow()).close();
    }

    private void handleSignup() throws IOException {
        String fullName = signupFullName.getText().trim();
        String email = signupEmail.getText().trim();
        String username = signupUsername.getText().trim();
        String password = signupPassword.getText().trim();

        if (fullName.isEmpty() || email.isEmpty() || username.isEmpty() || password.isEmpty()) {
            System.out.println("Please fill out all signup fields.");
            return;
        }
        openMenuWindow(username);
        ((Stage) signupButton.getScene().getWindow()).close();
    }

    public static void openMenuWindow(String username) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(LoginController.class.getResource("menu.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        MenuController menuController = fxmlLoader.getController();
        menuController.setUsername(username);

        Stage stage = new Stage();
        stage.setTitle("Tic Tac Toe Menu");
        stage.setScene(scene);
        stage.show();
    }
}
