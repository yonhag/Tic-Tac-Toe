package Server.Database;

public class Player extends BaseEntity {

    private String username;

    public Player(String name) {
        this.username = name;
    }

    public Player() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String name) {
        this.username = name;
    }
}
