package com.leaders.gamelogic.handlers;

import static org.junit.Assert.assertEquals;

import com.leaders.gamelogic.actions.BanishmentAction;
import com.leaders.gamelogic.entities.Board;
import com.leaders.gamelogic.entities.Game;
import com.leaders.gamelogic.enums.CharacterCard;
import com.leaders.gamelogic.enums.TeamColor;

import org.junit.Test;

import java.util.ArrayList;
import java.util.EnumMap;

public class BanishmentActionHandlerTest {

    private Game createTestGame() {
        return new Game(
                new Board(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new EnumMap<>(TeamColor.class)
        );
    }

    private BanishmentAction createBanishmentAction() {
        return new BanishmentAction(CharacterCard.HermitAndCub, TeamColor.Black);
    }

    @Test
    public void doAction_shouldRemoveCardFromRecruitableCards() {
        Game game = createTestGame();
        game.getRecruitableCards().add(CharacterCard.HermitAndCub);

        BanishmentAction action = createBanishmentAction();

        new BanishmentActionHandler(game, action).doAction();

        assertEquals(0, game.getRecruitableCards().size());
    }

    @Test
    public void doAction_shouldAddCardToBanishedCards() {
        Game game = createTestGame();
        BanishmentAction action = createBanishmentAction();

        new BanishmentActionHandler(game, action).doAction();

        assertEquals(1, game.getBanishedCards().size());
        assertEquals(CharacterCard.HermitAndCub, game.getBanishedCards().get(0));
    }

    @Test
    public void undoAction_shouldRestoreCardToRecruitableCards() {
        Game game = createTestGame();
        game.getRecruitableCards().add(CharacterCard.HermitAndCub);

        BanishmentAction action = createBanishmentAction();
        BanishmentActionHandler handler = new BanishmentActionHandler(game, action);

        handler.doAction();
        handler.undoAction();

        assertEquals(1, game.getRecruitableCards().size());
        assertEquals(CharacterCard.HermitAndCub, game.getRecruitableCards().get(0));
    }

    @Test
    public void undoAction_shouldRemoveCardFromBanishedCards() {
        Game game = createTestGame();
        game.getRecruitableCards().add(CharacterCard.HermitAndCub);

        BanishmentAction action = createBanishmentAction();
        BanishmentActionHandler handler = new BanishmentActionHandler(game, action);

        handler.doAction();
        handler.undoAction();

        assertEquals(0, game.getBanishedCards().size());
    }

    @Test
    public void doActionThenUndoAction_shouldRestoreGameState() {
        Game game = createTestGame();
        game.getRecruitableCards().add(CharacterCard.HermitAndCub);
        game.getBanishedCards().add(CharacterCard.Assassin);

        ArrayList<CharacterCard> recruitableCardsBefore =
                new ArrayList<>(game.getRecruitableCards());
        ArrayList<CharacterCard> banishedCardsBefore =
                new ArrayList<>(game.getBanishedCards());

        BanishmentAction action = createBanishmentAction();
        BanishmentActionHandler handler = new BanishmentActionHandler(game, action);

        handler.doAction();
        handler.undoAction();

        assertEquals(recruitableCardsBefore, game.getRecruitableCards());
        assertEquals(banishedCardsBefore, game.getBanishedCards());
    }
}