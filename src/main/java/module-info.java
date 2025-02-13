module tictactoe {
    requires javafx.controls;
    requires javafx.fxml;
    requires json.simple;


    opens tictactoe to javafx.fxml;
    exports tictactoe;
}