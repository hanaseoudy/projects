package view.stages.multiplayer;

import javafx.scene.effect.Glow;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import multiplayer.netty.room.NettyRoom;
import multiplayer.netty.room.NettyRoomPlayer;
import view.JackarooManager;
import view.middleware.frontend.JackarooController;

public final class JackarooRoom {

    public void start(final Stage stage, final NettyRoom room) {
        start(stage, room, false);
    }

    public void start(final Stage stage, final NettyRoom room, final boolean client) {
        JackarooManager.goToScene(stage, "Room", parent -> {
            stage.setTitle(room.getUsername() + "'s room");

            final VBox playersVBox = (VBox) parent.getScene().lookup("#playersView");
            playersVBox.getChildren().clear();

            playersVBox.getChildren().add(getUsernameText(room.getUsername()));

            System.out.println("PLAYERS: " + room.getPlayers().size());
            for (final NettyRoomPlayer roomPlayer : room.getPlayers()) {
                playersVBox.getChildren().add(getUsernameText(roomPlayer.getName()));
            }

            stage.setResizable(true);

            if (client)
                JackarooController.joining = false;
        });
    }

    private Text getUsernameText(final String username) {
        final Text usernameText = new Text(username);

        usernameText.setFill(Color.valueOf("#69cbf2"));
        usernameText.setStrokeType(StrokeType.OUTSIDE);
        usernameText.setStrokeWidth(0.0);
        usernameText.setFont(Font.font("Bauhaus 93", 15.0));
        usernameText.setEffect(new Glow(0.8));

        return usernameText;
    }

}
