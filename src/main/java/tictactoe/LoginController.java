package tictactoe;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

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

    public void initialize() { // Initialize database connection
        loginButton.setOnAction(event -> {
            try {
                handleLogin();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        signupButton.setOnAction(event -> {
            try {
                handleSignup();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void handleLogin() throws IOException {
        String username = loginUsername.getText();
        String password = loginPassword.getText();

        if (validateLogin(username, password)) {
            Utilities.openMenuWindow(username);
            ((Stage) loginButton.getScene().getWindow()).close();
        } else {
            System.out.println("Invalid username or password.");
        }
    }

    private boolean validateLogin(String username, String password) {
        return true;
    }

    private void handleSignup() throws IOException {
        String fullName = signupFullName.getText();
        String email = signupEmail.getText();
        String username = signupUsername.getText();
        String password = signupPassword.getText();

        if (createUser(fullName, email, username, password)) {
            Utilities.openMenuWindow(username);
            ((Stage) loginButton.getScene().getWindow()).close();
        } else {
            System.out.println("Signup failed. Username or email may already be taken.");
        }
    }

    private boolean createUser(String name, String email, String username, String password) {
        return true;
    };
}
