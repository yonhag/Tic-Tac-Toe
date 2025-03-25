package Shared.Protocol;

import java.io.Serializable;

public class ProtocolManager implements Serializable {
    private final MessageType type;
    private final Serializable data;

    public ProtocolManager(MessageType type, Serializable data) {
        this.type = type;
        this.data = data;
    }

    public MessageType getType() {
        return type;
    }

    public Serializable getData(){
        return data;
    }

    @Override
    public String toString() {
        return "ProtocolManager{" +
                "type=" + type +
                ", data='" + data + '\'' +
                '}';
    }
}
