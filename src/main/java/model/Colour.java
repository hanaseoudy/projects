package model;

import javafx.scene.paint.Color;

import java.io.Serializable;

public enum Colour implements Serializable {

    GREEN(Color.GREEN),
    RED(Color.RED),
    YELLOW(Color.YELLOW),
    BLUE(Color.BLUE);

    private final Color paint;

    Colour(Color paint) {
        this.paint = paint;
    }

    public Color getPaint() {
        return paint;
    }
}
