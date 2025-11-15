package view.stages;

import engine.Game;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import multiplayer.game.ActionResult;
import multiplayer.netty.NettyPacketManager;
import multiplayer.netty.room.NettyRoom;
import multiplayer.packet.Packet;
import multiplayer.packet.PacketType;
import view.JackarooManager;
import view.middleware.Middleware;
import view.middleware.frontend.JackarooController;
import view.middleware.frontend.Jackaroosounds;

import java.io.IOException;

public final class JackarooBoard {

    public void start(final Stage stage, final String username) throws Exception {
        start(stage, new Game(username), null);
    }

    public void start(final Stage stage, final Game game, final NettyRoom room) {
        JackarooManager.goToScene(stage, "Board", parent -> {
            new Middleware(parent, game);
            stage.setTitle("Jackaroo Board");

            stage.setResizable(false);

            stage.setOnCloseRequest(_ -> {
                Middleware.getInstance().getJackarooLinker().stopGame();
                JackarooController.shutdownMultiplayer();
                NettyPacketManager.getInstance().shutdown();
            });

            Jackaroosounds.stop();

            JackarooController.prepareChatBox((Pane) parent);

            if (room != null) {
                // Broadcasting update for board to all players
                try {
                    room.broadcast(new Packet(
                            Middleware.getInstance().getJackarooLinker().prepareActionResult(), PacketType.UPDATE_GAME)
                    );
                } catch (final IOException e) {
                    new JackarooError(e.getMessage());
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public void start(final Stage stage, final ActionResult result, final int id) {
        JackarooManager.goToScene(stage, "Board", parent -> {
            new Middleware(parent, result, id);
            stage.setTitle("Jackaroo Board");

            stage.setResizable(false);

            stage.setOnCloseRequest(_ -> {
                Middleware.getInstance().getJackarooLinker().stopGame();
                JackarooController.shutdownMultiplayer();
                NettyPacketManager.getInstance().shutdown();
            });

            Jackaroosounds.stop();

            JackarooController.prepareChatBox((Pane) parent);
            Middleware.getInstance().getJackarooConnector().updateResult(result);

            Middleware.getInstance().getJackarooUpdater().updateBoard(result);

        });
    }
}
