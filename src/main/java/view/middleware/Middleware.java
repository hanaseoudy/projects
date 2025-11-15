package view.middleware;

import engine.Game;
import javafx.scene.Parent;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import model.card.Card;
import model.player.Marble;
import multiplayer.game.ActionResult;
import view.middleware.connector.JackarooConnector;
import view.middleware.frontend.JackarooLinker;
import view.middleware.frontend.JackarooUpdater;

import java.util.ArrayList;
import java.util.List;

public final class Middleware {

    private volatile static Middleware instance;

    private final Game game;
    private Card selectedCard;
    private Rectangle selectedRectangle;
    private final List<Marble> selectedMarbles;
    private final JackarooLinker jackarooLinker;
    private final JackarooUpdater jackarooUpdater;
    private final JackarooConnector jackarooConnector;

    public Middleware(final Parent root, final Game game) {
        instance = this;

        this.game = game;
        this.selectedCard = null;
        this.selectedRectangle = null;
        this.selectedMarbles = new ArrayList<>();

        jackarooConnector = new JackarooConnector(root, game);

        jackarooLinker = new JackarooLinker(game, jackarooConnector);
        jackarooUpdater = new JackarooUpdater(game, jackarooConnector);
    }

    public Middleware(final Parent root, final ActionResult result, final int id) {
        instance = this;

        this.game = null;
        this.selectedRectangle = null;
        this.selectedMarbles = new ArrayList<>();
        this.selectedCard = result.getSelectedCard();

        jackarooConnector = new JackarooConnector(root, result, id);

        jackarooLinker = new JackarooLinker(jackarooConnector);

        jackarooUpdater = new JackarooUpdater(id, result, jackarooConnector);
    }

    public static Middleware getInstance() {
        return instance;
    }

    public void updateSelectedCard(final Card selectedCard, final Rectangle selectedRectangle) {
        if (this.selectedRectangle != null) {
            this.selectedRectangle.setStyle("-fx-stroke: transparent; -fx-stroke-width: 0");
        }

        this.selectedCard = selectedCard;
        this.selectedRectangle = selectedRectangle;
    }

    public void removeFromSelectedMarbles(final Marble marble, final Circle circle) {
        if (selectedMarbles.remove(marble))
            circle.setStyle("-fx-stroke: #090861; -fx-stroke-width: 1");
    }

    public void addToSelectedMarbles(final Marble marble, final Circle circle) {
        if (selectedMarbles.contains(marble)) {
            return;
        }

        selectedMarbles.add(marble);
        circle.setStyle("-fx-stroke: #69cbf2; -fx-stroke-width: 1; -fx-effect: dropshadow(gaussian, #69cbf2, 10, 0.8, 0, 0);");
    }

    public List<Marble> getSelectedMarbles() {
        return selectedMarbles;
    }

    public Game getGame() {
        return game;
    }

    public Card getSelectedCard() {
        return game == null
                ? selectedCard
                : game.getPlayers().getFirst().getSelectedCard();
    }

    public void shutdown() {
        instance = null;

        jackarooLinker.stopGame();
    }

    public JackarooLinker getJackarooLinker() {
        return jackarooLinker;
    }

    public JackarooUpdater getJackarooUpdater() {
        return jackarooUpdater;
    }

    public JackarooConnector getJackarooConnector() {
        return jackarooConnector;
    }
}
