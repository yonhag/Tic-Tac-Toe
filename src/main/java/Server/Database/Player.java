package Server.Database;

public class Player extends BaseEntity {

    private String username;
    private String email;
    private String name;
    private String password;

    public Player(String name) {
        this.username = name;
        this.password = name;
        this.name = name;
        this.email = name + "@gmail.com";
    }

    public Player() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String name) {
        this.username = name;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }
}
