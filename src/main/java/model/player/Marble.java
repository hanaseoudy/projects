package model.player;

import model.Colour;
import model.card.Card;

import java.io.Serializable;
import java.util.UUID;

public class Marble implements Serializable {

    private final UUID uuid;
    private final Colour colour;

    public Marble(Colour colour) {
        this.colour = colour;
        this.uuid = UUID.randomUUID();
    }

    public Colour getColour() {
        return this.colour;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Marble marble = (Marble) o;

        return this.uuid.equals(marble.uuid);
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }
}
