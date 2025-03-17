package tictactoeserver;

import java.sql.*;
import org.mindrot.jbcrypt.BCrypt;

public class DatabaseHandler {
    // 1) This URL connects to MySQL *without specifying a database*, allowing us to create one if needed.
    private static final String DB_URL = "jdbc:mysql://localhost:3306/tictactoe?useSSL=false&serverTimezone=UTC";

    // MySQL credentials
    private static final String USER     = "root";   // Change to your MySQL username
    private static final String PASSWORD = "yona";   // Change to your MySQL password

    // Script to create the database, tables, etc. if they don't exist
    private static final String CREATE_DB_SCRIPT = """
        SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS;
        SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS;
        SET @OLD_SQL_MODE=@@SQL_MODE;
        SET UNIQUE_CHECKS=0;
        SET FOREIGN_KEY_CHECKS=0;
        SET SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

        CREATE SCHEMA IF NOT EXISTS `TicTacToe` DEFAULT CHARACTER SET utf8;
        USE `TicTacToe`;

        CREATE TABLE IF NOT EXISTS `TicTacToe`.`Users` (
          `username` VARCHAR(45) NOT NULL,
          `password` VARCHAR(60) NOT NULL,
          `name` VARCHAR(45) NOT NULL,
          `email` VARCHAR(45) NOT NULL,
          PRIMARY KEY (`username`),
          UNIQUE INDEX `email_UNIQUE` (`email` ASC) VISIBLE,
          UNIQUE INDEX `username_UNIQUE` (`username` ASC) VISIBLE
        )
        ENGINE = InnoDB;

        CREATE TABLE IF NOT EXISTS `TicTacToe`.`Games` (
          `PlayerXUsername` VARCHAR(45) NOT NULL,
          `PlayerOUsername` VARCHAR(45) NOT NULL,
          `WinnerUsername` VARCHAR(45) NULL,
          `DateEnded` DATETIME NOT NULL,
          PRIMARY KEY (`DateEnded`, `PlayerXUsername`, `PlayerOUsername`),
          INDEX `fk_Games_Users_idx` (`PlayerOUsername` ASC) VISIBLE,
          INDEX `fk_Games_Users1_idx` (`PlayerXUsername` ASC) VISIBLE,
          CONSTRAINT `fk_Games_Users`
            FOREIGN KEY (`PlayerOUsername`)
            REFERENCES `TicTacToe`.`Users` (`username`)
            ON DELETE NO ACTION
            ON UPDATE NO ACTION,
          CONSTRAINT `fk_Games_Users1`
            FOREIGN KEY (`PlayerXUsername`)
            REFERENCES `TicTacToe`.`Users` (`username`)
            ON DELETE NO ACTION
            ON UPDATE NO ACTION
        )
        ENGINE = InnoDB;

        SET SQL_MODE=@OLD_SQL_MODE;
        SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
        SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
        """;

    /**
     * Constructor. Automatically ensures the database and tables exist.
     */
    public DatabaseHandler() throws ClassNotFoundException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        createDatabaseIfNotExists();
    }

    /**
     * 1) Connects to MySQL *without specifying a database*.
     * 2) Runs the SQL script to create the `TicTacToe` schema and tables if they do not exist.
     */
    private void createDatabaseIfNotExists() {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {
            stmt.execute(CREATE_DB_SCRIPT);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns a Connection *to the TicTacToe database*.
     */
    private Connection connectToGameDB() throws SQLException {
        return DriverManager.getConnection(DB_URL, USER, PASSWORD);
    }

    /**
     * Validate user login:
     * 1) Fetch the stored BCrypt hash from the database by username.
     * 2) Compare it to the plain-text password using BCrypt.
     */
    public boolean validateLogin(String username, String plainTextPassword) {
        String query = "SELECT password FROM Users WHERE username = ?";
        try (Connection conn = connectToGameDB();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String storedHash = rs.getString("password");
                // Compare plain-text password with stored BCrypt hash
                return BCrypt.checkpw(plainTextPassword, storedHash);
            }
            return false;  // username not found
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Create a new user:
     * 1) Hash the plain-text password with BCrypt.
     * 2) Insert hashed password and other details into the database.
     */
    public boolean createUser(String username, String plainTextPassword, String name, String email) {
        String query = "INSERT INTO Users (username, password, name, email) VALUES (?, ?, ?, ?)";
        try (Connection conn = connectToGameDB();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            // Hash the plain-text password before storing
            String hashedPassword = BCrypt.hashpw(plainTextPassword, BCrypt.gensalt());

            stmt.setString(1, username);
            stmt.setString(2, hashedPassword);
            stmt.setString(3, name);
            stmt.setString(4, email);

            int rowsInserted = stmt.executeUpdate();
            return rowsInserted > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
