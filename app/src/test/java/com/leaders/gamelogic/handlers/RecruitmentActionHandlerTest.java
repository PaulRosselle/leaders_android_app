package com.leaders.gamelogic.handlers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.actions.RecruitmentAction;
import com.leaders.gamelogic.actions.RecruitmentActionMotion;
import com.leaders.gamelogic.entities.Board;
import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.entities.Game;
import com.leaders.gamelogic.entities.Position;
import com.leaders.gamelogic.enums.CharacterCard;
import com.leaders.gamelogic.enums.CharacterType;
import com.leaders.gamelogic.enums.RecruitmentMotionType;
import com.leaders.gamelogic.enums.TeamColor;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;

public class RecruitmentActionHandlerTest {

    // Distinct positions so that several characters can be placed on the board in one test.
    private static final Position POS_1 = new Position(3, 3);
    private static final Position POS_2 = new Position(3, 4);
    private static final Position POS_3 = new Position(4, 3);
    private static final Position POS_4 = new Position(4, 4);
    private static final Position POS_5 = new Position(2, 3);

    private Game createTestGame() {
        // Build the minimal Game state required by the tests.
        // This state is intentionally invalid as a real game state.
        return new Game(new Board(),
                new ArrayList<>(), // recruitableCards
                new EnumMap<>(TeamColor.class), // playerRecruitedCharacters
                new EnumMap<>(TeamColor.class), // playerBanishedCards
                new EnumMap<>(TeamColor.class) // playerWarnings
        );
    }

    @NonNull
    private RecruitmentActionMotion createAddMotion(@NonNull Character character, @NonNull Position position) {
        return new RecruitmentActionMotion(RecruitmentMotionType.Add, character, position);
    }

    @NonNull
    private RecruitmentAction createRecruitmentAction(@NonNull Character character, @NonNull Position position) {
        return new RecruitmentAction(List.of(createAddMotion(character, position)));
    }

    /** Builds and executes a single-motion recruitment action, and returns its handler (for undo). */
    @NonNull
    private RecruitmentActionHandler recruit(@NonNull Game game, @NonNull TeamColor team,
                                             @NonNull CharacterType type, @NonNull Position position) {
        Character character = Character.create(type, team);
        RecruitmentActionHandler handler = new RecruitmentActionHandler(game, createRecruitmentAction(character, position));
        handler.doAction();
        return handler;
    }

    private int countCards(@NonNull Game game, @NonNull TeamColor team, @NonNull CharacterCard card) {
        return Collections.frequency(game.getRecruitedCards(team), card);
    }

    @Test
    public void doAction_shouldPlaceCharacterOnBoard() {
        Game game = createTestGame();
        Character character = Character.create(CharacterType.Archer, TeamColor.Black);
        RecruitmentAction action = createRecruitmentAction(character, POS_1);

        new RecruitmentActionHandler(game, action).doAction();

        assertSame(character, game.getBoard().getCell(POS_1).getCharacter());
    }

    @Test
    public void doAction_shouldRemoveCharacterCardFromRecruitableCards() {
        Game game = createTestGame();
        game.getRecruitableCards().add(CharacterCard.Archer);

        recruit(game, TeamColor.Black, CharacterType.Archer, POS_1);

        assertEquals(0, game.getRecruitableCards().size());
    }

    @Test
    public void doAction_shouldAddCardToRecruitedCards() {
        Game game = createTestGame();

        recruit(game, TeamColor.Black, CharacterType.Archer, POS_1);

        assertEquals(List.of(CharacterCard.Archer), game.getRecruitedCards(TeamColor.Black));
    }

    @Test
    public void doAction_shouldNotFailWhenCharacterCardIsNotInRecruitableCards() {
        Game game = createTestGame();
        Character character = Character.create(CharacterType.Cub, TeamColor.Black);
        RecruitmentAction action = createRecruitmentAction(character, POS_1);

        new RecruitmentActionHandler(game, action).doAction();

        assertSame(character, game.getBoard().getCell(POS_1).getCharacter());
        assertEquals(List.of(CharacterCard.HermitAndCub), game.getRecruitedCards(TeamColor.Black));
    }

    @Test
    public void doAction_shouldHandleSeveralMotionsInOneAction() {
        Game game = createTestGame();
        game.getRecruitableCards().add(CharacterCard.Archer);
        game.getRecruitableCards().add(CharacterCard.Assassin);
        Character archer = Character.create(CharacterType.Archer, TeamColor.Black);
        Character assassin = Character.create(CharacterType.Assassin, TeamColor.Black);
        RecruitmentAction action = new RecruitmentAction(List.of(
                createAddMotion(archer, POS_1),
                createAddMotion(assassin, POS_2)
        ));

        new RecruitmentActionHandler(game, action).doAction();

        assertSame(archer, game.getBoard().getCell(POS_1).getCharacter());
        assertSame(assassin, game.getBoard().getCell(POS_2).getCharacter());
        assertTrue(game.getRecruitableCards().isEmpty());
        assertEquals(1, countCards(game, TeamColor.Black, CharacterCard.Archer));
        assertEquals(1, countCards(game, TeamColor.Black, CharacterCard.Assassin));
        assertEquals(2, game.getRecruitedCards(TeamColor.Black).size());
    }

    @Test
    public void undoAction_shouldRemoveCharacterFromBoard() {
        Game game = createTestGame();
        RecruitmentActionHandler handler = recruit(game, TeamColor.Black, CharacterType.Archer, POS_1);

        handler.undoAction();

        assertNull(game.getBoard().getCell(POS_1).getCharacter());
    }

    @Test
    public void undoAction_shouldRemoveCardFromRecruitedCards() {
        Game game = createTestGame();
        RecruitmentActionHandler handler = recruit(game, TeamColor.Black, CharacterType.Archer, POS_1);

        handler.undoAction();

        assertTrue(game.getRecruitedCards(TeamColor.Black).isEmpty());
    }

    @Test
    public void undoAction_shouldRestoreCharacterCardToRecruitableCards() {
        Game game = createTestGame();
        game.getRecruitableCards().add(CharacterCard.Archer);
        RecruitmentActionHandler handler = recruit(game, TeamColor.Black, CharacterType.Archer, POS_1);

        handler.undoAction();

        assertEquals(List.of(CharacterCard.Archer), game.getRecruitableCards());
    }

    @Test
    public void undoAction_shouldNotAddDuplicateCharacterCard() {
        Game game = createTestGame();
        game.getRecruitableCards().add(CharacterCard.Archer);
        RecruitmentActionHandler handler = recruit(game, TeamColor.Black, CharacterType.Archer, POS_1);

        game.getRecruitableCards().add(CharacterCard.Archer);
        handler.undoAction();

        assertEquals(List.of(CharacterCard.Archer), game.getRecruitableCards());
    }

    @Test
    public void undoAction_shouldRevertSeveralMotionsInOneAction() {
        Game game = createTestGame();
        game.getRecruitableCards().add(CharacterCard.Archer);
        game.getRecruitableCards().add(CharacterCard.Assassin);
        RecruitmentAction action = new RecruitmentAction(List.of(
                createAddMotion(Character.create(CharacterType.Archer, TeamColor.Black), POS_1),
                createAddMotion(Character.create(CharacterType.Assassin, TeamColor.Black), POS_2)
        ));
        RecruitmentActionHandler handler = new RecruitmentActionHandler(game, action);

        handler.doAction();
        handler.undoAction();

        assertNull(game.getBoard().getCell(POS_1).getCharacter());
        assertNull(game.getBoard().getCell(POS_2).getCharacter());
        assertTrue(game.getRecruitedCards(TeamColor.Black).isEmpty());
        assertEquals(2, game.getRecruitableCards().size());
        assertTrue(game.getRecruitableCards().contains(CharacterCard.Archer));
        assertTrue(game.getRecruitableCards().contains(CharacterCard.Assassin));
    }

    @Test
    public void doActionThenUndoAction_shouldRestoreGameState() {
        Game game = createTestGame();
        game.getRecruitableCards().add(CharacterCard.Archer);

        Character character = Character.create(CharacterType.Archer, TeamColor.Black);
        RecruitmentActionHandler handler =
                new RecruitmentActionHandler(game, createRecruitmentAction(character, POS_1));

        Board boardBefore = new Board(game.getBoard());
        List<CharacterCard> recruitableCardsBefore = new ArrayList<>(game.getRecruitableCards());
        List<CharacterCard> recruitedCardsBefore = new ArrayList<>(game.getRecruitedCards(TeamColor.Black));

        handler.doAction();
        handler.undoAction();

        for (Position boardPosition : game.getBoard().getCells().keySet()) {
            assertEquals(
                    boardBefore.getCell(boardPosition).getCharacter(),
                    game.getBoard().getCell(boardPosition).getCharacter()
            );
        }

        assertEquals(recruitableCardsBefore, game.getRecruitableCards());
        assertEquals(recruitedCardsBefore, game.getRecruitedCards(TeamColor.Black));
    }

    @Test
    public void doAction_shouldNeverAddLeaderKingToRecruitedCards() {
        Game game = createTestGame();

        recruit(game, TeamColor.Black, CharacterType.LeaderKing, POS_1);

        assertTrue(game.getRecruitedCards(TeamColor.Black).isEmpty());
    }

    @Test
    public void doAction_shouldNeverAddLeaderQueenToRecruitedCards() {
        Game game = createTestGame();

        recruit(game, TeamColor.Black, CharacterType.LeaderQueen, POS_1);

        assertTrue(game.getRecruitedCards(TeamColor.Black).isEmpty());
    }

    @Test
    public void doAction_shouldIgnoreLeadersButKeepOtherRecruitedCards() {
        Game game = createTestGame();

        recruit(game, TeamColor.Black, CharacterType.LeaderKing, POS_1);
        recruit(game, TeamColor.Black, CharacterType.Archer, POS_2);

        assertEquals(List.of(CharacterCard.Archer), game.getRecruitedCards(TeamColor.Black));
    }

    @Test
    public void doActionThenUndoAction_shouldKeepRecruitedCardsEmptyForLeader() {
        Game game = createTestGame();
        RecruitmentActionHandler handler = recruit(game, TeamColor.Black, CharacterType.LeaderKing, POS_1);

        handler.undoAction();

        assertTrue(game.getRecruitedCards(TeamColor.Black).isEmpty());
        assertNull(game.getBoard().getCell(POS_1).getCharacter());
    }

    @Test
    public void doAction_oneHermitShouldGiveOneHermitAndCub() {
        Game game = createTestGame();

        recruit(game, TeamColor.Black, CharacterType.Hermit, POS_1);

        assertEquals(1, countCards(game, TeamColor.Black, CharacterCard.HermitAndCub));
        assertEquals(1, game.getRecruitedCards(TeamColor.Black).size());
    }

    @Test
    public void doAction_oneCubShouldGiveOneHermitAndCub() {
        Game game = createTestGame();

        recruit(game, TeamColor.Black, CharacterType.Cub, POS_1);

        assertEquals(1, countCards(game, TeamColor.Black, CharacterCard.HermitAndCub));
        assertEquals(1, game.getRecruitedCards(TeamColor.Black).size());
    }

    @Test
    public void doAction_oneHermitAndOneCubShouldGiveOneHermitAndCub() {
        Game game = createTestGame();

        recruit(game, TeamColor.Black, CharacterType.Hermit, POS_1);
        recruit(game, TeamColor.Black, CharacterType.Cub, POS_2);

        assertEquals(1, countCards(game, TeamColor.Black, CharacterCard.HermitAndCub));
    }

    @Test
    public void doAction_twoHermitsAndOneCubShouldGiveTwoHermitAndCub() {
        Game game = createTestGame();

        recruit(game, TeamColor.Black, CharacterType.Hermit, POS_1);
        recruit(game, TeamColor.Black, CharacterType.Hermit, POS_2);
        recruit(game, TeamColor.Black, CharacterType.Cub, POS_3);

        assertEquals(2, countCards(game, TeamColor.Black, CharacterCard.HermitAndCub));
    }

    @Test
    public void doAction_twoHermitsAndTwoCubsShouldGiveTwoHermitAndCub() {
        Game game = createTestGame();

        recruit(game, TeamColor.Black, CharacterType.Hermit, POS_1);
        recruit(game, TeamColor.Black, CharacterType.Hermit, POS_2);
        recruit(game, TeamColor.Black, CharacterType.Cub, POS_3);
        recruit(game, TeamColor.Black, CharacterType.Cub, POS_4);

        assertEquals(2, countCards(game, TeamColor.Black, CharacterCard.HermitAndCub));
    }

    @Test
    public void doAction_threeHermitsAndTwoCubsShouldGiveThreeHermitAndCub() {
        Game game = createTestGame();

        recruit(game, TeamColor.Black, CharacterType.Hermit, POS_1);
        recruit(game, TeamColor.Black, CharacterType.Hermit, POS_2);
        recruit(game, TeamColor.Black, CharacterType.Hermit, POS_3);
        recruit(game, TeamColor.Black, CharacterType.Cub, POS_4);
        recruit(game, TeamColor.Black, CharacterType.Cub, POS_5);

        assertEquals(3, countCards(game, TeamColor.Black, CharacterCard.HermitAndCub));
    }

    @Test
    public void doAction_hermitAndCubInSameActionShouldGiveOneHermitAndCub() {
        Game game = createTestGame();
        RecruitmentAction action = new RecruitmentAction(List.of(
                createAddMotion(Character.create(CharacterType.Cub, TeamColor.Black), POS_1),
                createAddMotion(Character.create(CharacterType.Hermit, TeamColor.Black), POS_2)
        ));

        new RecruitmentActionHandler(game, action).doAction();

        assertEquals(1, countCards(game, TeamColor.Black, CharacterCard.HermitAndCub));
        assertEquals(1, game.getRecruitedCards(TeamColor.Black).size());
    }

    @Test
    public void doAction_hermitAndCubSplitAcrossSeveralActionsShouldGiveTwoHermitAndCub() {
        Game game = createTestGame();

        // Action 1: Cub + Hermit
        new RecruitmentActionHandler(game, new RecruitmentAction(List.of(
                createAddMotion(Character.create(CharacterType.Cub, TeamColor.Black), POS_1),
                createAddMotion(Character.create(CharacterType.Hermit, TeamColor.Black), POS_2)
        ))).doAction();
        // Action 2: Cub
        recruit(game, TeamColor.Black, CharacterType.Cub, POS_3);
        // Action 3: Hermit
        recruit(game, TeamColor.Black, CharacterType.Hermit, POS_4);

        assertEquals(2, countCards(game, TeamColor.Black, CharacterCard.HermitAndCub));
        assertEquals(2, game.getRecruitedCards(TeamColor.Black).size());
    }

    @Test
    public void doAction_hermitAndCubResultShouldNotDependOnRecruitmentOrder() {
        Game hermitFirst = createTestGame();
        recruit(hermitFirst, TeamColor.Black, CharacterType.Hermit, POS_1);
        recruit(hermitFirst, TeamColor.Black, CharacterType.Hermit, POS_2);
        recruit(hermitFirst, TeamColor.Black, CharacterType.Cub, POS_3);

        Game cubFirst = createTestGame();
        recruit(cubFirst, TeamColor.Black, CharacterType.Cub, POS_1);
        recruit(cubFirst, TeamColor.Black, CharacterType.Hermit, POS_2);
        recruit(cubFirst, TeamColor.Black, CharacterType.Hermit, POS_3);

        assertEquals(hermitFirst.getRecruitedCards(TeamColor.Black), cubFirst.getRecruitedCards(TeamColor.Black));
        assertEquals(2, countCards(cubFirst, TeamColor.Black, CharacterCard.HermitAndCub));
    }

    @Test
    public void doAction_shouldRemoveHermitAndCubFromRecruitableCardsOnlyOnce() {
        Game game = createTestGame();
        game.getRecruitableCards().add(CharacterCard.HermitAndCub);

        recruit(game, TeamColor.Black, CharacterType.Hermit, POS_1);
        recruit(game, TeamColor.Black, CharacterType.Cub, POS_2);

        assertTrue(game.getRecruitableCards().isEmpty());
        assertEquals(1, countCards(game, TeamColor.Black, CharacterCard.HermitAndCub));
    }

    @Test
    public void undoAction_undoingOnlyHermitShouldKeepOneHermitAndCubWhenCubRemains() {
        Game game = createTestGame();
        RecruitmentActionHandler hermitHandler = recruit(game, TeamColor.Black, CharacterType.Hermit, POS_1);
        recruit(game, TeamColor.Black, CharacterType.Cub, POS_2);

        hermitHandler.undoAction();

        assertEquals(1, countCards(game, TeamColor.Black, CharacterCard.HermitAndCub));
    }

    @Test
    public void undoAction_undoingUnpairedHermitShouldReduceHermitAndCubCount() {
        Game game = createTestGame();
        recruit(game, TeamColor.Black, CharacterType.Hermit, POS_1);
        recruit(game, TeamColor.Black, CharacterType.Cub, POS_2);
        RecruitmentActionHandler extraHermitHandler =
                recruit(game, TeamColor.Black, CharacterType.Hermit, POS_3);
        assertEquals(2, countCards(game, TeamColor.Black, CharacterCard.HermitAndCub));

        extraHermitHandler.undoAction();

        assertEquals(1, countCards(game, TeamColor.Black, CharacterCard.HermitAndCub));
    }

    @Test
    public void undoAction_undoingBothHermitAndCubShouldRemoveHermitAndCub() {
        Game game = createTestGame();
        RecruitmentActionHandler hermitHandler = recruit(game, TeamColor.Black, CharacterType.Hermit, POS_1);
        RecruitmentActionHandler cubHandler = recruit(game, TeamColor.Black, CharacterType.Cub, POS_2);

        cubHandler.undoAction();
        hermitHandler.undoAction();

        assertTrue(game.getRecruitedCards(TeamColor.Black).isEmpty());
    }

    @Test
    public void undoAction_shouldRestoreHermitAndCubToRecruitableCardsOnlyOnce() {
        Game game = createTestGame();
        game.getRecruitableCards().add(CharacterCard.HermitAndCub);
        RecruitmentActionHandler hermitHandler = recruit(game, TeamColor.Black, CharacterType.Hermit, POS_1);
        RecruitmentActionHandler cubHandler = recruit(game, TeamColor.Black, CharacterType.Cub, POS_2);

        cubHandler.undoAction();
        hermitHandler.undoAction();

        assertEquals(List.of(CharacterCard.HermitAndCub), game.getRecruitableCards());
    }

    @Test
    public void undoAction_multiActionHermitAndCubScenarioShouldBeFullyReversible() {
        Game game = createTestGame();

        RecruitmentActionHandler action1 = new RecruitmentActionHandler(game, new RecruitmentAction(List.of(
                createAddMotion(Character.create(CharacterType.Cub, TeamColor.Black), POS_1),
                createAddMotion(Character.create(CharacterType.Hermit, TeamColor.Black), POS_2)
        )));
        action1.doAction();
        RecruitmentActionHandler action2 = recruit(game, TeamColor.Black, CharacterType.Cub, POS_3);
        RecruitmentActionHandler action3 = recruit(game, TeamColor.Black, CharacterType.Hermit, POS_4);
        assertEquals(2, countCards(game, TeamColor.Black, CharacterCard.HermitAndCub));

        action3.undoAction();
        assertEquals(2, countCards(game, TeamColor.Black, CharacterCard.HermitAndCub));

        action2.undoAction();
        assertEquals(1, countCards(game, TeamColor.Black, CharacterCard.HermitAndCub));

        action1.undoAction();
        assertTrue(game.getRecruitedCards(TeamColor.Black).isEmpty());
    }

    @Test
    public void doAction_shouldTrackRecruitedCardsPerTeam() {
        Game game = createTestGame();

        recruit(game, TeamColor.Black, CharacterType.Archer, POS_1);
        recruit(game, TeamColor.White, CharacterType.Assassin, POS_2);

        assertEquals(List.of(CharacterCard.Archer), game.getRecruitedCards(TeamColor.Black));
        assertEquals(List.of(CharacterCard.Assassin), game.getRecruitedCards(TeamColor.White));
    }

    @Test
    public void doAction_shouldNotPairHermitAndCubAcrossTeams() {
        Game game = createTestGame();

        recruit(game, TeamColor.Black, CharacterType.Hermit, POS_1);
        recruit(game, TeamColor.White, CharacterType.Cub, POS_2);

        assertEquals(1, countCards(game, TeamColor.Black, CharacterCard.HermitAndCub));
        assertEquals(1, countCards(game, TeamColor.White, CharacterCard.HermitAndCub));
    }

    @Test
    public void undoAction_shouldOnlyAffectTheRecruitingTeam() {
        Game game = createTestGame();
        recruit(game, TeamColor.White, CharacterType.Archer, POS_1);
        RecruitmentActionHandler blackHandler = recruit(game, TeamColor.Black, CharacterType.Archer, POS_2);

        blackHandler.undoAction();

        assertTrue(game.getRecruitedCards(TeamColor.Black).isEmpty());
        assertEquals(List.of(CharacterCard.Archer), game.getRecruitedCards(TeamColor.White));
    }

    @Test
    public void getRecruitedCards_shouldReturnEmptyListForTeamWithoutRecruitment() {
        Game game = createTestGame();

        assertTrue(game.getRecruitedCards(TeamColor.Black).isEmpty());
    }

    @Test
    public void getRecruitedCards_shouldReturnUnmodifiableList() {
        Game game = createTestGame();
        recruit(game, TeamColor.Black, CharacterType.Archer, POS_1);

        List<CharacterCard> recruitedCards = game.getRecruitedCards(TeamColor.Black);

        assertThrows(UnsupportedOperationException.class, () -> recruitedCards.add(CharacterCard.Assassin));
    }

    @Test
    public void getRecruitedCards_shouldReflectLaterChangesWithoutStaleData() {
        Game game = createTestGame();
        RecruitmentActionHandler handler = recruit(game, TeamColor.Black, CharacterType.Archer, POS_1);
        assertEquals(1, game.getRecruitedCards(TeamColor.Black).size());

        handler.undoAction();

        assertEquals(0, game.getRecruitedCards(TeamColor.Black).size());
    }

    @Test
    public void copyConstructor_shouldProduceIndependentRecruitedCharacters() {
        Game game = createTestGame();
        recruit(game, TeamColor.Black, CharacterType.Hermit, POS_1);

        Game copy = new Game(game);
        recruit(game, TeamColor.Black, CharacterType.Archer, POS_2);
        game.addRecruitedCharacter(TeamColor.Black, CharacterType.Cub);

        // The copy must keep the state it had at copy time.
        assertEquals(List.of(CharacterCard.HermitAndCub), copy.getRecruitedCards(TeamColor.Black));
        // The original reflects the new recruitments.
        assertEquals(1, countCards(game, TeamColor.Black, CharacterCard.Archer));
    }

    @Test
    public void copyConstructor_shouldCopyRecruitedCharacters() {
        Game game = createTestGame();
        recruit(game, TeamColor.Black, CharacterType.Archer, POS_1);
        recruit(game, TeamColor.White, CharacterType.Hermit, POS_2);

        Game copy = new Game(game);

        assertEquals(game.getRecruitedCards(TeamColor.Black), copy.getRecruitedCards(TeamColor.Black));
        assertEquals(game.getRecruitedCards(TeamColor.White), copy.getRecruitedCards(TeamColor.White));
    }
}