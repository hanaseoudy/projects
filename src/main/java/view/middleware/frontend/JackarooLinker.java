package view.middleware.frontend;

import engine.Game;
import engine.board.Cell;
import exception.GameException;
import exception.InvalidCardException;
import exception.InvalidMarbleException;
import javafx.application.Platform;
import javafx.scene.control.TextInputDialog;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import model.card.Card;
import model.card.standard.Seven;
import model.player.Marble;
import multiplayer.game.ActionResult;
import multiplayer.netty.room.player.NettyOnlinePlayer;
import multiplayer.packet.Packet;
import multiplayer.packet.PacketType;
import view.middleware.Middleware;
import view.middleware.connector.JackarooConnector;
import view.stages.JackarooError;

import java.io.IOException;
import java.util.*;

public final class JackarooLinker {

    private Timer timer;
    private final Game game;
    private final JackarooConnector connector;

    public JackarooLinker(final Game game, final JackarooConnector connector) {
        this.game = game;
        this.timer = new Timer();
        this.connector = connector;
    }

    public JackarooLinker(final JackarooConnector connector) {
        this.game = null;
        this.timer = new Timer();
        this.connector = connector;
    }

    public void deselectMarble(final Marble marble, final Circle circle) {
        game.getActivePlayer().deselectMarble(marble);

        circle.setStyle("-fx-stroke: #090861; -fx-stroke-width: 1");
        game.getActivePlayer().getSelectedMarblesCircles().remove(circle);
    }

    public void selectMarble(final Circle circle) {
        try {
            final Marble marble = getMarble(circle.getId());
            if (JackarooController.multiplayerHandler != null) {
                JackarooController.multiplayerHandler.select(marble);
                return;
            }

            if (marble == null || game.getActivePlayerIndex() != 0)
                return;

            List<Marble> selectedMarbles = game.getActivePlayer().getSelectedMarbles();

            if (selectedMarbles.contains(marble)) {
                deselectMarble(marble, circle);
            } else {
                game.selectMarble(marble);

                circle.setStyle("-fx-stroke: #69cbf2; -fx-stroke-width: 1; -fx-effect: dropshadow(gaussian, #69cbf2, 10, 0.8, 0, 0);");

                game.getActivePlayer().getSelectedMarblesCircles().add(circle);
            }
        } catch (final InvalidMarbleException e) {
            new JackarooError(e.getMessage());
        }
    }

    public void selectCard(final Rectangle rectangle) {
        try {
            final Card card = connector.getCards().get(rectangle);
            if (JackarooController.multiplayerHandler != null) {
                JackarooController.multiplayerHandler.selectCard(card);
                return;
            }

            if (card != null) {
                final Card previousSelectedCard = game.getActivePlayer().getSelectedCard();
                if (previousSelectedCard != null && card != previousSelectedCard) {
                    parseCardAsRectangle(previousSelectedCard).setStyle("-fx-stroke: transparent; -fx-stroke-width: 0");
                }

                game.selectCard(card);
            }
        } catch (final InvalidCardException ignore) {
        }
    }

    private Rectangle parseCardAsRectangle(final Card card) {
        if (card == null) return null;

        for (final Map.Entry<Rectangle, Card> entry : connector.getCards().entrySet()) {
            if (entry.getValue().equals(card))
                return entry.getKey();
        }

        return null;
    }

    private Marble getMarble(final String cellId) {
        final Marble marble;
        final Cell cell = connector.getCell(cellId);

        if (cell == null) {
            marble = connector.getMarble(cellId);
            if (marble != null)
                return validateSelection(marble);

            new JackarooError("Cell " + cellId + " not found");
            return null;
        }

        return validateSelection(cell.getMarble());
    }

    private Marble validateSelection(final Marble marble) {
        return game == null
                ? connector.getResult().getActionableMarbles().contains(marble)
                ? marble
                : null
                : game.getBoard().getActionableMarbles().contains(marble)
                ? marble
                : null;
    }

    public void play(boolean splitDistanceSet) {
        if (JackarooController.multiplayerHandler != null) {
            JackarooController.multiplayerHandler.play();
            return;
        }

        if (!game.getActivePlayer().isCPU()) {
            stopGame();

            timer = new Timer();//player play their turn
            try {
                final Card previousSelectedCard = game.getActivePlayer().getSelectedCard();
                parseCardAsRectangle(previousSelectedCard).setStyle("-fx-stroke: transparent; -fx-stroke-width: 0");

                for (final Circle circle : game.getActivePlayer().getSelectedMarblesCircles()) {
                    circle.setStyle("-fx-stroke: #090861; -fx-stroke-width: 1");
                }

                if (previousSelectedCard instanceof Seven
                        && game.getActivePlayer().getSelectedMarbles().size() == 2) {
                    if (!splitDistanceSet && game.getActivePlayer() instanceof NettyOnlinePlayer nettyOnlinePlayer) {
                        nettyOnlinePlayer.sendPacket(new Packet(null, PacketType.SPLIT_DISTANCE));
                    } else {
                        TextInputDialog dialog = new TextInputDialog("1");
                        dialog.setTitle("Split distance");
                        dialog.setHeaderText("Choose split distance (1 to 6)");
                        dialog.setContentText("Enter split distance (1 to 6)");
                        Optional<String> result = dialog.showAndWait();
                        result.ifPresent(split -> {
                            try {
                                game.editSplitDistance(Integer.parseInt(split));
                            } catch (final Exception e) {
                                new JackarooError("Invalid split distance");
                            }
                        });
                    }
                }

                game.playPlayerTurn();
                game.endPlayerTurn();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        try {
                            game.playCpu(timer);
                        } catch (GameException e) {
                            throw new RuntimeException(e);
                        }
                    } //timer task
                }, 1000);
            } catch (GameException e) {
                if (game.getActivePlayerIndex() == 0)
                    Platform.runLater(() -> new JackarooError(e.getMessage()));
                else
                    ((NettyOnlinePlayer) game.getActivePlayer()).sendPacket(new Packet(e.getMessage(), PacketType.ERROR));
                if (e instanceof InvalidCardException) {
                    return;
                }
                game.endPlayerTurn();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        try {
                            game.playCpu(timer);
                        } catch (GameException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }, 1000);
            }
        }
    }

    public void play(final NettyOnlinePlayer onlinePlayer) {
        if (game.getActivePlayer() == onlinePlayer) {
            stopGame();

            timer = new Timer();//player play their turn
            try {
                final Card previousSelectedCard = game.getActivePlayer().getSelectedCard();
                parseCardAsRectangle(previousSelectedCard).setStyle("-fx-stroke: transparent; -fx-stroke-width: 0");

                for (final Circle circle : game.getActivePlayer().getSelectedMarblesCircles()) {
                    circle.setStyle("-fx-stroke: #090861; -fx-stroke-width: 1");
                }

                if (previousSelectedCard instanceof Seven
                        && game.getActivePlayer().getSelectedMarbles().size() == 2) {
                    TextInputDialog dialog = new TextInputDialog("1");
                    dialog.setTitle("Split distance");
                    dialog.setHeaderText("Choose split distance (1 to 6)");
                    dialog.setContentText("Enter split distance (1 to 6)");
                    Optional<String> result = dialog.showAndWait();
                    result.ifPresent(split -> {
                        try {
                            game.editSplitDistance(Integer.parseInt(split));
                        } catch (final Exception e) {
                            new JackarooError("Invalid split distance");
                        }
                    });
                }

                game.playPlayerTurn();
                game.endPlayerTurn();
                if (JackarooController.room != null) {
                    try {
                        JackarooController.room.broadcast(new Packet(prepareActionResult(), PacketType.UPDATE_GAME));
                    } catch (final IOException e) {
                        new JackarooError(e.getMessage());
                    }
                }

                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        try {
                            game.playCpu(timer);
                        } catch (GameException e) {
                            throw new RuntimeException(e);
                        }
                    } //timer task
                }, 1000);
            } catch (GameException e) {
                new JackarooError(e.getMessage());
                if (e instanceof InvalidCardException) {
                    return;
                }
                game.endPlayerTurn();
                if (JackarooController.room != null) {
                    try {
                        JackarooController.room.broadcast(new Packet(prepareActionResult(), PacketType.UPDATE_GAME));
                    } catch (final IOException e2) {
                        new JackarooError(e2.getMessage());
                    }
                }

                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        try {
                            game.playCpu(timer);
                        } catch (GameException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }, 1000);
            }
        }
    }

    public void stopGame() {
        timer.cancel();
    }

    public Game getGame() {
        return game;
    }

    public ActionResult prepareActionResult() {
        if (game == null) return null;

        return new ActionResult(
                game.getPlayers(),
                game.getBoard().getTrack(),
                game.getActivePlayer().getSelectedCard(),
                game.getBoard().getSafeZones(),
                game.getBoard().getActionableMarbles(),
                game.checkWin()
        );
    }
}
