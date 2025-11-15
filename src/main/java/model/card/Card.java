package model.card;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;

import engine.GameManager;
import engine.board.BoardManager;
import exception.ActionException;
import exception.InvalidMarbleException;
import javafx.scene.paint.ImagePattern;
import model.Colour;
import model.player.Marble;

public abstract class Card implements Serializable {

    private final UUID uuid;
    private final String name;
    private final String description;
    protected GameManager gameManager;
    protected transient BoardManager boardManager;
    private final CardType cardType;

    public Card(String name, String description, BoardManager boardManager, GameManager gameManager) {
        this(name, description, boardManager, gameManager, null);
    }

    public Card(String name, String description, BoardManager boardManager, GameManager gameManager, CardType cardType) {
        this.name = name;

        this.uuid = UUID.randomUUID();
        this.description = description;
        this.boardManager = boardManager;
        this.gameManager = gameManager;
        this.cardType = cardType;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public ImagePattern getImagePattern() {
        if (cardType == null)
            return null;

        if (GraphicsEnvironment.isHeadless())
            throw new IllegalStateException("Attempted to render card image server-side!");

        return new ImagePattern(cardType.getImage());
    }


    public abstract void act(ArrayList<Marble> marbles) throws ActionException, InvalidMarbleException;
    
    public boolean validateMarbleSize(ArrayList<Marble> marbles) {
        return marbles.size() == 1;
    }
    
    public boolean validateMarbleColours(ArrayList<Marble> marbles) {
        Colour ownerColour = gameManager.getActivePlayerColour();
        boolean sameColour = true;
        for (Marble marble : marbles) {
            if (marble.getColour() != ownerColour) {
                sameColour = false;
            }
        }
        return sameColour;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Card card = (Card) o;

        return this.uuid.equals(card.uuid);
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

}
