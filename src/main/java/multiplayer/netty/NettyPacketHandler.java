package multiplayer.netty;

import io.netty.channel.*;
import io.netty.util.concurrent.ScheduledFuture;
import multiplayer.packet.Packet;
import multiplayer.packet.PacketType;

import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public final class NettyPacketHandler extends SimpleChannelInboundHandler<Packet> {
    
    private volatile long ping;
    private volatile boolean shutdown;
    
    private final boolean isServer;
    private Channel channel;
    private Consumer<Packet> packetConsumer;
    private Consumer<Exception> socketCloseConsumer;
    private ScheduledFuture<?> heartbeatTask;
    
    public NettyPacketHandler(final boolean isServer,
                             final Consumer<Packet> packetConsumer,
                             final Consumer<Exception> socketCloseConsumer) {
        this.isServer = isServer;
        this.packetConsumer = packetConsumer;
        this.socketCloseConsumer = socketCloseConsumer;
        this.shutdown = false;
    }
    
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws
            Exception {
        this.channel = ctx.channel();
        
        // Start heartbeat for server connections (uncomment if needed)
        if (isServer) {
            heartbeatTask = ctx.executor().scheduleAtFixedRate(() -> {
                try {
                    sendHeartBeat();
                } catch (Exception e) {
                    System.err.println("Failed to send heartbeat: " + e.getMessage() + ", closing connection.");
                    e.printStackTrace();
                    shutdown();
                }
            }, 1, 5, TimeUnit.SECONDS);
        }
        
        super.channelActive(ctx);
    }
    
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Packet packet) throws Exception {
        if (shutdown) return;
        
        NettyPacketManager.getInstance().receivePacket(packet);
        if (packetConsumer != null) {
            packetConsumer.accept(packet);
        }
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.err.println("Failed to handle packet: " + cause.getMessage());
        cause.printStackTrace();
        if (socketCloseConsumer != null) {
            socketCloseConsumer.accept(new Exception(cause));
        }
        ctx.close();
    }
    
    public long getPing() {
        return ping;
    }
    
    public SocketAddress getRemoteAddress() {
        return channel != null ? channel.remoteAddress() : null;
    }
    
    public void sendPacket(final Packet packet) {
        sendPacket(packet, null);
    }
    
    public void sendPacket(final Packet packet, final Consumer<Packet> echoCallback) {
        if (shutdown || channel == null || !channel.isOpen()) {
            if (socketCloseConsumer != null) {
                socketCloseConsumer.accept(new Exception("PacketHandler is closed or channel is not active."));
            }
            return;
        }
        
        try {
            NettyPacketManager.getInstance().sendPacket(packet, channel, echoCallback);
        } catch (Exception e) {
            if (socketCloseConsumer != null) {
                socketCloseConsumer.accept(e);
            }
            shutdown();
        }
    }
    
    public void setPacketConsumer(final Consumer<Packet> packetConsumer) {
        this.packetConsumer = packetConsumer;
    }
    
    public void setSocketCloseConsumer(final Consumer<Exception> socketCloseConsumer) {
        this.socketCloseConsumer = socketCloseConsumer;
    }
    
    public void shutdown() {
        if (shutdown) return;
        
        synchronized (this) {
            if (shutdown) return;
            this.shutdown = true;
        }
        
        // Cancel heartbeat task
        if (heartbeatTask != null && !heartbeatTask.isCancelled()) {
            heartbeatTask.cancel(true);
        }
        
        // Close channel
        if (channel != null && channel.isActive()) {
            channel.close();
        }
        
        // Remove from PacketManager
        NettyPacketManager pmInstance = NettyPacketManager.getInstance();
        if (pmInstance != null) {
            pmInstance.removePacketHandler(this);
        }
    }
    
    private void sendHeartBeat() {
        final long now = System.currentTimeMillis();
        sendPacket(
            new Packet(null, PacketType.HEARTBEAT),
            _ -> ping = System.currentTimeMillis() - now
        );
    }
}