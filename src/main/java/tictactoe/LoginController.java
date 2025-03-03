package tictactoe;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

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

    // Database connection placeholder
    private DatabaseHandler databaseHandler;

    public void initialize() {
        databaseHandler = new DatabaseHandler(); // Initialize database connection

        loginButton.setOnAction(event -> handleLogin());
        signupButton.setOnAction(event -> handleSignup());
    }

    private void handleLogin() {
        String username = loginUsername.getText();
        String password = loginPassword.getText();

        if (databaseHandler.validateLogin(username, password)) {
            System.out.println("Login successful!");
        } else {
            System.out.println("Invalid username or password.");
        }
    }

    private void handleSignup() {
        String fullName = signupFullName.getText();
        String email = signupEmail.getText();
        String username = signupUsername.getText();
        String password = signupPassword.getText();

        if (databaseHandler.createUser(fullName, email, username, password)) {
            System.out.println("Signup successful! You can now log in.");
        } else {
            System.out.println("Signup failed. Username or email may already be taken.");
        }
    }
}
