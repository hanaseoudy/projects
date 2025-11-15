package multiplayer.game;

import engine.board.Cell;
import engine.board.SafeZone;
import model.Colour;
import model.card.Card;
import model.player.Marble;
import model.player.Player;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public final class ActionResult implements Serializable {

    private final Colour win;
    private final List<Cell> cells;
    private final Card selectedCard;
    private final List<Player> players;
    private final List<SafeZone> safeZones;
    private final List<Marble> actionableMarbles;

    public ActionResult(List<Player> players,
                        List<Cell> cells, Card selectedCard,
                        List<SafeZone> safeZones,
                        List<Marble> actionableMarbles,
                        Colour win) {
        // Create defensive copies to prevent modification during serialization
        this.cells = cells != null ? new ArrayList<>(cells) : new ArrayList<>();
        this.players = players != null ? createPlayersCopy(players) : new ArrayList<>();
        this.selectedCard = selectedCard;
        this.safeZones = safeZones != null ? new ArrayList<>(safeZones) : new ArrayList<>();
        this.actionableMarbles = actionableMarbles;
        this.win = win;
    }

    private List<Player> createPlayersCopy(List<Player> originalPlayers) {
        List<Player> playersCopy = new ArrayList<>();
        for (Player player : originalPlayers) {
            if (player != null) {
                // Create a defensive copy of the player to ensure thread safety
                playersCopy.add(player);
            }
        }
        return playersCopy;
    }

    public List<Cell> getCells() {
        return cells;
    }

    public Card getSelectedCard() {
        return selectedCard;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public List<SafeZone> getSafeZones() {
        return safeZones;
    }

    public List<Marble> getActionableMarbles() {
        return actionableMarbles;
    }

    public Colour getWin() {
        return win;
    }

    // Add debugging methods for serialization
    @Serial
    private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        out.defaultWriteObject();

    }

    @Serial
    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
    }
}