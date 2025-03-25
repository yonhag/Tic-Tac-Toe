module Client {
    requires javafx.controls;
    requires javafx.fxml;
    requires json.simple;
    requires java.desktop;
    requires java.sql;
    requires jBCrypt;
    requires mysql.connector.j;


    opens Client to javafx.fxml;
    exports Client;
    exports Server.Backend;
    exports Server.Database;
    opens Server.Database to javafx.fxml;
    exports Shared;
    opens Shared to javafx.fxml;
    exports Shared.Protocol;
    opens Shared.Protocol to javafx.fxml;
    exports Shared.Player;
    opens Shared.Player to javafx.fxml;
}