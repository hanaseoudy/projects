package engine;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

import engine.board.Board;
import engine.board.SafeZone;
import exception.CannotDiscardException;
import exception.CannotFieldException;
import exception.GameException;
import exception.IllegalDestroyException;
import exception.InvalidCardException;
import exception.InvalidMarbleException;
import exception.SplitOutOfRangeException;
import model.Colour;
import model.card.Card;
import model.card.Deck;
import model.player.*;
import multiplayer.netty.room.NettyRoomPlayer;
import multiplayer.netty.room.player.NettyOnlinePlayer;
import multiplayer.packet.Packet;
import multiplayer.packet.PacketType;
import view.middleware.Middleware;
import view.middleware.frontend.JackarooController;
import view.stages.JackarooError;

public class Game implements GameManager, Serializable {

    private int turn;
    private final Board board;
    private int currentPlayerIndex;
    private final ArrayList<Card> firePit;
    private final ArrayList<Player> players;

    public Game(final String playerName) throws IOException {
        this(playerName, null);
    }

    public Game(final String playerName, final Set<NettyRoomPlayer> roomPlayers) throws IOException {
        turn = 0;
        currentPlayerIndex = 0;
        firePit = new ArrayList<>();

        final ArrayList<Colour> colourOrder = new ArrayList<>(Arrays.asList(Colour.values()));

        Collections.shuffle(colourOrder);

        this.board = new Board(colourOrder, this);

        Deck.loadCardPool(this.board, this);

        this.players = new ArrayList<>();
        this.players.add(new Player(playerName, colourOrder.getFirst()));

        if (roomPlayers == null) {
            for (int i = 1; i < 4; i++)
                this.players.add(new CPU("CPU " + i, colourOrder.get(i), this.board));
        } else {
            int i = 1;

            for (final NettyRoomPlayer roomPlayer : roomPlayers) {
                this.players.add(new NettyOnlinePlayer(roomPlayer.getName(), colourOrder.get(i), this.board, roomPlayer.getHandler()));
                i++;
            }

            // Rechecking if game is full, if not, adding empty slots with CPUs.
            for (; i < 4; i++)
                this.players.add(new CPU("CPU " + (i - roomPlayers.size() - 1), colourOrder.get(i), this.board));
        }

        for (int i = 0; i < 4; i++)
            this.players.get(i).setHand(Deck.drawCards());
    }

    public Board getBoard() {
        return board;
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }

    public ArrayList<Card> getFirePit() {
        return firePit;
    }

    public void selectCard(Card card) throws InvalidCardException {
        players.get(currentPlayerIndex).selectCard(card);
    }

    public void selectMarble(Marble marble) throws InvalidMarbleException {
        players.get(currentPlayerIndex).selectMarble(marble);
    }

    public void deselectAll() {
        players.get(currentPlayerIndex).deselectAll();
    }

    public void editSplitDistance(int splitDistance) throws SplitOutOfRangeException {
        if (splitDistance < 1 || splitDistance > 6)
            throw new SplitOutOfRangeException();

        board.setSplitDistance(splitDistance);
    }

    public boolean canPlayTurn() {
        return players.get(currentPlayerIndex).getHand().size() == (4 - turn);
    }

    public void playPlayerTurn() throws GameException {
        players.get(currentPlayerIndex).play();
    }

    public void endPlayerTurn() {
        Card selected = players.get(currentPlayerIndex).getSelectedCard();
        players.get(currentPlayerIndex).getHand().remove(selected);
        firePit.add(selected);
        players.get(currentPlayerIndex).deselectAll();

        currentPlayerIndex = (currentPlayerIndex + 1) % 4;

        if (currentPlayerIndex == 0 && turn < 3)
            turn++;

        else if (currentPlayerIndex == 0 && turn == 3) {
            turn = 0;
            for (Player p : players) {
                if (Deck.getPoolSize() < 4) {
                    Deck.refillPool(firePit);
                    firePit.clear();
                }
                ArrayList<Card> newHand = Deck.drawCards();
                p.setHand(newHand);
            }

        }
        if (!firePit.isEmpty()) { //there's cards in firepit
            Middleware.getInstance().getJackarooUpdater().updateFirePit(selected);
            checkAndUpdateFirePitMultiplayer(selected);
        } else {  //firepit is empty
            Middleware.getInstance().getJackarooUpdater().updateFirePit(null);
        }

        Middleware.getInstance().getJackarooUpdater().updateBoard();
    }

    private void checkAndUpdateFirePitMultiplayer(final Card selected) {
        if (JackarooController.room != null) {
            try {
                JackarooController.room.broadcast(new Packet(selected, PacketType.UPDATE_FIREPIT));
            } catch (final Exception e) {
                new JackarooError(e.getMessage());
                throw new RuntimeException(e);
            }
        }
    }

    public Colour checkWin() {
        for (SafeZone safeZone : board.getSafeZones())
            if (safeZone.isFull())
                return safeZone.getColour();

        return null;
    }

    @Override
    public void sendHome(Marble marble) {
        for (Player player : players) {
            if (player.getColour() == marble.getColour()) {
                player.regainMarble(marble);
                break;
            }
        }
    }

    @Override
    public void fieldMarble() throws CannotFieldException, IllegalDestroyException {
        Marble marble = players.get(currentPlayerIndex).getOneMarble();

        if (marble == null)
            throw new CannotFieldException("No marbles left in the Home Zone to field.");

        board.sendToBase(marble);
        players.get(currentPlayerIndex).getMarbles().remove(marble);
    }

    public void fieldMarble(int index) throws CannotFieldException, IllegalDestroyException {
        Marble marble = players.get(index).getOneMarble();

        if (marble == null)
            throw new CannotFieldException("No marbles left in the Home Zone to field.");

        board.sendToBase(marble);
        players.get(index).getMarbles().remove(marble);
    }

    @Override
    public void discardCard(Colour colour) throws CannotDiscardException {
        for (Player player : players) {
            if (player.getColour() == colour) {
                int handSize = player.getHand().size();
                if (handSize == 0)
                    throw new CannotDiscardException("Player has no cards to discard.");
                int randIndex = (int) (Math.random() * handSize);
                this.firePit.add(player.getHand().remove(randIndex));
            }
        }
    }

    @Override
    public void discardCard() throws CannotDiscardException {
        int randIndex = (int) (Math.random() * 4);
        while (randIndex == currentPlayerIndex)
            randIndex = (int) (Math.random() * 4);

        discardCard(players.get(randIndex).getColour());
    }

    @Override
    public Colour getActivePlayerColour() {
        return players.get(currentPlayerIndex).getColour();
    }

    @Override
    public Colour getNextPlayerColour() {
        return players.get((currentPlayerIndex + 1) % 4).getColour();
    }


    public int getActivePlayerIndex() {
        return currentPlayerIndex;
    }

    public void playCpu(final Timer timer) throws GameException {
        final String name = players.get(currentPlayerIndex).getName();

        if (!canPlayTurn()) {  // cpu/player card discarded
            endPlayerTurn();

            playCpu(timer);
            return;
        }

        if (players.get(currentPlayerIndex).isCPU()) { //if cpu can play
            Middleware.getInstance().getJackarooConnector().getTurnText().setText("Current: " + name + " - Next: " + players.get((currentPlayerIndex + 1) % 4).getName());

            playPlayerTurn();
            endPlayerTurn();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        playCpu(timer);
                    } catch (GameException e) {
                        throw new RuntimeException(e);
                    }
                } //timer task
            }, 1000);
            return;
        }
        //if player can play now

        Middleware.getInstance().getJackarooConnector().getTurnText().setText("Current: " + (name.isEmpty() ? "You" : name) + " - Next: " + players.get((currentPlayerIndex + 1) % 4).getName());
    }

    public Player getActivePlayer() {
        return players.get(currentPlayerIndex);
    }
}
