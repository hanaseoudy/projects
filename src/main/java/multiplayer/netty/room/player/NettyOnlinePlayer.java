package multiplayer.netty.room.player;

import engine.board.BoardManager;
import exception.GameException;
import exception.InvalidCardException;
import exception.InvalidMarbleException;
import exception.SplitOutOfRangeException;
import model.Colour;
import model.card.Card;
import model.player.CPU;
import model.player.Marble;
import multiplayer.netty.NettyPacketHandler;
import multiplayer.packet.ChatPacket;
import multiplayer.packet.Packet;
import multiplayer.packet.PacketType;
import multiplayer.packet.SelectCardPacket;
import view.middleware.Middleware;
import view.middleware.frontend.JackarooController;

import java.util.function.Consumer;

public final class NettyOnlinePlayer extends CPU {

    private boolean cpu;
    private transient final NettyPacketHandler handler;

    public NettyOnlinePlayer(final String name, final Colour colour,
                            final BoardManager boardManager, final NettyPacketHandler handler) {
        super(name, colour, boardManager);

        this.handler = handler;

        this.handler.setPacketConsumer(this::resolvePacket);
        this.handler.setSocketCloseConsumer(_ -> this.cpu = true);

        this.cpu = false;
    }

    private void resolvePacket(final Packet packet) {
        switch (packet.getPacketType()) {
            case PLAY:
                handlePlay(packet);
                break;
            case SELECT_CARD:
                handleSelectCard(packet);
                break;
            case SELECT_MARBLE:
                handleSelectMarble(packet);
                break;
            case DESELECT_MARBLE:
                handleDeselectMarble(packet);
                break;
            case SPLIT_DISTANCE:
                handleSplitDistance(packet);
                break;
            case CHAT:
                final ChatPacket chatPacket = (ChatPacket) packet.getPacketData();
                JackarooController.chatBox.addMessage(chatPacket.getUsername(), chatPacket.getMessage());
                break;
        }
    }

    private void handlePlay(final Packet packet) {
        if ((int) packet.getPacketData() != Middleware.getInstance().getGame().getActivePlayerIndex()) {
            sendPacket(new Packet("Not your turn!", PacketType.ERROR));
            return;
        }

        Middleware.getInstance().getJackarooLinker().play(false);
    }

    private void handleSelectCard(final Packet packet) {
        final SelectCardPacket selectCardPacket = (SelectCardPacket) packet.getPacketData();

        if (selectCardPacket.getId() != Middleware.getInstance().getGame().getActivePlayerIndex())
            return;

        try {
            selectCard(selectCardPacket.getCard());
            sendPacket(new Packet(null, PacketType.SUCCESS, packet.getId()), _ -> {
            });
        } catch (final InvalidCardException e) {
            sendPacket(new Packet(e.getMessage(), PacketType.ERROR, packet.getId()), _ -> {
            });
        }
    }

    private void handleSelectMarble(final Packet packet) {
        if (packet.getPacketData() instanceof Marble marble) {
            try {
                selectMarble(marble);
                sendPacket(new Packet(null, PacketType.SUCCESS, packet.getId()), _ -> {
                });
            } catch (final InvalidMarbleException e) {
                sendPacket(new Packet(e.getMessage(), PacketType.ERROR));
            }
            return;
        }

        sendPacket(new Packet("Invalid packet data for SELECT_MARBLE", PacketType.ERROR));
    }

    private void handleDeselectMarble(final Packet packet) {
        if (packet.getPacketData() instanceof Marble marble) {
            deselectMarble(marble);
            return;
        }

        sendPacket(new Packet("Invalid packet data for DESELECT_MARBLE", PacketType.ERROR));
    }

    private void handleSplitDistance(final Packet packet) {
        try {
            Middleware.getInstance().getGame().editSplitDistance((int) packet.getPacketData());
            Middleware.getInstance().getJackarooLinker().play(true);
        } catch (final SplitOutOfRangeException e) {
            sendPacket(new Packet(e.getMessage(), PacketType.ERROR, packet.getId()));
        }
    }

    public void sendPacket(final Packet packet) {
        sendPacket(packet, null);
    }

    public void sendPacket(final Packet packet, final Consumer<Packet> echo) {
        handler.sendPacket(packet, echo);
    }

    @Override
    public void play() throws GameException {
        if (cpu) {
            // Play as the CPU, meaning the player has left.
            super.play();
        } else {
            if (Middleware.getInstance().getGame().getActivePlayer() == this) {
                super.playerPlay();
            }
        }
    }

    @Override
    public boolean isCPU() {
        return cpu;
    }
    
    public NettyPacketHandler getHandler() {
        return handler;
    }
}