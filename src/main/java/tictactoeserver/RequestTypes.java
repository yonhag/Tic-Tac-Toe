package tictactoeserver;

public enum RequestTypes {
    Signup('0'),
    Login('1'),
    EnterQueue('2');

    private final char value;

    RequestTypes(char value) {
        this.value = value;
    }

    public char getValue() {
        return value;
    }

    public static RequestTypes getRequestType(String request) {
        char c = request.charAt(0);
        for (RequestTypes type : RequestTypes.values()) {
            if (type.value == c) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid request type: " + c);
    }
}
