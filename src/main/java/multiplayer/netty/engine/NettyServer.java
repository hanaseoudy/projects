package multiplayer.netty.engine;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import multiplayer.netty.NettyPacketHandler;
import multiplayer.netty.NettyPacketManager;
import multiplayer.packet.Packet;
import multiplayer.packet.codec.PacketCodec;

import java.util.function.Consumer;

public final class NettyServer {

    private final int port;
    private final Consumer<Packet> packetConsumer;
    private final Consumer<Exception> socketCloseConsumer;
    private final Consumer<SocketChannel> connectionCloseConsumer;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel serverChannel;

    public NettyServer(int port,
                       Consumer<Packet> packetConsumer,
                       Consumer<Exception> socketCloseConsumer,
                       Consumer<SocketChannel> connectionConsumer) {
        this.port = port;
        this.packetConsumer = packetConsumer;
        this.socketCloseConsumer = socketCloseConsumer;
        this.connectionCloseConsumer = connectionConsumer;
    }

    public void start() throws InterruptedException {
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            NettyPacketHandler handler = NettyPacketManager.getInstance()
                                    .createPacketHandler(true, packetConsumer, socketCloseConsumer);

                            ch.pipeline()
                                    .addLast(new PacketCodec())
                                    .addLast(handler);

                            connectionCloseConsumer.accept(ch);
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture future = bootstrap.bind(port).sync();
            serverChannel = future.channel();
            System.out.println("Server started on port " + port);

        } catch (Exception e) {
            shutdown();
            throw e;
        }
    }

    public void shutdown() {
        if (serverChannel != null) {
            serverChannel.close();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
    }
}