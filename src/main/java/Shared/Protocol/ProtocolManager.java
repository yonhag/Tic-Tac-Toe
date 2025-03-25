package Shared.Protocol;

import java.io.Serializable;

public record ProtocolManager(MessageType type, Serializable data) implements Serializable {

    @Override
    public String toString() {
        return "ProtocolManager{" +
                "type=" + type +
                ", data='" + data + '\'' +
                '}';
    }
}
