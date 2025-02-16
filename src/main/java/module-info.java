module tictactoe {
    requires javafx.controls;
    requires javafx.fxml;
    requires json.simple;
    requires java.desktop;


    opens tictactoe to javafx.fxml;
    exports tictactoe;
}