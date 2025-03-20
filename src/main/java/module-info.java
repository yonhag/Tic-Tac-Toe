module tictactoe {
    requires javafx.controls;
    requires javafx.fxml;
    requires json.simple;
    requires java.desktop;
    requires java.sql;
    requires jBCrypt;
    requires mysql.connector.j;


    opens tictactoe to javafx.fxml;
    exports tictactoe;
    exports tictactoeserver;
    opens tictactoeserver to javafx.fxml;
}