package multiplayer.packet;

import java.io.Serializable;
import java.util.UUID;

public final class Packet implements Serializable {

    private final UUID id;
    private final boolean echo;
    private final Object packetData;
    private final PacketType packetType;

    public Packet(final Object packetData, final PacketType packetType) {
        this(packetData, packetType, false);
    }

    public Packet(final Object packetData, final PacketType packetType,
                  final boolean echo) {
        this(packetData, packetType, echo, UUID.randomUUID());
    }

    public Packet(final Object packetData, final PacketType packetType, final UUID id) {
        this(packetData, packetType, id != null, id);
    }

    public Packet(final Object packetData, final PacketType packetType,
                  final boolean echo, final UUID id) {
        this.id = id;
        this.packetData = packetData;
        this.packetType = packetType;
        this.echo = echo;
    }

    public UUID getId() {
        return id;
    }

    public boolean isEcho() {
        return echo;
    }

    public Object getPacketData() {
        return packetData;
    }

    public PacketType getPacketType() {
        return packetType;
    }
}