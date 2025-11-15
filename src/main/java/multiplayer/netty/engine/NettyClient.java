package multiplayer.netty.engine;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import multiplayer.netty.NettyPacketHandler;
import multiplayer.netty.NettyPacketManager;
import multiplayer.packet.Packet;
import multiplayer.packet.codec.PacketCodec;

import java.util.function.Consumer;

public final class NettyClient {

    private final String host;
    private final int port;
    private final Consumer<Packet> packetConsumer;
    private final Consumer<Exception> socketCloseConsumer;

    private EventLoopGroup group;
    private Channel channel;
    private NettyPacketHandler packetHandler;

    public NettyClient(String host, int port,
                       Consumer<Packet> packetConsumer,
                       Consumer<Exception> socketCloseConsumer) {
        this.host = host;
        this.port = port;
        this.packetConsumer = packetConsumer;
        this.socketCloseConsumer = socketCloseConsumer;
    }

    public void connect() throws InterruptedException {
        group = new NioEventLoopGroup();

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            packetHandler = NettyPacketManager.getInstance()
                                    .createPacketHandler(false, packetConsumer, socketCloseConsumer);

                            ch.pipeline()
                                    .addLast(new PacketCodec())
                                    .addLast(packetHandler);
                        }
                    });

            ChannelFuture future = bootstrap.connect(host, port).sync();
            channel = future.channel();
            System.out.println("Connected to " + host + ":" + port);

        } catch (Exception e) {
            shutdown();
            throw e;
        }
    }

    public NettyPacketHandler getPacketHandler() {
        return packetHandler;
    }

    public void shutdown() {
        if (packetHandler != null) {
            packetHandler.shutdown();
        }
        if (channel != null) {
            channel.close();
        }
        if (group != null) {
            group.shutdownGracefully();
        }
    }
}