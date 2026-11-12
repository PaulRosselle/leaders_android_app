package com.leaders.gamelogic.queries;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import com.leaders.gamelogic.actions.BanishmentAction;
import com.leaders.gamelogic.actions.CharacterAction;
import com.leaders.gamelogic.actions.RecruitmentAction;
import com.leaders.gamelogic.actions.RecruitmentActionMotion;
import com.leaders.gamelogic.entities.Board;
import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.entities.Game;
import com.leaders.gamelogic.entities.GameConfig;
import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.gamelogic.entities.PlayableCharacter;
import com.leaders.gamelogic.entities.Player;
import com.leaders.gamelogic.entities.Position;
import com.leaders.gamelogic.enums.CharacterCard;
import com.leaders.gamelogic.enums.CharacterMotionType;
import com.leaders.gamelogic.enums.CharacterType;
import com.leaders.gamelogic.enums.GameMode;
import com.leaders.gamelogic.enums.GamePhaseType;
import com.leaders.gamelogic.enums.RecruitmentMotionType;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.gamelogic.factories.GameFactory;
import com.leaders.gamelogic.historyentries.segments.ActionsPhase;
import com.leaders.gamelogic.historyentries.segments.Turn;
import com.leaders.gamelogic.interactions.InteractionContext;
import com.leaders.gamelogic.interactions.InteractionRequest;
import com.leaders.gamelogic.interactions.InteractionResult;
import com.leaders.gamelogic.interactions.InteractionResultType;
import com.leaders.gamelogic.interactions.InteractionTarget;
import com.leaders.gamelogic.interactions.InteractionType;
import com.leaders.gamelogic.interactions.TargetCategory;

import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;

public class AutoPlayQueryTest {

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private static Character nemesis(TeamColor team) {
        return Character.create(CharacterType.Nemesis, team);
    }

    private static Character acrobat(TeamColor team) {
        return Character.create(CharacterType.Acrobat, team);
    }

    /**
     * ADAPT IF NEEDED: the meaning/order of the two booleans of the PlayableCharacter constructor.
     * Here the 4th argument is assumed to be "mandatory".
     */
    private static PlayableCharacter playable(Character character, boolean mandatory) {
        return new PlayableCharacter(character, new Position(3, 3), mandatory, false);
    }

    private static InteractionTarget playableTarget(Character character, boolean mandatory) {
        return new InteractionTarget(TargetCategory.PlayableCharacter, playable(character, mandatory));
    }

    /**
     * ADAPT IF NEEDED: the InteractionRequest constructor signature.
     */
    private static InteractionRequest request(InteractionType type,
                                              List<InteractionTarget> targets,
                                              InteractionContext context) {
        return new InteractionRequest(
                type,
                context,
                targets,
                new ArrayList<>()
        );
    }

    private static InteractionRequest playableRequest(InteractionTarget... targets) {
        return request(InteractionType.PlayableCharacterExpected, Arrays.asList(targets),
                new InteractionContext());
    }

    private static InteractionResult playableChosenResult(Character character, boolean mandatory) {
        return new InteractionResult(
                InteractionResultType.PlayableCharacterChosen,
                new InteractionContext(),
                playableTarget(character, mandatory)
        );
    }

    private static InteractionResult positionChosenResult(Character sourceCharacter) {
        return new InteractionResult(
                InteractionResultType.PositionChosen,
                new InteractionContext(sourceCharacter),
                new InteractionTarget(TargetCategory.MovementDestination, new Position(3, 2))
        );
    }

    private static GameHistory createHistory() {
        List<Player> players = Arrays.asList(
                new Player(TeamColor.Black, "Black Player"),
                new Player(TeamColor.White, "White Player")
        );
        RecruitmentAction initialPlacement = new RecruitmentAction(Arrays.asList(
                new RecruitmentActionMotion(RecruitmentMotionType.Add,
                        Character.create(CharacterType.LeaderKing, TeamColor.Black), new Position(3, 0)),
                new RecruitmentActionMotion(RecruitmentMotionType.Add,
                        Character.create(CharacterType.LeaderQueen, TeamColor.White), new Position(3, 6))
        ));
        GameConfig config = new GameConfig(
                players,
                players.get(0),
                GameMode.Puzzle,
                Collections.emptyList(),
                Collections.singletonList(initialPlacement)
        );
        return new GameHistory(config, new ArrayList<>());
    }

    private static Character createFrogifiedCharacter(Character before) {
        return Character.transform(before, CharacterType.Frog, before.getTeamColor());
    }

    /**
     * Builds a game whose board contains the transformed Frog, with the matching transformed state.
     */
    private static Game createGameWithTransformedFrog(Character before, Character frog, Position position) {
        Board board = new Board();
        board.getCell(position).setCharacter(frog);
        Game game = new Game(
                board,
                new ArrayList<>(),
                new EnumMap<>(TeamColor.class),
                new EnumMap<>(TeamColor.class),
                new EnumMap<>(TeamColor.class),
                new HashMap<>()
        );
        game.addTransformedState(before, frog);
        return game;
    }

    private static CharacterAction createTransformAction(Character source, Character before,
                                                         Character frog, Position position) {
        return new CharacterAction(source, List.of(CharacterTransformUtils.createTransformMotion(
                CharacterMotionType.Transform, before, frog, position)));
    }

    // ------------------------------------------------------------------
    // Utility class
    // ------------------------------------------------------------------

    @Test
    public void constructor_shouldThrowAssertionError() throws Exception {
        Constructor<AutoPlayQuery> constructor = AutoPlayQuery.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        InvocationTargetException exception =
                assertThrows(InvocationTargetException.class, constructor::newInstance);
        assertTrue(exception.getCause() instanceof AssertionError);
    }

    @Test
    public void getAutoPlayableTeam_shouldReturnWhite() {
        assertEquals(TeamColor.White, AutoPlayQuery.getAutoPlayableTeam());
    }

    // ------------------------------------------------------------------
    // canAutoSelectCharacter(GameMode, InteractionRequest)
    // ------------------------------------------------------------------

    @Test
    public void canAutoSelectCharacter_request_shouldReturnTrueForSingleMandatoryNemesis() {
        InteractionRequest request = playableRequest(playableTarget(nemesis(TeamColor.White), true));

        assertTrue(AutoPlayQuery.canAutoSelectCharacter(GameMode.Puzzle, request));
    }

    @Test
    public void canAutoSelectCharacter_request_shouldIgnoreTeam() {
        InteractionRequest request = playableRequest(playableTarget(nemesis(TeamColor.Black), true));

        assertTrue(AutoPlayQuery.canAutoSelectCharacter(GameMode.Puzzle, request));
    }

    @Test
    public void canAutoSelectCharacter_request_shouldReturnFalseOutsidePuzzleMode() {
        InteractionRequest request = playableRequest(playableTarget(nemesis(TeamColor.White), true));

        assertFalse(AutoPlayQuery.canAutoSelectCharacter(GameMode.Discovery, request));
        assertFalse(AutoPlayQuery.canAutoSelectCharacter(GameMode.Strategist, request));
    }

    @Test
    public void canAutoSelectCharacter_request_shouldReturnFalseForOtherRequestType() {
        InteractionRequest request = request(
                InteractionType.PositionExpected,
                Collections.singletonList(playableTarget(nemesis(TeamColor.White), true)),
                new InteractionContext()
        );

        assertFalse(AutoPlayQuery.canAutoSelectCharacter(GameMode.Puzzle, request));
    }

    @Test
    public void canAutoSelectCharacter_request_shouldReturnFalseWhenThereIsNoTarget() {
        InteractionRequest request = playableRequest();

        assertFalse(AutoPlayQuery.canAutoSelectCharacter(GameMode.Puzzle, request));
    }

    @Test
    public void canAutoSelectCharacter_request_shouldReturnFalseWhenSeveralTargets() {
        InteractionRequest request = playableRequest(
                playableTarget(nemesis(TeamColor.White), true),
                playableTarget(acrobat(TeamColor.Black), true)
        );

        assertFalse(AutoPlayQuery.canAutoSelectCharacter(GameMode.Puzzle, request));
    }

    @Test
    public void canAutoSelectCharacter_request_shouldReturnFalseWhenNemesisIsNotMandatory() {
        InteractionRequest request = playableRequest(playableTarget(nemesis(TeamColor.White), false));

        assertFalse(AutoPlayQuery.canAutoSelectCharacter(GameMode.Puzzle, request));
    }

    @Test
    public void canAutoSelectCharacter_request_shouldReturnFalseWhenCharacterIsNotNemesis() {
        InteractionRequest request = playableRequest(playableTarget(acrobat(TeamColor.White), true));

        assertFalse(AutoPlayQuery.canAutoSelectCharacter(GameMode.Puzzle, request));
    }

    @Test
    public void canAutoSelectCharacter_request_shouldThrowWhenTargetIsNotAPlayableCharacter() {
        InteractionRequest request = playableRequest(
                new InteractionTarget(TargetCategory.MovementDestination, new Position(1, 1))
        );

        assertThrows(IllegalArgumentException.class,
                () -> AutoPlayQuery.canAutoSelectCharacter(GameMode.Puzzle, request));
    }

    // ------------------------------------------------------------------
    // canAutoSelectCharacter(GameMode, InteractionResult)
    // ------------------------------------------------------------------

    @Test
    public void canAutoSelectCharacter_result_shouldReturnTrueForMandatoryNemesis() {
        InteractionResult result = playableChosenResult(nemesis(TeamColor.White), true);

        assertTrue(AutoPlayQuery.canAutoSelectCharacter(GameMode.Puzzle, result));
    }

    @Test
    public void canAutoSelectCharacter_result_shouldIgnoreTeam() {
        InteractionResult result = playableChosenResult(nemesis(TeamColor.Black), true);

        assertTrue(AutoPlayQuery.canAutoSelectCharacter(GameMode.Puzzle, result));
    }

    @Test
    public void canAutoSelectCharacter_result_shouldReturnFalseOutsidePuzzleMode() {
        InteractionResult result = playableChosenResult(nemesis(TeamColor.White), true);

        assertFalse(AutoPlayQuery.canAutoSelectCharacter(GameMode.Discovery, result));
    }

    @Test
    public void canAutoSelectCharacter_result_shouldReturnFalseForOtherResultType() {
        InteractionResult result = new InteractionResult(
                InteractionResultType.EndPhase, new InteractionContext(), null);

        assertFalse(AutoPlayQuery.canAutoSelectCharacter(GameMode.Puzzle, result));
    }

    @Test
    public void canAutoSelectCharacter_result_shouldReturnFalseWhenNemesisIsNotMandatory() {
        InteractionResult result = playableChosenResult(nemesis(TeamColor.White), false);

        assertFalse(AutoPlayQuery.canAutoSelectCharacter(GameMode.Puzzle, result));
    }

    @Test
    public void canAutoSelectCharacter_result_shouldReturnFalseWhenCharacterIsNotNemesis() {
        InteractionResult result = playableChosenResult(acrobat(TeamColor.White), true);

        assertFalse(AutoPlayQuery.canAutoSelectCharacter(GameMode.Puzzle, result));
    }

    @Test
    public void canAutoSelectCharacter_result_shouldThrowWhenChosenTargetIsNull() {
        InteractionResult result = new InteractionResult(
                InteractionResultType.PlayableCharacterChosen, new InteractionContext(), null);

        assertThrows(IllegalArgumentException.class,
                () -> AutoPlayQuery.canAutoSelectCharacter(GameMode.Puzzle, result));
    }

    // ------------------------------------------------------------------
    // canAutoPlayCharacter(GameMode, InteractionRequest)
    // ------------------------------------------------------------------

    @Test
    public void canAutoPlayCharacter_request_shouldReturnTrueForWhiteNemesisPosition() {
        InteractionRequest request = request(
                InteractionType.PositionExpected,
                Collections.emptyList(),
                new InteractionContext(nemesis(TeamColor.White))
        );

        assertTrue(AutoPlayQuery.canAutoPlayCharacter(GameMode.Puzzle, request));
    }

    @Test
    public void canAutoPlayCharacter_request_shouldReturnFalseForBlackNemesis() {
        InteractionRequest request = request(
                InteractionType.PositionExpected,
                Collections.emptyList(),
                new InteractionContext(nemesis(TeamColor.Black))
        );

        assertFalse(AutoPlayQuery.canAutoPlayCharacter(GameMode.Puzzle, request));
    }

    @Test
    public void canAutoPlayCharacter_request_shouldReturnFalseOutsidePuzzleMode() {
        InteractionRequest request = request(
                InteractionType.PositionExpected,
                Collections.emptyList(),
                new InteractionContext(nemesis(TeamColor.White))
        );

        assertFalse(AutoPlayQuery.canAutoPlayCharacter(GameMode.Discovery, request));
    }

    @Test
    public void canAutoPlayCharacter_request_shouldReturnFalseForOtherRequestType() {
        InteractionRequest request = request(
                InteractionType.PlayableCharacterExpected,
                Collections.emptyList(),
                new InteractionContext(nemesis(TeamColor.White))
        );

        assertFalse(AutoPlayQuery.canAutoPlayCharacter(GameMode.Puzzle, request));
    }

    @Test
    public void canAutoPlayCharacter_request_shouldReturnFalseWhenNoSourceCharacter() {
        InteractionRequest request = request(
                InteractionType.PositionExpected,
                Collections.emptyList(),
                new InteractionContext()
        );

        assertFalse(AutoPlayQuery.canAutoPlayCharacter(GameMode.Puzzle, request));
    }

    @Test
    public void canAutoPlayCharacter_request_shouldReturnFalseForNonNemesisCharacter() {
        InteractionRequest request = request(
                InteractionType.PositionExpected,
                Collections.emptyList(),
                new InteractionContext(acrobat(TeamColor.White))
        );

        assertFalse(AutoPlayQuery.canAutoPlayCharacter(GameMode.Puzzle, request));
    }

    // ------------------------------------------------------------------
    // canAutoPlayCharacter(GameMode, InteractionResult)
    // ------------------------------------------------------------------

    @Test
    public void canAutoPlayCharacter_result_shouldReturnTrueForPlayableCharacterChosenWhiteNemesis() {
        InteractionResult result = playableChosenResult(nemesis(TeamColor.White), true);

        assertTrue(AutoPlayQuery.canAutoPlayCharacter(GameMode.Puzzle, result));
    }

    @Test
    public void canAutoPlayCharacter_result_shouldReturnFalseForPlayableCharacterChosenBlackNemesis() {
        InteractionResult result = playableChosenResult(nemesis(TeamColor.Black), true);

        assertFalse(AutoPlayQuery.canAutoPlayCharacter(GameMode.Puzzle, result));
    }

    @Test
    public void canAutoPlayCharacter_result_shouldReturnFalseForNonMandatoryNemesis() {
        InteractionResult result = playableChosenResult(nemesis(TeamColor.White), false);

        assertFalse(AutoPlayQuery.canAutoPlayCharacter(GameMode.Puzzle, result));
    }

    @Test
    public void canAutoPlayCharacter_result_shouldReturnFalseForNonNemesisPlayableCharacter() {
        InteractionResult result = playableChosenResult(acrobat(TeamColor.White), true);

        assertFalse(AutoPlayQuery.canAutoPlayCharacter(GameMode.Puzzle, result));
    }

    @Test
    public void canAutoPlayCharacter_result_shouldReturnTrueForPositionChosenWhiteNemesis() {
        assertTrue(AutoPlayQuery.canAutoPlayCharacter(
                GameMode.Puzzle, positionChosenResult(nemesis(TeamColor.White))));
    }

    @Test
    public void canAutoPlayCharacter_result_shouldReturnFalseForPositionChosenBlackNemesis() {
        assertFalse(AutoPlayQuery.canAutoPlayCharacter(
                GameMode.Puzzle, positionChosenResult(nemesis(TeamColor.Black))));
    }

    @Test
    public void canAutoPlayCharacter_result_shouldReturnFalseForPositionChosenNonNemesis() {
        assertFalse(AutoPlayQuery.canAutoPlayCharacter(
                GameMode.Puzzle, positionChosenResult(acrobat(TeamColor.White))));
    }

    @Test
    public void canAutoPlayCharacter_result_shouldReturnFalseForPositionChosenWithoutSourceCharacter() {
        InteractionResult result = new InteractionResult(
                InteractionResultType.PositionChosen,
                new InteractionContext(),
                new InteractionTarget(TargetCategory.MovementDestination, new Position(3, 2))
        );

        assertFalse(AutoPlayQuery.canAutoPlayCharacter(GameMode.Puzzle, result));
    }

    @Test
    public void canAutoPlayCharacter_result_shouldReturnFalseForOtherResultType() {
        InteractionResult result = new InteractionResult(
                InteractionResultType.EndPhase, new InteractionContext(), null);

        assertFalse(AutoPlayQuery.canAutoPlayCharacter(GameMode.Puzzle, result));
    }

    @Test
    public void canAutoPlayCharacter_result_shouldReturnFalseOutsidePuzzleMode() {
        assertFalse(AutoPlayQuery.canAutoPlayCharacter(
                GameMode.Discovery, playableChosenResult(nemesis(TeamColor.White), true)));
        assertFalse(AutoPlayQuery.canAutoPlayCharacter(
                GameMode.Discovery, positionChosenResult(nemesis(TeamColor.White))));
    }

    // ------------------------------------------------------------------
    // isAutoCancellableAction
    // ------------------------------------------------------------------

    @Test
    public void isAutoCancellableAction_shouldReturnTrueForNemesisCharacterAction() {
        CharacterAction action = new CharacterAction(nemesis(TeamColor.White), new ArrayList<>());

        assertTrue(AutoPlayQuery.isAutoCancellableAction(GameMode.Puzzle, action));
    }

    @Test
    public void isAutoCancellableAction_shouldIgnoreTeam() {
        CharacterAction action = new CharacterAction(nemesis(TeamColor.Black), new ArrayList<>());

        assertTrue(AutoPlayQuery.isAutoCancellableAction(GameMode.Puzzle, action));
    }

    @Test
    public void isAutoCancellableAction_shouldReturnFalseOutsidePuzzleMode() {
        CharacterAction action = new CharacterAction(nemesis(TeamColor.White), new ArrayList<>());

        assertFalse(AutoPlayQuery.isAutoCancellableAction(GameMode.Discovery, action));
    }

    @Test
    public void isAutoCancellableAction_shouldReturnFalseForNonNemesisCharacterAction() {
        CharacterAction action = new CharacterAction(acrobat(TeamColor.White), new ArrayList<>());

        assertFalse(AutoPlayQuery.isAutoCancellableAction(GameMode.Puzzle, action));
    }

    @Test
    public void isAutoCancellableAction_shouldReturnFalseForNonCharacterAction() {
        BanishmentAction action = new BanishmentAction(CharacterCard.Archer, TeamColor.Black);

        assertFalse(AutoPlayQuery.isAutoCancellableAction(GameMode.Puzzle, action));
    }

    // ------------------------------------------------------------------
    // getEndTurnAutoAction
    // ------------------------------------------------------------------

    @Test
    public void getEndTurnAutoAction_shouldThrowWhenNoTurnInProgress() {
        GameHistory history = createHistory();
        Game game = GameFactory.create(history);

        assertThrows(IllegalStateException.class,
                () -> AutoPlayQuery.getEndTurnAutoAction(game, history));
    }

    @Test
    public void getEndTurnAutoAction_shouldThrowWhenTurnEndPhaseHasNotStarted() {
        GameHistory history = createHistory();
        history.getEntries().add(new Turn(TeamColor.Black));
        Game game = GameFactory.create(history);

        assertThrows(IllegalStateException.class,
                () -> AutoPlayQuery.getEndTurnAutoAction(game, history));
    }

    @Test
    public void getEndTurnAutoAction_shouldThrowWhenTurnEndPhaseHasEnded() {
        GameHistory history = createHistory();
        Turn turn = new Turn(TeamColor.Black);
        history.getEntries().add(turn);
        turn.getSubPhase(GamePhaseType.TurnEnd).start();
        turn.getSubPhase(GamePhaseType.TurnEnd).end();
        Game game = GameFactory.create(history);

        assertThrows(IllegalStateException.class,
                () -> AutoPlayQuery.getEndTurnAutoAction(game, history));
    }

    @Test
    public void getEndTurnAutoAction_shouldReturnNullWhenNoFrogIsTransformed() {
        GameHistory history = createHistory();
        Turn turn = new Turn(TeamColor.Black);
        history.getEntries().add(turn);
        turn.getSubPhase(GamePhaseType.TurnEnd).start();
        Game game = GameFactory.create(history);

        assertNull(AutoPlayQuery.getEndTurnAutoAction(game, history));
    }

    // ------------------------------------------------------------------
    // getFrogAutoUntransformAction
    // ------------------------------------------------------------------

    @Test
    public void getFrogAutoUntransformAction_shouldReturnNullWhenNoFrogIsTransformed() {
        GameHistory history = createHistory();
        Turn turn = new Turn(TeamColor.Black);
        history.getEntries().add(turn);
        Game game = GameFactory.create(history);

        assertNull(AutoPlayQuery.getFrogAutoUntransformAction(game, turn));
    }

    @Test
    public void getFrogAutoUntransformAction_shouldReturnUntransformActionWhenTransformationIsOver() {
        Position position = new Position(3, 3);
        Character hermit = Character.create(CharacterType.Hermit, TeamColor.White);
        Character frog = createFrogifiedCharacter(hermit);
        Game game = createGameWithTransformedFrog(hermit, frog, position);

        // The transformation happened in a previous turn: the current turn has no matching action
        Turn turn = new Turn(TeamColor.Black);

        CharacterAction action = AutoPlayQuery.getFrogAutoUntransformAction(game, turn);

        assertNotNull(action);
        assertEquals(frog.getId(), action.getSrcCharacter().getId());
        assertEquals(1, action.getMotions().size());
        assertEquals(CharacterMotionType.Untransform, action.getMotions().get(0).getMotionType());
        assertEquals(2, action.getMotions().get(0).getTargets().size());

        // Frog (origin) -> original character (destination), at the Frog's position on the board
        assertEquals(CharacterType.Frog,
                action.getMotions().get(0).getTargets().get(0).getCharacter().getCharacterType());
        assertEquals(CharacterType.Hermit,
                action.getMotions().get(0).getTargets().get(1).getCharacter().getCharacterType());
        assertEquals(position, action.getMotions().get(0).getTargets().get(0).getOriginPos());
        assertNull(action.getMotions().get(0).getTargets().get(0).getDestPos());
        assertNull(action.getMotions().get(0).getTargets().get(1).getOriginPos());
        assertEquals(position, action.getMotions().get(0).getTargets().get(1).getDestPos());
    }

    @Test
    public void getFrogAutoUntransformAction_shouldReturnNullWhenFrogWasTransformedDuringThisTurn() {
        Position position = new Position(3, 3);
        Character shaman = Character.create(CharacterType.Shaman, TeamColor.Black);
        Character hermit = Character.create(CharacterType.Hermit, TeamColor.White);
        Character frog = createFrogifiedCharacter(hermit);
        Game game = createGameWithTransformedFrog(hermit, frog, position);

        Turn turn = new Turn(TeamColor.Black);
        ((ActionsPhase) turn.getSubPhase(GamePhaseType.Actions)).getActions()
                .add(createTransformAction(shaman, hermit, frog, position));

        assertNull(AutoPlayQuery.getFrogAutoUntransformAction(game, turn));
    }

    @Test
    public void getFrogAutoUntransformAction_shouldReturnUntransformActionWhenAnotherCharacterWasTransformedThisTurn() {
        Position frogPosition = new Position(3, 3);
        Position otherPosition = new Position(4, 3);
        Character shaman = Character.create(CharacterType.Shaman, TeamColor.Black);
        Character hermit = Character.create(CharacterType.Hermit, TeamColor.White);
        Character frog = createFrogifiedCharacter(hermit);
        Character acrobat = Character.create(CharacterType.Acrobat, TeamColor.White);
        Character otherFrog = createFrogifiedCharacter(acrobat);
        Game game = createGameWithTransformedFrog(hermit, frog, frogPosition);

        // Only an unrelated transformation happened this turn: the existing Frog is still reverted
        Turn turn = new Turn(TeamColor.Black);
        ((ActionsPhase) turn.getSubPhase(GamePhaseType.Actions)).getActions()
                .add(createTransformAction(shaman, acrobat, otherFrog, otherPosition));

        CharacterAction action = AutoPlayQuery.getFrogAutoUntransformAction(game, turn);

        assertNotNull(action);
        assertEquals(hermit.getId(), action.getSrcCharacter().getId());
    }

    @Test
    public void getFrogAutoUntransformAction_shouldReturnNullWhenOnlyNonFrogTransformationExists() {
        Character bruiser = Character.create(CharacterType.Bruiser, TeamColor.White);
        Character cub = Character.transform(bruiser, CharacterType.Cub, TeamColor.White);
        Game game = createGameWithTransformedFrog(bruiser, cub, new Position(3, 3));

        assertNull(AutoPlayQuery.getFrogAutoUntransformAction(game, new Turn(TeamColor.Black)));
    }

    // ------------------------------------------------------------------
    // getEndTurnAutoAction (with a transformed Frog)
    // ------------------------------------------------------------------

    @Test
    public void getEndTurnAutoAction_shouldReturnUntransformActionWhenFrogTransformationIsOver() {
        Position position = new Position(3, 3);
        Character hermit = Character.create(CharacterType.Hermit, TeamColor.White);
        Character frog = createFrogifiedCharacter(hermit);
        Game game = createGameWithTransformedFrog(hermit, frog, position);

        GameHistory history = createHistory();
        Turn turn = new Turn(TeamColor.Black);
        history.getEntries().add(turn);
        turn.getSubPhase(GamePhaseType.TurnEnd).start();

        CharacterAction action = AutoPlayQuery.getEndTurnAutoAction(game, history);

        assertNotNull(action);
        assertEquals(frog.getId(), action.getSrcCharacter().getId());
        assertEquals(CharacterMotionType.Untransform, action.getMotions().get(0).getMotionType());
    }

    @Test
    public void getEndTurnAutoAction_shouldReturnNullWhenFrogWasTransformedDuringThisTurn() {
        Position position = new Position(3, 3);
        Character shaman = Character.create(CharacterType.Shaman, TeamColor.Black);
        Character hermit = Character.create(CharacterType.Hermit, TeamColor.White);
        Character frog = createFrogifiedCharacter(hermit);
        Game game = createGameWithTransformedFrog(hermit, frog, position);

        GameHistory history = createHistory();
        Turn turn = new Turn(TeamColor.Black);
        history.getEntries().add(turn);
        ((ActionsPhase) turn.getSubPhase(GamePhaseType.Actions)).getActions()
                .add(createTransformAction(shaman, hermit, frog, position));
        turn.getSubPhase(GamePhaseType.TurnEnd).start();

        assertNull(AutoPlayQuery.getEndTurnAutoAction(game, history));
    }
}