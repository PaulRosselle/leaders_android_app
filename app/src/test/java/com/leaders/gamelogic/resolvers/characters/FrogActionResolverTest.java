package com.leaders.gamelogic.resolvers.characters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.actions.CharacterAction;
import com.leaders.gamelogic.actions.CharacterActionMotion;
import com.leaders.gamelogic.entities.Board;
import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.entities.Game;
import com.leaders.gamelogic.entities.GameConfig;
import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.gamelogic.entities.Player;
import com.leaders.gamelogic.entities.Position;
import com.leaders.gamelogic.enums.CharacterMotionType;
import com.leaders.gamelogic.enums.CharacterType;
import com.leaders.gamelogic.enums.GameMode;
import com.leaders.gamelogic.enums.GamePhaseType;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.gamelogic.historyentries.segments.Turn;
import com.leaders.gamelogic.interactions.CharacterActionBuilder;
import com.leaders.gamelogic.interactions.InteractionContext;
import com.leaders.gamelogic.interactions.InteractionFeedback;
import com.leaders.gamelogic.interactions.InteractionRequest;
import com.leaders.gamelogic.interactions.InteractionResult;
import com.leaders.gamelogic.interactions.InteractionResultType;
import com.leaders.gamelogic.interactions.InteractionTarget;
import com.leaders.gamelogic.interactions.InteractionType;
import com.leaders.gamelogic.interactions.TargetCategory;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;

public class FrogActionResolverTest {

    private static final Position FROG_POSITION = new Position(3, 3);
    // Adjacent cell (distance 1)
    private static final Position ADJACENT_DESTINATION = new Position(3, 2);
    // Cell at distance 2 in a straight line (same distance with any metric)
    private static final Position JUMP_DESTINATION = new Position(3, 1);
    // Cell at distance 3 in a straight line (out of range with any metric)
    private static final Position OUT_OF_RANGE_POSITION = new Position(3, 0);
    // Cell in range, occupied by an enemy
    private static final Position OCCUPIED_POSITION = new Position(3, 4);

    private Character frog;
    private FrogActionResolver resolver;

    private Game createTestGame() {
        // Build the minimal Game state required by the tests.
        // This state is intentionally invalid as a real game state.
        return new Game(new Board(),
                new ArrayList<>(), // recruitableCards
                new EnumMap<>(TeamColor.class), // playerRecruitedCharacters
                new EnumMap<>(TeamColor.class), // playerBanishedCards
                new EnumMap<>(TeamColor.class), // playerWarnings
                new HashMap<>() // charactersTransformedState
        );
    }

    private GameHistory createTestGameHistory() {
        // Build the minimal GameHistory state required by the tests.
        // This state is intentionally invalid as a real game state.
        ArrayList<Player> players = new ArrayList<>();
        players.add(new Player(TeamColor.Black, "Paul"));
        players.add(new Player(TeamColor.White, "Elise"));
        GameHistory history = new GameHistory(new GameConfig(
                players,
                players.get(1), // firstPlayer
                GameMode.Discovery,
                new ArrayList<>(), // initialRecruitableCards
                new ArrayList<>() // initialPlacements
        ), new ArrayList<>());

        Turn turn = new Turn(TeamColor.White);
        history.getEntries().add(turn);

        turn.getSubPhase(GamePhaseType.TurnStart).start();
        turn.getSubPhase(GamePhaseType.TurnStart).end();
        turn.getSubPhase(GamePhaseType.Actions).start();

        return history;
    }

    private CharacterActionBuilder createBuilder() {
        return new CharacterActionBuilder(frog);
    }

    @Before
    public void setUp() {
        Game game = createTestGame();

        Character leader = Character.create(CharacterType.LeaderKing, TeamColor.Black);
        game.getBoard().getCell(new Position(0, 0)).setCharacter(leader);

        frog = Character.create(CharacterType.Frog, TeamColor.Black);
        game.getBoard().getCell(FROG_POSITION).setCharacter(frog);

        Character enemy = Character.create(CharacterType.Hermit, TeamColor.White);
        game.getBoard().getCell(OCCUPIED_POSITION).setCharacter(enemy);

        resolver = new FrogActionResolver(game, createTestGameHistory(), frog);
    }

    @Test
    public void getNextInteraction_shouldRequestMovementDestinations() {
        InteractionRequest request = resolver.getNextInteraction(createBuilder());

        assertNotNull(request);
        assertEquals(InteractionType.PositionExpected, request.getRequestType());

        assertTrue(request.getLegalResults().contains(InteractionResultType.PositionChosen));
        assertTrue(request.getLegalResults().contains(InteractionResultType.CancelAction));

        for (InteractionTarget target : request.getLegalTargets()) {
            assertEquals(TargetCategory.MovementDestination, target.getCategory());
        }
    }

    @Test
    public void getNextInteraction_shouldExposeAdjacentEmptyCells() {
        InteractionRequest request = resolver.getNextInteraction(createBuilder());

        assertNotNull(request);
        assertTrue(containsTarget(request, ADJACENT_DESTINATION));
    }

    @Test
    public void getNextInteraction_shouldAllowJumpingToCellsAtRangeTwo() {
        InteractionRequest request = resolver.getNextInteraction(createBuilder());

        assertNotNull(request);
        assertTrue(containsTarget(request, JUMP_DESTINATION));
    }

    @Test
    public void getNextInteraction_shouldNotExposeCellsOutOfRange() {
        InteractionRequest request = resolver.getNextInteraction(createBuilder());

        assertNotNull(request);
        assertFalse(containsTarget(request, OUT_OF_RANGE_POSITION));
    }

    @Test
    public void getNextInteraction_shouldNotExposeOccupiedCells() {
        InteractionRequest request = resolver.getNextInteraction(createBuilder());

        assertNotNull(request);
        assertFalse(containsTarget(request, OCCUPIED_POSITION));
    }

    @Test
    public void getNextInteraction_shouldNotExposeFrogCurrentPosition() {
        InteractionRequest request = resolver.getNextInteraction(createBuilder());

        assertNotNull(request);
        assertFalse(containsTarget(request, FROG_POSITION));
    }

    @Test
    public void getNextInteraction_shouldReturnNullAfterPositionChosen() {
        CharacterActionBuilder builder = createBuilder();
        builder.addResult(createPositionResult());

        assertNull(resolver.getNextInteraction(builder));
    }

    @Test
    public void getNextFeedback_shouldCreateJumpFeedback() {
        CharacterActionBuilder builder = createBuilder();
        builder.addResult(createPositionResult());

        InteractionFeedback feedback = resolver.getNextFeedback(builder);

        assertNotNull(feedback);

        List<CharacterActionMotion> motions = feedback.getCharacterActionMotions();
        assertEquals(1, motions.size());

        CharacterActionMotion motion = motions.get(0);
        assertEquals(CharacterMotionType.Jump, motion.getMotionType());
        assertEquals(1, motion.getTargets().size());

        assertEquals(frog, motion.getTargets().get(0).getCharacter());
        assertEquals(FROG_POSITION, motion.getTargets().get(0).getOriginPos());
        assertEquals(JUMP_DESTINATION, motion.getTargets().get(0).getDestPos());
    }

    @Test
    public void getNextFeedback_shouldReturnNullAfterFeedbackWasGenerated() {
        CharacterActionBuilder builder = createBuilder();
        builder.addResult(createPositionResult());

        InteractionFeedback feedback = resolver.getNextFeedback(builder);
        assertNotNull(feedback);

        builder.addFeedback(feedback);

        assertNull(resolver.getNextFeedback(builder));
    }

    @Test
    public void buildAction_shouldBuildJumpAction() {
        CharacterActionBuilder builder = createBuilder();
        builder.addResult(createPositionResult());

        InteractionFeedback feedback = resolver.getNextFeedback(builder);
        assertNotNull(feedback);

        builder.addFeedback(feedback);

        CharacterAction action = resolver.buildAction(builder);

        assertEquals(frog, action.getSrcCharacter());
        assertEquals(1, action.getMotions().size());

        CharacterActionMotion motion = action.getMotions().get(0);
        assertEquals(CharacterMotionType.Jump, motion.getMotionType());
        assertEquals(1, motion.getTargets().size());

        assertEquals(frog, motion.getTargets().get(0).getCharacter());
        assertEquals(FROG_POSITION, motion.getTargets().get(0).getOriginPos());
        assertEquals(JUMP_DESTINATION, motion.getTargets().get(0).getDestPos());
    }

    @NonNull
    private InteractionResult createPositionResult() {
        return new InteractionResult(
                InteractionResultType.PositionChosen,
                new InteractionContext(frog),
                new InteractionTarget(TargetCategory.MovementDestination, FrogActionResolverTest.JUMP_DESTINATION)
        );
    }

    private boolean containsTarget(@NonNull InteractionRequest request,
                                   @NonNull Position position) {
        for (InteractionTarget target : request.getLegalTargets()) {
            if (target.getCategory() == TargetCategory.MovementDestination &&
                    position.equals(target.getChosenPosition())) {
                return true;
            }
        }

        return false;
    }
}