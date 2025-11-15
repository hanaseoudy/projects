package multiplayer.packet;

import java.io.Serializable;

public final class ChatPacket implements Serializable {

    private final String username, message;

    public ChatPacket(final String username, final String message) {
        this.username = username;
        this.message = message;
    }

    public String getUsername() {
        return username;
    }

    public String getMessage() {
        return message;
    }
}
