package view.middleware.frontend;

import engine.Game;
import engine.board.Cell;
import engine.board.SafeZone;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import model.Colour;
import model.card.Card;
import model.card.CardType;
import model.player.CPU;
import model.player.Marble;
import model.player.Player;
import multiplayer.game.ActionResult;
import multiplayer.packet.Packet;
import multiplayer.packet.PacketType;
import view.middleware.Middleware;
import view.middleware.connector.JackarooConnector;
import view.stages.JackarooError;

import java.util.*;

public final class JackarooUpdater {

    private static final Random RANDOM = new Random();

    private final int id;
    private final Game game;
    private final Queue<Rectangle> cardQueue;
    private final JackarooConnector connector;

    public JackarooUpdater(final int id, final ActionResult result, final JackarooConnector connector) {
        this(null, result, connector, id);
    }

    public JackarooUpdater(final Game game, final JackarooConnector connector) {
        this(game, null, connector, 0);
    }

    public JackarooUpdater(final Game game, final ActionResult result, final JackarooConnector connector, final int id) {
        this.id = id;
        this.game = game;
        this.connector = connector;
        this.cardQueue = new LinkedList<>();

        connector.getBoardPane().setOpacity(1);
        connector.getWinPane().setVisible(false);

        if (game != null)
            updateBoard();

        if (result != null)
            updateBoard(result);
    }

    public void updateBoard(final ActionResult result) {
        updateCells(result);
        updateCards(result);

        checkWin(result);
    }


    public void updateBoard() {
        updateCells();
        updateCards();

        checkWin();

        checkAndUpdateForMultiplayer();
    }

    public void updateFirePit(final Card selected) {
        if (selected != null) {//there's cards in firepit
            final Rectangle rectangle = new Rectangle(138, 179);
            rectangle.setFill(selected.getImagePattern());

            Platform.runLater(() -> cardFirePitAnimation(rectangle));
        } else {  //firepit is empty
            Platform.runLater(this::clearFirePit);
        }
    }

    private void checkAndUpdateForMultiplayer() {
        if (JackarooController.room != null) {
            try {
                ActionResult actionResult = Middleware.getInstance().getJackarooLinker().prepareActionResult();

                Packet packet = new Packet(actionResult, PacketType.UPDATE_GAME);

                // Debug logging after packet creation
                JackarooController.room.broadcast(packet);
            } catch (final Exception e) {
                new JackarooError(e.getMessage());
                throw new RuntimeException(e);
            }
        }
    }

    private void checkWin() {
        checkWin(null);
    }

    private void checkWin(final ActionResult result) {
        final Colour colour = result == null ? game.checkWin() : result.getWin();

        if (colour != null) {
            connector.getBoardPane().setOpacity(0.5);

            connector.getWinText().setText(colour + " WON THE GAME");
            connector.getWinPane().setVisible(true);
        }
    }

    private void updateCells(final ActionResult result) {
        updateCells(result.getCells(), result.getSafeZones(), result.getPlayers());
    }

    private void updateCells() {
        updateCells(game.getBoard().getTrack(), game.getBoard().getSafeZones(), game.getPlayers());
    }

    private void updateCells(List<Cell> cells, final List<SafeZone> safeZones,
                             final List<Player> players) {
        for (int i = 0; i < cells.size(); i++) {
            final Cell cell = cells.get(i);

            final Circle circle = connector.getCircles().get("cell" + Middleware.getInstance().getJackarooConnector().normalizeCell(i));
            updateCellView(cell.getMarble(), circle);
        }

        for (int i = 0; i < 4; i++) {
            cells = safeZones.get(i).getCells();

            for (int j = 0; j < cells.size(); j++) {
                final Cell cell = cells.get(j);

                final Circle circle = connector.getCircles().get("safe_" + Middleware.getInstance().getJackarooConnector().normalizeHomeAndSafeZone(i) + "_" + j);
                updateCellView(cell.getMarble(), circle);
            }
        }

        for (int i = 0; i < 4; i++) {
            final Map<Integer, Circle> homeZone = connector.getHomeZones().get(i);
            final List<Marble> marbles = players.get(i).getMarbles();

            for (int j = 0; j < 4; j++) {
                final Circle circle = homeZone.get(j);

                if (j > marbles.size() - 1) {
                    circle.getStyleClass().removeAll("home-zone");
                    circle.getStyleClass().add("empty-home-zone");
                    continue;
                }

                final Marble marble = marbles.get(j);
                circle.getStyleClass().removeAll("empty-home-zone");
                circle.getStyleClass().add("home-zone");

                final Color paint = marble.getColour().getPaint();
                circle.setFill(paint);
            }
        }
    }

    private void updateCellView(final Marble marble, final Circle circle) {
        if (marble == null) {
            circle.setStyle("");
            circle.getStyleClass().removeAll("circle");
            circle.getStyleClass().add("emptyCircle");
            return;
        }

        circle.getStyleClass().removeAll("emptyCircle");
        circle.getStyleClass().add("circle");

        circle.setFill(marble.getColour().getPaint());
    }

    private void updateCards(final ActionResult result) {
        for (int i = 0; i < 4; i++) {
            final HBox playerCards = connector.getPlayersCards().get(i);

            updateCards(i, playerCards, result.getPlayers().get(i).getHand());
        }
    }

    private void updateCards() {
        for (int i = 0; i < 4; i++) {
            final Player player = game.getPlayers().get(i);
            final HBox playerCards = connector.getPlayersCards().get(i);

            updateCards(player, playerCards);
        }
    }

    private void updateCards(final Player player, final HBox playerCards) {
        final List<Card> hand = player.getHand();
        final List<Node> modifiedRectangles = new ArrayList<>();
        final List<Node> nodes = new ArrayList<>(playerCards.getChildren());

        // Filter out null cards first to prevent index mismatches
        final List<Card> validCards = new ArrayList<>();
        for (Card card : hand) {
            if (card != null) {
                validCards.add(card);
            }
        }

        for (int i = 0; i < validCards.size() && i < nodes.size(); i++) {
            final Card card = validCards.get(i);
            final Node node = nodes.get(i);

            // Should always be true, but for extra confirmation with parsing.
            if (node instanceof Rectangle rectangle) {
                if (!(player instanceof CPU))
                    rectangle.setFill(card.getImagePattern());
                else
                    rectangle.setFill(new ImagePattern(CardType.BACK.getImage()));
                modifiedRectangles.add(rectangle);
                connector.getCards().put(rectangle, card);
            }
        }

        for (final Node node : nodes) {
            node.setVisible(modifiedRectangles.contains(node));
        }
    }

    private void updateCards(final int playerId, final HBox playerCards, final List<Card> hand) {
        final List<Node> modifiedRectangles = new ArrayList<>();
        final List<Node> nodes = new ArrayList<>(playerCards.getChildren());

        // Filter out null cards first to prevent index mismatches
        final List<Card> validCards = new ArrayList<>();
        for (Card card : hand) {
            if (card != null) {
                validCards.add(card);
            }
        }

        for (int i = 0; i < validCards.size() && i < nodes.size(); i++) {
            final Card card = validCards.get(i);
            final Node node = nodes.get(i);

            // Should always be true, but for extra confirmation with parsing.
            if (node instanceof Rectangle rectangle) {
                if (this.id == playerId)
                    rectangle.setFill(card.getImagePattern());
                else
                    rectangle.setFill(new ImagePattern(CardType.BACK.getImage()));

                modifiedRectangles.add(rectangle);
                connector.getCards().put(rectangle, card);
            }
        }

        for (final Node node : nodes)
            node.setVisible(modifiedRectangles.contains(node));
    }

    private void cardFirePitAnimation(final Rectangle rectangle) {
        final StackPane firePit = Middleware.getInstance().getJackarooConnector().getFirePit(); // StackPane

        // Random rotation
        int randomAngle = RANDOM.nextInt(360);
        rectangle.setRotate(randomAngle);

        // Add to scene graph if not already
        if (!firePit.getChildren().contains(rectangle)) {
            firePit.getChildren().add(rectangle);
        }

        // Get firePit center in parent coordinates
        Bounds firePitBounds = firePit.localToScene(firePit.getBoundsInLocal());
        Bounds cardBounds = rectangle.localToScene(rectangle.getBoundsInLocal());

        double toX = firePitBounds.getCenterX() - cardBounds.getCenterX();
        double toY = firePitBounds.getCenterY() - cardBounds.getCenterY();

        // Create TranslateTransition
        TranslateTransition translate = new TranslateTransition(Duration.seconds(0.7), rectangle);
        translate.setByX(toX);
        translate.setByY(toY);
        translate.setInterpolator(Interpolator.EASE_OUT);

        // Create RotateTransition
        RotateTransition rotate = new RotateTransition(Duration.seconds(0.7), rectangle);
        rotate.setByAngle(randomAngle);
        rotate.setInterpolator(Interpolator.EASE_OUT);

        // Play both in parallel
        ParallelTransition transition = new ParallelTransition(translate, rotate);
        transition.play();

        cardQueue.add(rectangle);
        if (cardQueue.size() > 2) {
            final Rectangle oldestCard = cardQueue.poll();

            final FadeTransition fadeOut = new FadeTransition(Duration.seconds(1), oldestCard);

            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(_ -> firePit.getChildren().remove(oldestCard));
            fadeOut.play();
        }
    }

    private void clearFirePit() {
        StackPane firePit = Middleware.getInstance().getJackarooConnector().getFirePit();

        List<Node> children = new ArrayList<>(firePit.getChildren());

        for (Node node : children) {
            if (!(node instanceof Rectangle card)) continue;

            double spin = (Math.random() - 0.5) * 720;

            // Create TranslateTransition (fly up)
            TranslateTransition translate = new TranslateTransition(Duration.seconds(1.5), card);
            translate.setByY(-150); // Move up
            translate.setInterpolator(Interpolator.EASE_OUT);

            // Create RotateTransition
            RotateTransition rotate = new RotateTransition(Duration.seconds(1.5), card);
            rotate.setByAngle(spin);
            rotate.setInterpolator(Interpolator.EASE_BOTH);

            // Create FadeTransition (fade to invisible)
            FadeTransition fade = new FadeTransition(Duration.seconds(1.5), card);
            fade.setToValue(0);
            fade.setInterpolator(Interpolator.EASE_IN);

            // Create ScaleTransition (shrink)
            ScaleTransition scale = new ScaleTransition(Duration.seconds(1.5), card);
            scale.setToX(0.1);
            scale.setToY(0.1);
            scale.setInterpolator(Interpolator.EASE_IN);

            // Combine all
            ParallelTransition animation = new ParallelTransition(translate, rotate, fade, scale);
            animation.setOnFinished(_ -> firePit.getChildren().remove(card));
            animation.play();
        }
    }
}