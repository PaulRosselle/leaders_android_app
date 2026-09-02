package com.leaders.gamelogic.resolvers.characters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.actions.CharacterAction;
import com.leaders.gamelogic.actions.CharacterActionMotion;
import com.leaders.gamelogic.entities.Board;
import com.leaders.gamelogic.entities.Cell;
import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.entities.Game;
import com.leaders.gamelogic.entities.GameConfig;
import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.gamelogic.entities.Player;
import com.leaders.gamelogic.entities.Position;
import com.leaders.gamelogic.enums.CharacterMotionType;
import com.leaders.gamelogic.enums.CharacterType;
import com.leaders.gamelogic.enums.Direction;
import com.leaders.gamelogic.enums.GameMode;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.gamelogic.interactions.CharacterActionBuilder;
import com.leaders.gamelogic.interactions.InteractionContext;
import com.leaders.gamelogic.interactions.InteractionFeedback;
import com.leaders.gamelogic.interactions.InteractionRequest;
import com.leaders.gamelogic.interactions.InteractionResult;
import com.leaders.gamelogic.interactions.InteractionResultType;
import com.leaders.gamelogic.interactions.InteractionTarget;
import com.leaders.gamelogic.interactions.TargetCategory;
import com.leaders.gamelogic.queries.BoardQuery;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

public class NemesisActionResolverTest {
    private static final Position NEMESIS_POSITION = new Position(0, 0);

    private Character character;
    private Game game;
    private NemesisActionResolver resolver;

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

    private GameHistory createTestGameHistory() {
        ArrayList<Player> players = new ArrayList<>();
        players.add(new Player(TeamColor.Black, "Paul"));
        players.add(new Player(TeamColor.White, "Elise"));

        return new GameHistory(new GameConfig(
                players,
                players.get(1),
                GameMode.Discovery,
                new ArrayList<>(),
                new ArrayList<>()
        ), new ArrayList<>());
    }

    private CharacterActionBuilder createBuilder() {
        return new CharacterActionBuilder(character);
    }

    @NonNull
    private InteractionResult createMovementResult(@NonNull Position position) {
        return new InteractionResult(
                InteractionResultType.PositionChosen,
                new InteractionContext(character),
                new InteractionTarget(TargetCategory.MovementDestination, position)
        );
    }

    private void placeCharacter(@NonNull Position position) {
        Character character = Character.create(CharacterType.Hermit, TeamColor.Black);
        game.getBoard().getCell(position).setCharacter(character);
    }

    @Before
    public void setUp() {
        game = createTestGame();
        GameHistory gameHistory = createTestGameHistory();

        Character leader = Character.create(CharacterType.LeaderKing, TeamColor.Black);
        game.getBoard().getCell(new Position(6, 3)).setCharacter(leader);

        character = Character.create(CharacterType.Nemesis, TeamColor.Black);
        game.getBoard().getCell(NEMESIS_POSITION).setCharacter(character);

        resolver = new NemesisActionResolver(game, gameHistory, character);
    }

    @Test
    public void getNextInteraction_shouldRequestMovementDestination() {
        CharacterActionBuilder builder = createBuilder();

        InteractionRequest request = resolver.getNextInteraction(builder);

        assertNotNull(request);
        assertEquals(TargetCategory.MovementDestination,
                request.getLegalTargets().get(0).getCategory());
        assertTrue(request.getLegalResults().contains(InteractionResultType.PositionChosen));
        assertTrue(request.getLegalResults().contains(InteractionResultType.CancelAction));
    }

    @Test
    public void getNextInteraction_shouldPreferTwoStepDestinations() {
        CharacterActionBuilder builder = createBuilder();

        InteractionRequest request = resolver.getNextInteraction(builder);

        assertNotNull(request);

        for (InteractionTarget target : request.getLegalTargets()) {
            Position destPos = target.getChosenPosition();

            assertNotNull(destPos);
            assertTrue("Nemesis should only expose two-step destinations when available",
                    isReachableInTwoSteps(destPos)
            );
        }
    }

    @Test
    public void getNextInteraction_shouldNotReturnOriginalPosition() {
        CharacterActionBuilder builder = createBuilder();

        InteractionRequest request = resolver.getNextInteraction(builder);

        assertNotNull(request);

        for (InteractionTarget target : request.getLegalTargets()) {
            assertNotEquals(NEMESIS_POSITION, target.getChosenPosition());
        }
    }

    @Test
    public void getNextInteraction_shouldFallbackToOneStepDestinations() {
        placeCharacter(new Position(0, 1));
        placeCharacter(new Position(1, 1));
        placeCharacter(new Position(2, 0));
        placeCharacter(new Position(2, 1));

        CharacterActionBuilder builder = createBuilder();

        InteractionRequest request = resolver.getNextInteraction(builder);

        assertNotNull(request);
        assertEquals(1, request.getLegalTargets().size());

        InteractionTarget target = request.getLegalTargets().get(0);
        assertNotNull(target.getChosenPosition());
        assertTrue(isAdjacentToCharacter(target.getChosenPosition()));
    }

    @Test
    public void getNextInteraction_shouldNotMoveIfBlocked() {
        placeCharacter(new Position(0, 1));
        placeCharacter(new Position(1, 0));
        placeCharacter(new Position(1, 1));

        CharacterActionBuilder builder = createBuilder();

        InteractionRequest request = resolver.getNextInteraction(builder);

        assertNotNull(request);
        assertTrue(request.getLegalTargets().isEmpty());
    }

    @Test
    public void getNextInteraction_shouldReturnNullAfterPositionChosen() {
        CharacterActionBuilder builder = createBuilder();
        Position destination = new Position(3, 1);
        builder.addResult(createMovementResult(destination));

        InteractionRequest request = resolver.getNextInteraction(builder);

        assertNull(request);
    }

    @Test
    public void getNextFeedback_shouldCreateMoveFeedback() {
        Position intermediatePos = new Position(1, 1);
        Position destination = new Position(2, 2);
        CharacterActionBuilder builder = createBuilder();
        builder.addResult(createMovementResult(destination));

        InteractionFeedback feedback = resolver.getNextFeedback(builder);

        assertNotNull(feedback);

        List<CharacterActionMotion> motions = feedback.getCharacterActionMotions();

        assertEquals(2, motions.size());

        CharacterActionMotion firstMotion = motions.get(0);

        assertEquals(CharacterMotionType.Move, firstMotion.getMotionType());
        assertEquals(1, firstMotion.getTargets().size());
        assertEquals(character, firstMotion.getTargets().get(0).getCharacter());
        assertEquals(NEMESIS_POSITION, firstMotion.getTargets().get(0).getOriginPos());
        assertEquals(intermediatePos, firstMotion.getTargets().get(0).getDestPos());

        CharacterActionMotion secondMotion = motions.get(1);

        assertEquals(CharacterMotionType.Move, secondMotion.getMotionType());
        assertEquals(1, secondMotion.getTargets().size());
        assertEquals(character, secondMotion.getTargets().get(0).getCharacter());
        assertEquals(intermediatePos, secondMotion.getTargets().get(0).getOriginPos());
        assertEquals(destination, secondMotion.getTargets().get(0).getDestPos());
    }

    @Test
    public void getNextFeedback_shouldReturnNullForCancelAction() {
        CharacterActionBuilder builder = createBuilder();

        builder.addResult(new InteractionResult(
                InteractionResultType.CancelAction,
                new InteractionContext(character),
                null
        ));

        InteractionFeedback feedback = resolver.getNextFeedback(builder);

        assertNull(feedback);
    }

    @Test
    public void buildAction_shouldBuildActionFromFeedback() {
        Position destination = new Position(2, 2);
        CharacterActionBuilder builder = createBuilder();
        builder.addResult(createMovementResult(destination));

        InteractionFeedback feedback = resolver.getNextFeedback(builder);

        assertNotNull(feedback);

        builder.addFeedback(feedback);

        CharacterAction action = resolver.buildAction(builder);

        assertEquals(character, action.getSrcCharacter());
        assertEquals(2, action.getMotions().size());
        assertEquals(CharacterMotionType.Move, action.getMotions().get(1).getMotionType());
        assertEquals(1, action.getMotions().get(1).getTargets().size());
        assertEquals(destination, action.getMotions().get(1).getTargets().get(0).getDestPos());
    }

    private boolean isAdjacentToCharacter(@NonNull Position position) {
        return position.distanceTo(NEMESIS_POSITION) == 1;
    }

    private boolean isReachableInTwoSteps(@NonNull Position destination) {
        for (Direction firstDirection : Direction.values()) {
            Cell firstStepCell = BoardQuery.findAdjacentCell(
                    game.getBoard(), NEMESIS_POSITION, firstDirection);

            if (firstStepCell == null || firstStepCell.getCharacter() != null) {
                continue;
            }

            for (Direction secondDirection : Direction.values()) {
                Cell secondStepCell = BoardQuery.findAdjacentCell(
                        game.getBoard(), firstStepCell.getPosition(), secondDirection);

                if (secondStepCell != null &&
                        secondStepCell.getCharacter() == null &&
                        destination.equals(secondStepCell.getPosition())) {
                    return true;
                }
            }
        }

        return false;
    }

    private int getDistance(@NonNull Position second) {
        int dq = Math.abs(NemesisActionResolverTest.NEMESIS_POSITION.getQ() - second.getQ());
        int dr = Math.abs(NemesisActionResolverTest.NEMESIS_POSITION.getR() - second.getR());
        int ds = Math.abs(NemesisActionResolverTest.NEMESIS_POSITION.getS() - second.getS());

        return Math.max(dq, Math.max(dr, ds));
    }
}