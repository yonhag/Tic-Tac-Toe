package tictactoe;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;

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

    private Socket server;

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
            Utilities.openMenuWindow(server, username);
            ((Stage) loginButton.getScene().getWindow()).close();
        } else {
            System.out.println("Invalid username or password.");
        }
    }

    private boolean validateLogin(String username, String password) {
        JSONObject j = new JSONObject();
        j.put("Username", username);
        j.put("Password", password);
        String request = RequestTypes.Login.getValue() + j.toString();

        try {
            server = new Socket();
            SocketAddress socketAddress = new InetSocketAddress(Utilities.serverIP, Utilities.serverPort);
            server.connect(socketAddress);

            PrintWriter writer = new PrintWriter(server.getOutputStream(), true);
            writer.println(request);

            BufferedReader reader = new BufferedReader(new InputStreamReader(server.getInputStream()));
            JSONObject response = (JSONObject) new JSONParser().parse(reader.readLine());

            return response.containsKey("Status") && (Boolean)response.get("Status");
        } catch(IOException e) {
            System.err.println("ERROR: Server isn't available");
            System.exit(-1);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    private void handleSignup() throws IOException {
        String fullName = signupFullName.getText();
        String email = signupEmail.getText();
        String username = signupUsername.getText();
        String password = signupPassword.getText();

        if (signup(fullName, email, username, password)) {
            Utilities.openMenuWindow(server, username);
            ((Stage) loginButton.getScene().getWindow()).close();
        } else {
            System.out.println("Signup failed. Username or email may already be taken.");
        }
    }

    private boolean signup(String name, String email, String username, String password) {
        JSONObject j = new JSONObject();
        j.put("Username", username);
        j.put("Password", password);
        j.put("Email", email);
        j.put("Name", name);
        String request = RequestTypes.Signup.getValue() + j.toString();

        try {
            server = new Socket();
            SocketAddress socketAddress = new InetSocketAddress(Utilities.serverIP, Utilities.serverPort);
            server.connect(socketAddress);

            PrintWriter writer = new PrintWriter(server.getOutputStream(), true);
            writer.println(request);

            BufferedReader reader = new BufferedReader(new InputStreamReader(server.getInputStream()));
            JSONObject response = (JSONObject) new JSONParser().parse(reader.readLine());

            return response.containsKey("Status") && (Boolean)response.get("Status");
        } catch(IOException e) {
            System.err.println("ERROR: Server isn't available");
            System.exit(-1);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return false;
    };
}
