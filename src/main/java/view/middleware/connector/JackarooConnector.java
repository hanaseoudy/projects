package view.middleware.connector;

import engine.Game;
import engine.board.Cell;
import engine.board.SafeZone;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import model.card.Card;
import model.player.Marble;
import multiplayer.game.ActionResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class JackarooConnector {

    private final int id;
    private final Game game;
    private ActionResult result;

    private final Text winText, turnText, description;
    private final Pane winPane, boardPane, descriptionPane;

    private final StackPane firePit;

    private final Map<Circle, Cell> cells;
    private final Map<String, Circle> circles;

    private final Map<Rectangle, Card> cards;
    private final Map<Integer, HBox> playersCards;

    private final Map<Integer, Map<Integer, Circle>> homeZones;

    public JackarooConnector(final Parent root, final Game game) {
        this(root, null, game, 0);
    }

    public JackarooConnector(final Parent root, final ActionResult result, final int id) {
        this(root, result, null, id);
    }

    public JackarooConnector(final Parent root, final ActionResult result, final Game game, final int id) {
        this.id = id;
        this.game = game;
        this.result = result;

        this.cells = new HashMap<>();
        this.circles = new HashMap<>();

        this.homeZones = new HashMap<>();

        this.cards = new HashMap<>();
        this.playersCards = new HashMap<>();

        this.boardPane = (Pane) root.lookup("#board");

        this.winPane = (Pane) root.lookup("#winPane");
        this.winText = (Text) root.lookup("#winPrompt");
        this.descriptionPane = (StackPane) root.lookup("#descriptionPane");

        this.turnText = (Text) root.lookup("#turnText");

        this.firePit = (StackPane) root.lookup("#Firepit");

        this.description = (Text) root.lookup("#description");

        turnText.setText("Current: You - Next: CPU1");

        linkSceneCells(root);
        if (game == null) {
            linkCircles(result.getCells(), result.getSafeZones());
        } else {
            linkCircles(game.getBoard().getTrack(), game.getBoard().getSafeZones());
        }

        ((Text) root.lookup("#username")).setText(
                (game == null ? result.getPlayers() : game.getPlayers()).get(normalizePlayerIndex(0)).getName().toUpperCase() + " (YOU)"
        );

        ((Text) root.lookup("#player1Name")).setText(
                (game == null ? result.getPlayers() : game.getPlayers()).get(normalizePlayerIndex(1)).getName().toUpperCase()
        );

        ((Text) root.lookup("#player2Name")).setText(
                (game == null ? result.getPlayers() : game.getPlayers()).get(normalizePlayerIndex(2)).getName().toUpperCase()
        );

        ((Text) root.lookup("#player3Name")).setText(
                (game == null ? result.getPlayers() : game.getPlayers()).get(normalizePlayerIndex(3)).getName().toUpperCase()
        );
    }

    public void updateResult(final ActionResult result) {
        this.result = result;
    }

    private void linkSceneCells(final Node node) {
        if (node instanceof Circle circle) {
            circles.put(circle.getId(), circle);
            return;
        }

        if (node instanceof HBox hBox
                && hBox.getId() != null
                && hBox.getId().startsWith("cards_")) {
            playersCards.put(normalizePlayerIndex(Integer.parseInt(hBox.getId().split("_")[1])), hBox);
            return;
        }

        if (node instanceof Parent parent)
            for (Node child : parent.getChildrenUnmodifiable())
                linkSceneCells(child);
    }

    private void linkCircles(final List<Cell> track, final List<SafeZone> safeZones) {
        for (final Map.Entry<String, Circle> entry : circles.entrySet()) {
            final String circleId = entry.getKey();

            // Track Cell
            if (circleId.startsWith("cell")) {
                final int cellId = Integer.parseInt(circleId.substring(4));

                final Cell cell = track.get(normalizeCell(cellId));

                cells.put(entry.getValue(), cell);
                continue;
            }

            // SafeZone Cell
            if (circleId.startsWith("safe_")) {
                final String[] data = circleId.split("_");

                final int safeZoneNo = normalizeHomeAndSafeZone(Integer.parseInt(data[1]));
                final int cellNo = Integer.parseInt(data[2]);

                final ArrayList<Cell> safeZone = safeZones.get(safeZoneNo).getCells();

                cells.put(entry.getValue(), safeZone.get(cellNo));
                continue;
            }

            String[] data = circleId.split("_");

            int homeZoneNo = normalizeHomeAndSafeZone(Integer.parseInt(data[1]));
            int cellNo = Integer.parseInt(data[2]);

            Map<Integer, Circle> circles = homeZones.getOrDefault(homeZoneNo, new HashMap<>());

            circles.put(cellNo, entry.getValue());

            homeZones.put(homeZoneNo, circles);
        }
    }

    public Map<Circle, Cell> getCells() {
        return cells;
    }

    public Map<String, Circle> getCircles() {
        return circles;
    }

    public Map<Rectangle, Card> getCards() {
        return cards;
    }

    public Map<Integer, HBox> getPlayersCards() {
        return playersCards;
    }

    public Map<Integer, Map<Integer, Circle>> getHomeZones() {
        return homeZones;
    }

    public Cell getCell(final String cellId) {
        if (cellId.startsWith("cell")) {
            return cells.get(circles.get("cell" + normalizeCell(Integer.parseInt(cellId.split("cell")[1]))));
        }

        if (cellId.startsWith("safe_")) {

            final String[] data = cellId.split("_");
            final int safeZoneNo = Integer.parseInt(data[1]);
            final int cellNo = Integer.parseInt(data[2]);

            return game == null
                    ? result.getSafeZones().get(safeZoneNo).getCells().get(cellNo)
                    : game.getBoard().getSafeZones().get(safeZoneNo).getCells().get(cellNo);
        }

        return null;
    }

    public Marble getMarble(final String cellId) {
        if (cellId.startsWith("home_")) {
            final String[] data = cellId.split("_");
            final int homeZoneNo = normalizeHomeAndSafeZone(Integer.parseInt(data[1]));
            final int cellNo = Integer.parseInt(data[2]);

            return game == null
                    ? result.getPlayers().get(homeZoneNo).getMarbles().get(cellNo)
                    : game.getPlayers().get(homeZoneNo).getMarbles().get(cellNo);
        }

        final Cell cell = getCell(cellId);
        return cell == null ? null : cell.getMarble();
    }

    public Pane getWinPane() {
        return winPane;
    }

    public Text getWinText() {
        return winText;
    }

    public Pane getBoardPane() {
        return boardPane;
    }

    public StackPane getFirePit() {
        return firePit;
    }

    public Text getTurnText() {
        return turnText;
    }

    public void setDescription(final String text) {
        descriptionPane.setVisible(true);
        description.setText(text);
    }

    public void hideDescriptionPane() {
        descriptionPane.setVisible(false);
    }

    public Circle getCircleByMarble(final Marble marble) {
        for (final Map.Entry<Circle, Cell> entry : cells.entrySet()) {
            if (marble.equals(entry.getValue().getMarble()))
                return entry.getKey();
        }

        return null;
    }

    public ActionResult getResult() {
        return result;
    }

    public int normalizeCell(final int cellIndex) {
        return (cellIndex - id * 25 + 100) % 100;
    }

    public int normalizeHomeAndSafeZone(final int index) {
        return (index - id + 4) % 4;
    }

    private int normalizePlayerIndex(final int index) {
        return (index + id) % 4;
    }
}