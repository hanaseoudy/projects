package model.player;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import exception.GameException;
import exception.InvalidCardException;
import exception.InvalidMarbleException;
import javafx.scene.shape.Circle;
import model.Colour;
import model.card.Card;
import view.middleware.Middleware;

@SuppressWarnings("unused")
public class Player implements Serializable {
    private final String name;
    private final Colour colour;
    private ArrayList<Card> hand;
    private final ArrayList<Marble> marbles;
    private Card selectedCard;
    private final ArrayList<Marble> selectedMarbles;
    private final List<Circle> selectedMarblesCircles;

    public Player(String name, Colour colour) {
        this.name = name;
        this.colour = colour;
        this.hand = new ArrayList<>();
        this.selectedMarbles = new ArrayList<>();
        this.marbles = new ArrayList<>();
        this.selectedMarblesCircles = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            this.marbles.add(new Marble(colour));
        }

        //default value
        this.selectedCard = null;
    }

    public String getName() {
        return name;
    }

    public Colour getColour() {
        return colour;
    }

    public ArrayList<Card> getHand() {
        return hand;
    }

    public void setHand(ArrayList<Card> hand) {
        this.hand = hand;
    }

    public ArrayList<Marble> getMarbles() {
        return marbles;
    }

    public Card getSelectedCard() {
        return selectedCard;
    }

    public void regainMarble(Marble marble) {
        this.marbles.add(marble);
    }

    public Marble getOneMarble() {
        if (marbles.isEmpty())
            return null;

        return this.marbles.getFirst();
    }

    public void selectCard(Card card) throws InvalidCardException {
        if (!this.hand.contains(card))
            throw new InvalidCardException("Card not in hand.");

        if (card.equals(this.selectedCard)) {
            Middleware.getInstance().getJackarooLinker().play(false);
            return;
        }

        this.selectedCard = card;
    }

    public void selectMarble(Marble marble) throws InvalidMarbleException {
        if (!this.selectedMarbles.contains(marble)) {
            if (this.selectedMarbles.size() > 1)
                throw new InvalidMarbleException("Cannot select more than 2 marbles.");

            selectedMarbles.add(marble);
        }
    }

    public void deselectAll() {
        this.selectedCard = null;
        this.selectedMarbles.clear();
    }

    public void play() throws GameException {
        if (selectedCard == null)
            throw new InvalidCardException("Must select a card to play.");

        if (!this.selectedCard.validateMarbleSize(this.selectedMarbles))
            throw new InvalidMarbleException("Invalid number of marbles selected for " + selectedCard.getName() + ".");

        if (!this.selectedCard.validateMarbleColours(this.selectedMarbles))
            throw new InvalidMarbleException("Invalid marble colours selected for " + selectedCard.getName() + ".");

        this.selectedCard.act(this.selectedMarbles);
    }

    public void deselectMarble(Marble marble) {
        selectedMarbles.remove(marble);
    }

    public ArrayList<Marble> getSelectedMarbles() {
        return selectedMarbles;
    }

    public List<Circle> getSelectedMarblesCircles() {
        return selectedMarblesCircles;
    }

    public boolean isCPU() {
        return false;
    }
}

