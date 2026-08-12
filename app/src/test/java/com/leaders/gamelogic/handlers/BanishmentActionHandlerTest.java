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
import java.util.List;

public class BanishmentActionHandlerTest {

    private Game createTestGame() {
        // Build the minimal Game state required by the tests.
        // This state is intentionally invalid as a real game state.
        return new Game(new Board(),
                new ArrayList<>(), // recruitableCards
                new ArrayList<>(), // recruitedCharacters
                new EnumMap<>(TeamColor.class), // playerBanishedCards
                new EnumMap<>(TeamColor.class) // playerWarnings
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

        assertEquals(0, game.getBanishedCards(TeamColor.White).size());
        List<CharacterCard> blackBans = game.getBanishedCards(TeamColor.Black);
        assertEquals(1, blackBans.size());
        assertEquals(CharacterCard.HermitAndCub, blackBans.get(0));
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

        for (TeamColor teamColor : TeamColor.values()) {
            assertEquals(0, game.getBanishedCards(teamColor).size());
        }
    }

    @Test
    public void doActionThenUndoAction_shouldRestoreGameState() {
        Game game = createTestGame();
        game.getRecruitableCards().add(CharacterCard.HermitAndCub);
        game.addBanishedCard(TeamColor.Black, CharacterCard.Assassin);

        ArrayList<CharacterCard> recruitableCardsBefore = new ArrayList<>(game.getRecruitableCards());
        List<CharacterCard> banishedCardsBefore = List.of(CharacterCard.Assassin);

        BanishmentAction action = createBanishmentAction();
        BanishmentActionHandler handler = new BanishmentActionHandler(game, action);

        handler.doAction();
        handler.undoAction();

        assertEquals(recruitableCardsBefore, game.getRecruitableCards());
        assertEquals(banishedCardsBefore, game.getBanishedCards(TeamColor.Black));
        assertEquals(0, game.getBanishedCards(TeamColor.White).size());
    }
}