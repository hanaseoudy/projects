package model.card.wild;

import engine.GameManager;
import engine.board.BoardManager;
import model.card.Card;
import model.card.CardType;

public abstract class Wild extends Card {

    public Wild(String name, String description, BoardManager boardManager, GameManager gameManager) {
        super(name, description, boardManager, gameManager);
    }

    public Wild(String name, String description, BoardManager boardManager, GameManager gameManager, CardType cardType) {
        super(name, description, boardManager, gameManager, cardType);
    }
    
}
