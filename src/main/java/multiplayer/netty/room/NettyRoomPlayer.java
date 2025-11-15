package multiplayer.netty.room;

import io.netty.channel.Channel;
import io.netty.channel.socket.SocketChannel;
import multiplayer.netty.NettyPacketManager;
import multiplayer.packet.Packet;
import multiplayer.packet.PacketType;
import multiplayer.netty.NettyPacketHandler;

import java.io.IOException;
import java.io.Serializable;
import java.net.SocketAddress;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

public final class NettyRoomPlayer implements Serializable {

    private String name;
    private final NettyRoom room;
    private transient final Channel channel;
    private transient final NettyPacketHandler handler;

    public NettyRoomPlayer(final NettyRoom room, final String name, final SocketChannel channel) {
        this.room = room;
        this.name = name;
        this.channel = channel;

        channel.pipeline().removeLast();

        // Create packet handler for this specific player
        this.handler = NettyPacketManager.getInstance().createPacketHandler(
                true,
                this::resolvePacket,
                _ -> room.removePlayer(this)
        );
        channel.pipeline().addLast(handler);
    }

    private void resolvePacket(final Packet packet) {
        System.out.println(packet.getPacketType() + " - " + packet.getPacketData());
        switch (packet.getPacketType()) {
            case NAME -> {
                System.out.println(packet.getPacketData());
                if (packet.getPacketData() != null) {
                    name = packet.getPacketData().toString();
                    try {
                        System.out.println("Room size before adding: " + room.getSize());
                        if (room.getSize() >= 4) {
                            handler.sendPacket(new Packet("Sorry, but the room is full", PacketType.ERROR, packet.getId()));
                            return;
                        }

                        // Now add the player to the room (this matches the original behavior)
                        room.addPlayer(this);
                        final Packet successPacket = new Packet(new JoinPacket(room.getSize(), room), PacketType.SUCCESS, packet.getId());
                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                handler.sendPacket(successPacket, _ -> {});
                            }
                        }, 1000);
                    } catch (final IOException e) {
                        e.printStackTrace();
                        System.err.println("Couldn't send packet to NettyRoomPlayer " + name + ": " + e.getMessage());
                        leave();
                    }
                }
            }

            case LEAVE_ROOM -> leave();
        }
    }

    public String getName() {
        return name;
    }

    public void sendPacket(final Packet packet, final Consumer<Packet> callback) {
        handler.sendPacket(packet, callback);
    }

    public SocketAddress getRemoteAddress() {
        return handler.getRemoteAddress();
    }

    public NettyPacketHandler getHandler() {
        return handler;
    }

    public Channel getChannel() {
        return channel;
    }

    private void leave() {
        room.removePlayer(this);
        handler.shutdown();
    }

    public void shutdown() {
        if (handler != null) {
            handler.shutdown();
        }
    }
}