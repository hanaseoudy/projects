package model.card;

import javafx.scene.image.Image;
import model.card.standard.Suit;

public enum CardType {

    // Ace
    ACE_DIAMOND("images/cards/diamonds_A.jpeg"),
    ACE_HEART("images/cards/hearts_A.jpeg"),
    ACE_SPADE("images/cards/spades_A.jpeg"),
    ACE_CLUB("images/cards/clubs_A.jpeg"),

    // Two
    TWO_DIAMOND("images/cards/diamonds_2.jpeg"),
    TWO_HEART("images/cards/hearts_2.jpeg"),
    TWO_SPADE("images/cards/spades_2.jpeg"),
    TWO_CLUB("images/cards/clubs_2.jpeg"),

    // Three
    THREE_DIAMOND("images/cards/diamonds_3.jpeg"),
    THREE_HEART("images/cards/hearts_3.jpeg"),
    THREE_SPADE("images/cards/spades_3.jpeg"),
    THREE_CLUB("images/cards/clubs_3.jpeg"),

    // Four
    FOUR_DIAMOND("images/cards/diamonds_4.jpeg"),
    FOUR_HEART("images/cards/hearts_4.jpeg"),
    FOUR_SPADE("images/cards/spades_4.jpeg"),
    FOUR_CLUB("images/cards/clubs_4.jpeg"),

    // Five
    FIVE_DIAMOND("images/cards/diamonds_5.jpeg"),
    FIVE_HEART("images/cards/hearts_5.jpeg"),
    FIVE_SPADE("images/cards/spades_5.jpeg"),
    FIVE_CLUB("images/cards/clubs_5.jpeg"),

    // Six
    SIX_DIAMOND("images/cards/diamonds_6.jpeg"),
    SIX_HEART("images/cards/hearts_6.jpeg"),
    SIX_SPADE("images/cards/spades_6.jpeg"),
    SIX_CLUB("images/cards/clubs_6.jpeg"),

    // Seven
    SEVEN_DIAMOND("images/cards/diamonds_7.jpeg"),
    SEVEN_HEART("images/cards/hearts_7.jpeg"),
    SEVEN_SPADE("images/cards/spades_7.jpeg"),
    SEVEN_CLUB("images/cards/clubs_7.jpeg"),

    // Eight
    EIGHT_DIAMOND("images/cards/diamonds_8.jpeg"),
    EIGHT_HEART("images/cards/hearts_8.jpeg"),
    EIGHT_SPADE("images/cards/spades_8.jpeg"),
    EIGHT_CLUB("images/cards/clubs_8.jpeg"),

    // Nine
    NINE_DIAMOND("images/cards/diamonds_9.jpeg"),
    NINE_HEART("images/cards/hearts_9.png"),
    NINE_SPADE("images/cards/spades_9.jpeg"),
    NINE_CLUB("images/cards/clubs_9.jpeg"),

    // Ten
    TEN_DIAMOND("images/cards/diamonds_10.jpeg"),
    TEN_HEART("images/cards/hearts_10.jpeg"),
    TEN_SPADE("images/cards/spades_10.jpeg"),
    TEN_CLUB("images/cards/clubs_10.jpeg"),

    // King
    KING_DIAMOND("images/cards/diamonds_king.jpeg"),
    KING_HEART("images/cards/hearts_king.jpeg"),
    KING_SPADE("images/cards/spades_king.jpeg"),
    KING_CLUB("images/cards/clubs_king.jpeg"),

    // Jack
    JACK_DIAMOND("images/cards/diamonds_jack.jpeg"),
    JACK_HEART("images/cards/hearts_jack.jpeg"),
    JACK_SPADE("images/cards/spades_jack.jpeg"),
    JACK_CLUB("images/cards/clubs_jack.jpeg"),

    // Queen
    QUEEN_DIAMOND("images/cards/diamonds_queen.jpeg"),
    QUEEN_HEART("images/cards/hearts_queen.jpeg"),
    QUEEN_SPADE("images/cards/spades_queen.jpeg"),
    QUEEN_CLUB("images/cards/clubs_queen.jpeg"),

    // Burner
    BURNER("images/cards/burner.png"),

    // Saver
    SAVER("images/cards/saver.png"),

    BACK("images/cards/CardsBackground.png")
    ;

    private transient Image image;
    private final String path;

    CardType(final String path) {
        this.path = path;
    }

    public Image getImage() {
        if (image == null)
            image = new Image(path);

        return image;
    }

    public static CardType getCardType(final String name, final Suit suit) {
        try {
            return CardType.valueOf(name.toUpperCase() + "_" + suit.name());
        } catch (final IllegalArgumentException e) {
            return null;
        }
    }
}
