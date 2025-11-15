package multiplayer.netty.room;

import engine.Game;
import io.netty.channel.Channel;
import io.netty.channel.socket.SocketChannel;
import javafx.application.Platform;
import javafx.stage.Stage;
import model.player.Player;
import multiplayer.netty.engine.NettyServer;
import multiplayer.netty.room.player.NettyOnlinePlayer;
import multiplayer.packet.Packet;
import multiplayer.packet.PacketType;
import view.middleware.Middleware;
import view.stages.JackarooBoard;
import view.stages.JackarooError;
import view.stages.multiplayer.JackarooRoom;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public final class NettyRoom implements Serializable {

    private final String username;
    private final transient Stage stage;
    private final Set<NettyRoomPlayer> players;
    private transient boolean open, started;
    private final transient NettyServer server;

    // Map to track channels to players (for pending connections)
    private transient final ConcurrentHashMap<Channel, NettyRoomPlayer> channelPlayerMap;

    public NettyRoom(final Stage stage, final String username, final int port) {
        this.open = true;
        this.stage = stage;
        this.username = username;
        this.players = ConcurrentHashMap.newKeySet(); // Thread-safe set
        this.channelPlayerMap = new ConcurrentHashMap<>();

        // Create server with custom packet consumer that handles new connections
        this.server = new NettyServer(port,
                null, // Don't handle packets at room level
                this::handleDisconnection,
                this::join
        );

        try {
            server.start();
            System.out.println("Room server started on port " + port);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            new JackarooError("Failed to start room server: " + e.getMessage());
        }
    }

    private void join(final SocketChannel socketChannel) {
        // Create a pending player but don't add to players set yet
        // The player will be added when they send their NAME packet
        NettyRoomPlayer pendingPlayer = new NettyRoomPlayer(this, "Pending Player " + (channelPlayerMap.size() + 1), socketChannel);
        channelPlayerMap.put(socketChannel, pendingPlayer);
    }

    private void handleDisconnection(Exception exception) {
        System.err.println("Room connection error: " + exception.getMessage());
    }

    public void broadcast(final Packet packet) throws IOException {
        broadcast(packet, null);
    }

    public void broadcast(final Packet packet, final Consumer<Packet> callback) throws IOException {
        broadcast(packet, callback, null);
    }

    public void broadcast(final Packet packet, final Consumer<Packet> callback, final Set<NettyRoomPlayer> excluded) throws IOException {
        if (packet.getPacketType() == PacketType.UPDATE_FIREPIT) {
            System.out.println("Sending update firepit packet!");
        }

        if (!started) {
            final Set<NettyRoomPlayer> players = new HashSet<>(this.players);
            if (excluded != null && !excluded.isEmpty())
                players.removeAll(excluded);

            for (final NettyRoomPlayer player : players)
                player.sendPacket(packet, callback);
            return;
        }

        for (final Player player : Middleware.getInstance().getGame().getPlayers()) {
            if (player instanceof NettyOnlinePlayer onlinePlayer) {
                if (!onlinePlayer.isCPU()) {
                    onlinePlayer.sendPacket(packet, callback);
                }
            }
        }
    }

    public void start() throws IOException {
        open = false;
        started = true;

        // Creating the game with the room players
        final Game game = new Game(username, players);

        // Loading board for host
        new JackarooBoard().start(stage, game, this);
    }

    public int getSize() {
        return players.size();
    }

    public void addPlayer(final NettyRoomPlayer player) throws IOException {
        players.add(player);

        Platform.runLater(() -> new JackarooRoom().start(stage, this));

        broadcast(new Packet(this, PacketType.UPDATE_ROOM), null, Collections.singleton(player));
    }

    public void removePlayer(final NettyRoomPlayer player) {
        players.remove(player);

        // Remove from channel mapping
        channelPlayerMap.values().removeIf(p -> p.equals(player));

        Platform.runLater(() -> new JackarooRoom().start(stage, this));

        try {
            broadcast(new Packet(this, PacketType.UPDATE_ROOM));
        } catch (final IOException e) {
            new JackarooError(e.getMessage());
        }
    }

    public NettyRoomPlayer getPlayerByChannel(Channel channel) {
        return channelPlayerMap.get(channel);
    }

    public void shutdown() {
        open = false;
        if (server != null) {
            server.shutdown();
        }

        // Shutdown all player handlers
        for (NettyRoomPlayer player : players) {
            player.shutdown();
        }

        // Shutdown pending players too
        for (NettyRoomPlayer player : channelPlayerMap.values()) {
            player.shutdown();
        }
    }

    public Set<NettyRoomPlayer> getPlayers() {
        return players;
    }

    public String getUsername() {
        return username;
    }

    public NettyServer getServer() {
        return server;
    }
}