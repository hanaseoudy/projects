package view.middleware.frontend;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Arc;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.card.Card;
import multiplayer.MultiplayerHandler;
import multiplayer.netty.room.NettyRoom;
import view.JackarooManager;
import view.middleware.Middleware;
import view.stages.ChatBox;
import view.stages.JackarooBoard;
import view.stages.JackarooError;
import view.stages.JackarooMenu;
import view.stages.multiplayer.JackarooMultiplayer;
import view.stages.multiplayer.JackarooRoom;

public final class JackarooController {


    static int Sound = 0;
    static final Jackaroosounds Songs = new Jackaroosounds("mixkit-retro-arcade-casino-notification-211.wav", "the-sun-falls-149603.wav");
    @FXML
    public VBox playersView;

    @FXML
    public TextField ip; // Used for joining multiplayer
    @FXML
    public TextField portNumber; // Used for hosting multiplayer
    @FXML
    public TextField usernameTextField;
    public static boolean joining = false;
    public static ChatBox chatBox;

//    @FXML
//    private ImageView playerImageView;
//    @FXML
//    private Button uploadButton;

    public static NettyRoom room;
    public static MultiplayerHandler multiplayerHandler;

    @FXML
    public void onCellClick(MouseEvent event) {
        Circle circle = (Circle) event.getSource();

        Middleware.getInstance().getJackarooLinker().selectMarble(circle);
    }

    @FXML
    public void playClick(MouseEvent event) {
        try {
            clickSound();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            JackarooManager.goToScene(stage, "NamePrompt", _ -> {
            });
        } catch (Exception ignored) {
        }
    }

    @FXML
    public void clickSound() {
        try {
            URL soundURL = getClass().getResource("/mixkit-retro-arcade-casino-notification-211.wav");
            if (soundURL == null) {
                System.out.println("Sound file not found!");
                return;
            }

            AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundURL);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start();

        } catch (Exception ignored) {
        }
    }


    @FXML
    public void playHover(MouseEvent event) {
        Button B = (Button) event.getSource();

        B.setStyle("-fx-effect: dropshadow(gaussian,#ff33c4, 10, 0.5, 0, 0);");

    }

    @FXML
    public void playHoverExit(MouseEvent event) {
        Button B = (Button) event.getSource();
        B.setStyle("-fx-background-color: transparent; -fx-border-color: #FF69B4; -fx-border-width: 3px; -fx-border-radius: 5px; -fx-cursor: hand; -fx-tooltip: \"Enter your name\";;");
    }


    @FXML
    public void settingsClick(MouseEvent event) {
        clickSound();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        JackarooManager.goToScene(stage, "Settings", _ -> stage.setTitle("Jackroo Settings"));
    }

    @FXML
    public void settingsHover(MouseEvent event) {
        Button B = (Button) event.getSource();

        B.setStyle("-fx-effect: dropshadow(gaussian,#ff33c4, 10, 0.5, 0, 0);");
    }

    @FXML
    public void settingsHoverExit(MouseEvent event) {
        Button B = (Button) event.getSource();
        B.setStyle("-fx-background-color: transparent; -fx-border-color: #FF69B4; -fx-border-width: 3px; -fx-border-radius: 5px; -fx-cursor: hand; -fx-tooltip: \"Enter your name\";;");
    }

    @FXML
    public void selectCard(MouseEvent event) {
        Rectangle rectangle = (Rectangle) event.getSource();
        if (!rectangle.isVisible()) // Trying to select an already played card.
            return;

        Middleware.getInstance().getJackarooLinker().selectCard(rectangle);
    }

    @FXML
    public void backToMenu(MouseEvent event) throws Exception {
        shutdownMultiplayer();

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        if (Middleware.getInstance() != null)
            Middleware.getInstance().shutdown();

        new JackarooMenu().start(stage);
    }

    @FXML
    public void onExit(MouseEvent event) throws Exception {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Exit Game");
        alert.setHeaderText("Are you sure you want to exit?");

        if (alert.showAndWait().get() == ButtonType.OK) {
            Middleware.getInstance().getJackarooLinker().stopGame();
            backToMenu(event);
        }
    }


//    @FXML
//    public void uploadPicture(MouseEvent event) {
//        FileChooser fileChooser = new FileChooser();
//        fileChooser.getExtensionFilters().addAll(
//                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"));
//
//
//        File selectedFile = fileChooser.showOpenDialog(uploadButton.getScene().getWindow());
//
//        if (selectedFile != null) {
//            try {
//                // Load the image
//                Image image = new Image(selectedFile.toURI().toString());
//                playerImageView.setImage(image);
//            } catch (Exception e) {
//                System.err.println("Error loading image: " + e.getMessage());
//            }
//        }
//    }

    @FXML
    public void startGame(MouseEvent event) {
        try {
            clickSound();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            new JackarooBoard().start(stage, usernameTextField.getText().trim());
        } catch (Exception e) {
        }
    }

    @FXML
    public void cardHover(MouseEvent event) {
        Rectangle r = (Rectangle) event.getSource();
        r.setStyle("-fx-stroke: #69cbf2; -fx-stroke-width: 1; -fx-effect: dropshadow(gaussian, #69cbf2, 10, 0.8, 0, 0);");

        try {
            final String s = Middleware.getInstance().getJackarooConnector().getCards().get(r).getDescription();
            Middleware.getInstance().getJackarooConnector().setDescription(s);
        } catch (final Exception ignored) {
        }
    }

    @FXML
    public void cardHoverExit(MouseEvent event) {
        Rectangle r = (Rectangle) event.getSource();
        final Card selected = Middleware.getInstance().getSelectedCard();
        final Card rectangle = Middleware.getInstance().getJackarooConnector().getCards().get(r);

        if (selected != rectangle)
            r.setStyle("-fx-stroke: transparent; -fx-stroke-width: 0");// or your original color

        Middleware.getInstance().getJackarooConnector().hideDescriptionPane();
    }

    @FXML
    public void gamePlay() {
        Middleware.getInstance().getJackarooLinker().play(false);
    }

    private void updateIfMultiplayer() {

    }

    @FXML
    public void onKeyPressed(KeyEvent event) {
        Object source = event.getSource();
        if (!(source instanceof StackPane stackPane) || !stackPane.getId().equals("bigBoard"))
            return;

        try {
            switch (event.getCode()) {
                case DIGIT0 -> {
                    Middleware.getInstance().getGame().fieldMarble(0);
                    Middleware.getInstance().getJackarooUpdater().updateBoard();
                }
                case DIGIT1 -> {
                    Middleware.getInstance().getGame().fieldMarble(1);
                    Middleware.getInstance().getJackarooUpdater().updateBoard();
                }
                case DIGIT2 -> {
                    Middleware.getInstance().getGame().fieldMarble(2);
                    Middleware.getInstance().getJackarooUpdater().updateBoard();
                }
                case DIGIT3 -> {
                    Middleware.getInstance().getGame().fieldMarble(3);
                    Middleware.getInstance().getJackarooUpdater().updateBoard();
                }
                case T -> {
                    // Toggle chat box visibility
                    chatBox.toggleChatBox();
                    event.consume(); // Prevent event from bubbling up
                }
                case ESCAPE -> {
                    // Hide chat box if it's open
                    chatBox.hideChatBox();
                    event.consume();
                }
            }
        } catch (Exception e) {
            new JackarooError(e.getMessage());
        }
    }

    public void controlSound(MouseEvent event) {
        Sound++;
        Sound = Sound % 2;
        if (Sound == 1) {
            List<Clip> c = Jackaroosounds.clip;
            for (Clip cl : c) {
                if (cl.isRunning()) {
                    cl.stop();
                }

            }

        }
    }

    @FXML
    public void rotate(MouseEvent event) {
        RotateTransition r;
        TranslateTransition tt;
        FadeTransition f;
        Timeline sweep;
        List<Animation> anim = new ArrayList<>();


        for (Node node : ((Button)event.getSource()).getParent().getChildrenUnmodifiable()) {

            //RotateTransition r = new RotateTransition(Duration.seconds(20), node);
            // TranslateTransition tt = new TranslateTransition(Duration.seconds(20), node);
            // FadeTransition t = new FadeTransition(Duration.seconds(20), node);
            if (node instanceof Button ) {
                Button b=(Button)node;
                r = new RotateTransition(Duration.seconds(20), b);
                r.setByAngle(360);
                r.setCycleCount(1);
                anim.add(r);
            }
            if(node instanceof Arc n){
                tt = new TranslateTransition(Duration.seconds(20), n);
                sweep = new Timeline(
                        new KeyFrame(Duration.ZERO,new KeyValue(n.lengthProperty(), 300)),
                        new KeyFrame(Duration.seconds(3),new KeyValue(n.lengthProperty(), 360))
                );
                tt.setByX(500);
                tt.setCycleCount(1);
                tt.setAutoReverse(false);
                sweep.setCycleCount(Animation.INDEFINITE);
                anim.add(sweep);
                anim.add(tt);
            }
            if(node instanceof Text){
                f = new FadeTransition(Duration.seconds(20), node);
                f.setDelay(Duration.millis(1000));
                // f.wait(3000);
                f.setFromValue(1.0);
                f.setToValue(0.0);
                f.setAutoReverse(true);
                f.setCycleCount(2);
                f.setAutoReverse(true);
                anim.add(f);
            }
            anim.forEach(Animation::play);
        }

        //r.play();
        //tt.play();
        //f.play();
        //sweep.play();
    }

    @FXML
    public void onMultiplayerClick(final MouseEvent event) {
        try {
            clickSound();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            new JackarooMultiplayer().start(stage);
        } catch (final Exception ignored) {
        }
    }

    @FXML
    public void joinClick(MouseEvent event) {
        clickSound();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        new JackarooMultiplayer().startJoin(stage);
    }

    @FXML
    public void hostClick(MouseEvent event) {
        clickSound();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        new JackarooMultiplayer().startHost(stage);
    }

    @FXML
    public void joinGame(MouseEvent event) {
        if (joining)
            return;

        joining = true;

        clickSound();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        multiplayerHandler = new MultiplayerHandler(stage, usernameTextField.getText(), ip.getText());
    }

    @FXML
    public void startHost(final MouseEvent event) {
        clickSound();

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        if (portNumber.getText().isEmpty()) {
            new JackarooError("Invalid port!");
            return;
        }

        try {
            room = new NettyRoom(stage, usernameTextField.getText(), Integer.parseInt(portNumber.getText()));

            new JackarooRoom().start(stage, room);
        } catch (final NumberFormatException ignored) {
            new JackarooError("Invalid port!");
        }
    }

    public static void shutdownMultiplayer() {
        if (room != null) {
            room.shutdown();
            room = null;
        }

        if (multiplayerHandler != null) {
            multiplayerHandler.shutdown();
            multiplayerHandler = null;
        }
    }

    public VBox getPlayersView() {
        return playersView;
    }

    @FXML
    public void startMultiplayer(MouseEvent event) {
        clickSound();

        try {
            room.start();
        } catch (final IOException e) {
            new JackarooError(e.getMessage());
        }
    }

    public static void disableChatBox() {
        if (chatBox != null) {
            chatBox.setDisable(true);
            chatBox.hideChatBox();
        }
    }

    public static void prepareChatBox(final Pane pane) {
        chatBox = ChatBox.getInstance();

        chatBox.setLayoutX(220.0);
        chatBox.setLayoutY(452.0);

        // Make sure the chatbox can receive focus for keyboard events
        chatBox.setFocusTraversable(true);

        // Add chatbox to your root pane (replace 'rootPane' with your actual root container)
        // This should be a Pane, AnchorPane, or similar container
        pane.getChildren().add(chatBox);
    }
}