package multiplayer;

import javafx.application.Platform;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import model.card.Card;
import model.player.Marble;
import multiplayer.game.ActionResult;
import multiplayer.netty.NettyPacketHandler;
import multiplayer.netty.engine.NettyClient;
import multiplayer.netty.room.JoinPacket;
import multiplayer.netty.room.NettyRoom;
import multiplayer.packet.ChatPacket;
import multiplayer.packet.Packet;
import multiplayer.packet.PacketType;
import multiplayer.packet.SelectCardPacket;
import view.middleware.Middleware;
import view.middleware.frontend.JackarooController;
import view.stages.JackarooBoard;
import view.stages.JackarooError;
import view.stages.multiplayer.JackarooRoom;

import java.io.IOException;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Used for joining other player's host
public final class MultiplayerHandler {

    private int id;
    private final Stage stage;
    private NettyClient client;
    private final String username;
    private NettyPacketHandler handler;
    private final ExecutorService executor;

    public MultiplayerHandler(final Stage stage, final String username, final String ip) {
        this.stage = stage;
        this.username = username;
        this.executor = Executors.newSingleThreadExecutor();

        executor.submit(() -> {
            try {
                final String[] split = ip.split(":");

                client = new NettyClient(split[0], Integer.parseInt(split[1]),
                        this::resolvePacket,
                        e -> {
                            e.printStackTrace();
                            leaveRoom(e.getMessage());
                        });

                client.connect();

                handler = client.getPacketHandler();

                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        try {
                            joinRoom();
                        } catch (IOException e) {
                            new JackarooError(e.getMessage());
                        }
                    }
                }, 1000);
            } catch (final Exception e) {
                new JackarooError(e.getMessage());
            }
        });
    }

    public void selectCard(final Card card) {
        try {
            Rectangle rectangle = null;

            for (final Map.Entry<Rectangle, Card> entry : Middleware.getInstance().getJackarooConnector().getCards().entrySet()) {
                if (entry.getValue().equals(card)) {
                    rectangle = entry.getKey();
                    break;
                }
            }

            if (rectangle == null) {
                Platform.runLater(() -> new JackarooError("Couldn't fetch card view component from clicked card!"));
                return;
            }

            final Rectangle finalRectangle = rectangle;
            handler.sendPacket(new Packet(new SelectCardPacket(id, card), PacketType.SELECT_CARD, true), echo -> {
                if (echo.getPacketType() == PacketType.SUCCESS) {
                    Middleware.getInstance().updateSelectedCard(card, finalRectangle);
                    finalRectangle.setStyle("-fx-stroke: #69cbf2; -fx-stroke-width: 1; -fx-effect: dropshadow(gaussian, #69cbf2, 10, 0.8, 0, 0);");
                    return;
                }

                Platform.runLater(() -> new JackarooError(echo.getPacketData().toString()));
            });
        } catch (final Exception e) {
            new JackarooError(e.getMessage());
        }
    }

    public void deselect(final Marble marble) {
        try {
            final Circle circle = Middleware.getInstance().getJackarooConnector().getCircleByMarble(marble);

            if (circle == null) {
                Platform.runLater(() -> new JackarooError("Couldn't fetch cell view component from clicked marble!"));
                return;
            }

            handler.sendPacket(new Packet(marble, PacketType.DESELECT_MARBLE, true), echo -> {
                if (echo.getPacketType() == PacketType.SUCCESS) {
                    Middleware.getInstance().removeFromSelectedMarbles(marble, circle);
                    return;
                }

                Platform.runLater(() -> new JackarooError(echo.getPacketData().toString()));
            });
        } catch (final Exception e) {
            new JackarooError(e.getMessage());
        }
    }

    public void select(final Marble marble) {
        try {
            final Circle circle = Middleware.getInstance().getJackarooConnector().getCircleByMarble(marble);

            if (circle == null) {
                Platform.runLater(() -> new JackarooError("Couldn't fetch cell view component from clicked marble!"));
                return;
            }

            if (Middleware.getInstance().getSelectedMarbles().contains(marble)) {
                deselect(marble);
                return;
            }

            handler.sendPacket(new Packet(marble, PacketType.SELECT_MARBLE, true), echo -> {
                if (echo.getPacketType() == PacketType.SUCCESS) {
                    Middleware.getInstance().addToSelectedMarbles(marble, circle);
                    return;
                }

                Platform.runLater(() -> new JackarooError(echo.getPacketData().toString()));
            });
        } catch (final Exception e) {
            new JackarooError(e.getMessage());
        }
    }

    public void play() {
        try {
            handler.sendPacket(new Packet(id, PacketType.PLAY));
        } catch (final Exception e) {
            new JackarooError(e.getMessage());
        }
    }

    public void shutdown() {
        handler.shutdown();
        executor.shutdownNow();
    }

    private void joinRoom() throws IOException {
        handler.sendPacket(new Packet(username, PacketType.NAME, true), echo -> {
            switch (echo.getPacketType()) {
                case ERROR -> new JackarooError(echo.getPacketData().toString());
                case SUCCESS -> {
                    // Connection established and username exchanged
                    final JoinPacket joinPacket = (JoinPacket) echo.getPacketData();

                    id = joinPacket.getId();

                    showRoom(joinPacket.getRoom());
                }
            }
        });
    }

    private void resolvePacket(final Packet packet) {
        switch (packet.getPacketType()) {
            case UPDATE_ROOM ->
                    showRoom((NettyRoom) packet.getPacketData());
            case UPDATE_GAME -> {
                final ActionResult actionResult = (ActionResult) packet.getPacketData();

                // Enhanced debugging with null checks
                if (actionResult == null) {
                    return;
                }

                if (actionResult.getPlayers() == null || actionResult.getPlayers().isEmpty()) {
                    return;
                }

                updateGame(actionResult);
            }
            case UPDATE_FIREPIT ->
                    Middleware.getInstance().getJackarooUpdater().updateFirePit((Card) packet.getPacketData());
            case ERROR -> {
                Platform.runLater(() -> new JackarooError(packet.getPacketData().toString()));
            }

            case CHAT -> {
                final ChatPacket chatPacket = (ChatPacket) packet.getPacketData();
                JackarooController.chatBox.addMessage(chatPacket.getUsername(), chatPacket.getMessage());
            }
        }
    }

    public void sendChatMessage(final String message) {
        handler.sendPacket(new Packet(
                new ChatPacket(username, message),
                PacketType.CHAT
        ));
    }

    private void leaveRoom() {

    }

    private void leaveRoom(final String errorMessage) {
        leaveRoom();

        Platform.runLater(() -> new JackarooError(errorMessage));
    }

    private void showRoom(final NettyRoom room) {
        Platform.runLater(() -> new JackarooRoom().start(stage, room, true));
    }

    private void updateGame(final ActionResult result) {
        if (Middleware.getInstance() == null) {
            Platform.runLater(() -> new JackarooBoard().start(stage, result, id));
            return;
        }

        Middleware.getInstance().getJackarooConnector().updateResult(result);

        Middleware.getInstance().getJackarooUpdater().updateBoard(result);
    }

}