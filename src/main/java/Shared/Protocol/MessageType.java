package Shared.Protocol;

import java.io.Serializable;

public enum MessageType implements Serializable {
    LOGIN, DISCONNECT, MOVE, START_GAME, WAIT_FOR_PLAYER, GAME_STATUS, INVALID_MOVE, VALID_MOVE
}
