package multiplayer.netty;

import multiplayer.packet.Packet;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public final class EchoManager {

    private final Map<UUID, Consumer<Packet>> callbacks;

    public EchoManager() {
        // To ensure thread safety, using ConcurrentHashMap
        this.callbacks = new ConcurrentHashMap<>();
    }

    public void registerCallback(final UUID packetId, final Consumer<Packet> callback) {
        callbacks.put(packetId, callback);
    }

    public void handleResponse(final Packet responsePacket) {
        final Consumer<Packet> callback = callbacks.remove(responsePacket.getId());
        if (callback != null)
            callback.accept(responsePacket);
    }
}
