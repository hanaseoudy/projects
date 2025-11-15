package multiplayer.netty.room;

import java.io.Serializable;

public final class JoinPacket implements Serializable {
    private final int id;
    private final NettyRoom room;
    
    public JoinPacket(int id, NettyRoom room) {
        this.id = id;
        this.room = room;
    }
    
    public int getId() {
        return id;
    }
    
    public NettyRoom getRoom() {
        return room;
    }
}