package tictactoeserver;

import java.sql.*;
import org.mindrot.jbcrypt.BCrypt;

public class DatabaseHandler {
    // 1) This URL connects to MySQL *without specifying a database*, allowing us to create one if needed.
    private static final String DB_URL = "jdbc:mysql://localhost:3306/tictactoe?useSSL=false&serverTimezone=UTC";

    // MySQL credentials
    private static final String USER     = "root";   // Change to your MySQL username
    private static final String PASSWORD = "yona";   // Change to your MySQL password


    /**
     * Constructor. Automatically ensures the database and tables exist.
     */
    public DatabaseHandler() throws ClassNotFoundException {
        Class.forName("com.mysql.cj.jdbc.Driver");
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
            String salt = BCrypt.gensalt();
            String hashedPassword = BCrypt.hashpw(plainTextPassword, salt);


            stmt.setString(1, username);
            stmt.setString(2, hashedPassword);
            stmt.setString(3, name);
            stmt.setString(4, email);

            System.out.println(username + name + email);
            System.out.println(stmt);

            int rowsInserted = stmt.executeUpdate();
            System.out.println(rowsInserted);
            return rowsInserted > 0;
        } catch (java.sql.SQLIntegrityConstraintViolationException e) {
            System.out.println("Username already exists");
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean saveGameResult(String playerX, String playerO, String winner, Timestamp dateEnded) {
        String query = "INSERT INTO games (PlayerXUsername, PlayerOUsername, WinnerUsername, DateEnded) VALUES (?, ?, ?, ?)";

        try (Connection conn = connectToGameDB();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, playerX);
            stmt.setString(2, playerO);
            stmt.setString(3, winner);  // Winner can be null if it's a draw
            stmt.setTimestamp(4, dateEnded);

            int rowsInserted = stmt.executeUpdate();
            return rowsInserted > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
