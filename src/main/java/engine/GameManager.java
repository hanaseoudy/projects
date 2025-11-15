package engine;

import exception.CannotDiscardException;
import exception.CannotFieldException;
import exception.IllegalDestroyException;
import model.Colour;
import model.player.Marble;

import java.io.Serializable;

public interface GameManager extends Serializable {
	public void sendHome(Marble marble);
    public void fieldMarble() throws CannotFieldException, IllegalDestroyException;
    public void discardCard(Colour colour) throws CannotDiscardException;
    public void discardCard() throws CannotDiscardException;
    public Colour getActivePlayerColour();
    public Colour getNextPlayerColour();
}
