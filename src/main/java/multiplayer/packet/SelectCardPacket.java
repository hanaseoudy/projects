package multiplayer.packet;

import model.card.Card;

import java.io.Serializable;

public final class SelectCardPacket implements Serializable {

    private final int id;
    private final Card card;

    public SelectCardPacket(final int id, final Card card) {
        this.id = id;
        this.card = card;
    }

    public int getId() {
        return id;
    }

    public Card getCard() {
        return card;
    }
}
