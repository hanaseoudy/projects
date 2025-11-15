package multiplayer.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import multiplayer.packet.Packet;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public final class NettyPacketManager {
    
    private static NettyPacketManager instance;
    
    private final EchoManager echoManager;
    private final CopyOnWriteArrayList<NettyPacketHandler> packetHandlers;
    
    public NettyPacketManager() {
        instance = this;
        this.echoManager = new EchoManager();
        this.packetHandlers = new CopyOnWriteArrayList<>();
    }
    
    public static NettyPacketManager getInstance() {
        return instance;
    }
    
    public void sendPacket(final Packet packet, final Channel channel,
                          final Consumer<Packet> callback) {
        if (packet.isEcho() && callback != null) {
            echoManager.registerCallback(packet.getId(), callback);
        }
        
        ChannelFuture future = channel.writeAndFlush(packet);
        future.addListener(f -> {
            if (!f.isSuccess()) {
                System.err.println("Failed to send packet: " + f.cause().getMessage());
            }
        });
    }
    
    public NettyPacketHandler createPacketHandler(final boolean server,
                                                  final Consumer<Packet> packetConsumer,
                                                  final Consumer<Exception> socketCloseConsumer) {
        final NettyPacketHandler handler = new NettyPacketHandler(
            server, packetConsumer, socketCloseConsumer
        );
        
        packetHandlers.add(handler);
        return handler;
    }
    
    public void removePacketHandler(final NettyPacketHandler handler) {
        packetHandlers.remove(handler);
    }
    
    public void receivePacket(final Packet packet) {
        if (packet.isEcho()) {
            echoManager.handleResponse(packet);
        }
    }
    
    public void shutdown() {
        for (final NettyPacketHandler packetHandler : packetHandlers) {
            packetHandler.shutdown();
        }
    }
}