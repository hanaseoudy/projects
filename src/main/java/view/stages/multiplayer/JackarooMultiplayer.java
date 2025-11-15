package view.stages.multiplayer;

import javafx.stage.Stage;
import multiplayer.netty.NettyPacketManager;
import view.JackarooManager;
import view.middleware.frontend.JackarooController;

public class JackarooMultiplayer {

    public void start(final Stage stage) {
        JackarooManager.goToScene(stage, "Multiplayer", _ -> {
            stage.setTitle("Jackaroo Multiplayer");

            stage.setResizable(false);

            attach(stage);
        });
    }

    public void startHost(final Stage stage) {
        JackarooManager.goToScene(stage, "Host", _ -> {
            stage.setTitle("Jackaroo Host");

            stage.setResizable(false);

            attach(stage);
        });
    }

    public void startJoin(final Stage stage) {
        JackarooManager.goToScene(stage, "Join", _ -> {
            stage.setTitle("Jackaroo Join");

            stage.setResizable(false);

            attach(stage);
        });
    }

    private void attach(final Stage stage) {
        stage.setOnCloseRequest(_ -> {
            JackarooController.shutdownMultiplayer();
            NettyPacketManager.getInstance().shutdown();
        });
    }

}
