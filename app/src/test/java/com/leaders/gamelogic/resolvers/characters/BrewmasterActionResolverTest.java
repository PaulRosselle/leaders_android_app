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
import com.leaders.gamelogic.enums.TeamColor;
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
import java.util.List;

public class BrewmasterActionResolverTest {

    private static final Position BREWMASTER_POSITION = new Position(3, 3);

    private Game game;
    private Character brewmaster;
    private BrewmasterActionResolver resolver;

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
        return new GameHistory(new GameConfig(players, players.get(1), GameMode.Discovery,
                new ArrayList<>(), new ArrayList<>()), new ArrayList<>());
    }

    private CharacterActionBuilder createBuilder() {
        return new CharacterActionBuilder(brewmaster, new ArrayList<>(), new ArrayList<>());
    }



    @NonNull
    private Character placeCharacter(@NonNull CharacterType type, @NonNull TeamColor teamColor,
                                     @NonNull Position position) {
        Character character = Character.create(type, teamColor);
        game.getBoard().getCell(position).setCharacter(character);
        return character;
    }

    private void occupyAdjacentCells(@NonNull Position position) {
        // Keep the test independent of the exact number of board cells around the target.
        // Only occupy cells that are available inside the board.
        for (com.leaders.gamelogic.enums.Direction direction : com.leaders.gamelogic.enums.Direction.values()) {
            com.leaders.gamelogic.entities.Cell cell =
                    com.leaders.gamelogic.queries.BoardQuery.findAdjacentCell(game.getBoard(), position, direction);
            if (cell != null && cell.getCharacter() == null) {
                placeCharacter(CharacterType.Hermit, TeamColor.White, cell.getPosition());
            }
        }
    }

    @NonNull
    private InteractionResult createPositionResult(@NonNull TargetCategory category,
                                                   @NonNull Position position) {
        return new InteractionResult(InteractionResultType.PositionChosen,
                new InteractionContext(brewmaster),
                new InteractionTarget(category, position));
    }

    private boolean containsTarget(@NonNull InteractionRequest request, @NonNull TargetCategory category,
                                   @NonNull Position position) {
        for (InteractionTarget target : request.getLegalTargets()) {
            if (target.getCategory() == category && position.equals(target.getChosenPosition())) {
                return true;
            }
        }
        return false;
    }

    @Before
    public void setUp() {
        game = createTestGame();

        Character leader = Character.create(CharacterType.LeaderKing, TeamColor.Black);
        game.getBoard().getCell(new Position(0, 0)).setCharacter(leader);

        brewmaster = Character.create(CharacterType.Brewmaster, TeamColor.Black);
        game.getBoard().getCell(BREWMASTER_POSITION).setCharacter(brewmaster);

        resolver = new BrewmasterActionResolver(game, createTestGameHistory(), brewmaster);
    }

    @Test
    public void getNextInteraction_shouldRequestNormalMovementAndBrewmasterTargets() {
        Position normalDestination = new Position(3, 2);
        Position allyPosition = new Position(3, 4);

        placeCharacter(CharacterType.Hermit, TeamColor.Black, allyPosition);

        InteractionRequest request = resolver.getNextInteraction(createBuilder());

        assertNotNull(request);
        assertEquals(InteractionType.PositionExpected, request.getRequestType());
        assertTrue(request.getLegalResults().contains(InteractionResultType.PositionChosen));
        assertTrue(request.getLegalResults().contains(InteractionResultType.CancelAction));

        assertTrue(containsTarget(request, TargetCategory.MovementDestination, normalDestination));
        assertTrue(containsTarget(request, TargetCategory.ActiveAbilityTargetPosition, allyPosition));
    }

    @Test
    public void getNextInteraction_shouldNotExposeAllyWithoutValidDestination() {
        Position allyPosition = new Position(3, 4);

        placeCharacter(CharacterType.Hermit, TeamColor.Black, allyPosition);

        occupyAdjacentCells(allyPosition);

        InteractionRequest request = resolver.getNextInteraction(createBuilder());

        assertNotNull(request);
        assertFalse(containsTarget(request, TargetCategory.ActiveAbilityTargetPosition, allyPosition));
    }

    @Test
    public void getNextInteraction_shouldReturnDestinationChoicesAfterAllySelected() {
        Position allyPosition = new Position(3, 4);
        Position destination = new Position(3, 5);

        placeCharacter(CharacterType.Hermit, TeamColor.Black, allyPosition);

        CharacterActionBuilder builder = createBuilder();
        builder.addResult(createPositionResult(TargetCategory.ActiveAbilityTargetPosition, allyPosition));

        InteractionRequest request = resolver.getNextInteraction(builder);

        assertNotNull(request);
        assertEquals(InteractionType.PositionExpected, request.getRequestType());
        assertTrue(request.getLegalResults().contains(InteractionResultType.PositionChosen));
        assertTrue(request.getLegalResults().contains(InteractionResultType.CancelAction));
        assertTrue(containsTarget(request, TargetCategory.ActiveAbilityDestination, destination));
    }

    @Test
    public void getNextInteraction_shouldDelegateNormalMovementToParent() {
        Position destination = new Position(3, 2);

        CharacterActionBuilder builder = createBuilder();
        builder.addResult(createPositionResult(TargetCategory.MovementDestination, destination));

        assertNull(resolver.getNextInteraction(builder));
    }

    @Test
    public void getNextFeedback_shouldDelegateNormalMovementToParent() {
        Position destination = new Position(3, 2);

        CharacterActionBuilder builder = createBuilder();
        builder.addResult(createPositionResult(TargetCategory.MovementDestination, destination));

        InteractionFeedback feedback = resolver.getNextFeedback(builder);

        assertNotNull(feedback);

        List<CharacterActionMotion> motions = feedback.getCharacterActionMotions();

        assertEquals(1, motions.size());
        CharacterActionMotion motion = motions.get(0);

        assertEquals(CharacterMotionType.Move, motion.getMotionType());
        assertEquals(1, motion.getTargets().size());
        assertEquals(brewmaster, motion.getTargets().get(0).getCharacter());
        assertEquals(BREWMASTER_POSITION, motion.getTargets().get(0).getOriginPos());
        assertEquals(destination, motion.getTargets().get(0).getDestPos());
    }

    @Test
    public void getNextFeedback_shouldCreateMoveForBrewmasterAbility() {
        Position allyPosition = new Position(3, 4);
        Position destination = new Position(3, 5);

        Character ally = placeCharacter(CharacterType.Hermit, TeamColor.Black, allyPosition);

        CharacterActionBuilder builder = createBuilder();
        builder.addResult(createPositionResult(TargetCategory.ActiveAbilityTargetPosition, allyPosition));
        builder.addResult(createPositionResult(TargetCategory.ActiveAbilityDestination, destination));

        InteractionFeedback feedback = resolver.getNextFeedback(builder);

        assertNotNull(feedback);

        List<CharacterActionMotion> motions = feedback.getCharacterActionMotions();

        assertEquals(1, motions.size());
        CharacterActionMotion motion = motions.get(0);

        assertEquals(CharacterMotionType.Move, motion.getMotionType());
        assertEquals(1, motion.getTargets().size());
        assertEquals(ally, motion.getTargets().get(0).getCharacter());
        assertEquals(allyPosition, motion.getTargets().get(0).getOriginPos());
        assertEquals(destination, motion.getTargets().get(0).getDestPos());
    }

    @Test
    public void getNextFeedback_shouldReturnNullBeforeDestinationIsSelected() {
        Position allyPosition = new Position(3, 4);

        placeCharacter(CharacterType.Hermit, TeamColor.Black, allyPosition);

        CharacterActionBuilder builder = createBuilder();
        builder.addResult(createPositionResult(TargetCategory.ActiveAbilityTargetPosition, allyPosition));

        assertNull(resolver.getNextFeedback(builder));
    }

    @Test
    public void getNextFeedback_shouldReturnNullAfterFeedbackWasGenerated() {
        Position allyPosition = new Position(3, 4);
        Position destination = new Position(3, 5);

        placeCharacter(CharacterType.Hermit, TeamColor.Black, allyPosition);

        CharacterActionBuilder builder = createBuilder();
        builder.addResult(createPositionResult(TargetCategory.ActiveAbilityTargetPosition, allyPosition));
        builder.addResult(createPositionResult(TargetCategory.ActiveAbilityDestination, destination));

        InteractionFeedback feedback = resolver.getNextFeedback(builder);

        assertNotNull(feedback);

        builder.addFeedback(feedback);

        assertNull(resolver.getNextFeedback(builder));
    }

    @Test
    public void buildAction_shouldBuildNormalMoveAction() {
        Position destination = new Position(3, 2);

        CharacterActionBuilder builder = createBuilder();
        builder.addResult(createPositionResult(TargetCategory.MovementDestination, destination));

        InteractionFeedback feedback = resolver.getNextFeedback(builder);

        assertNotNull(feedback);

        builder.addFeedback(feedback);

        CharacterAction action = resolver.buildAction(builder);

        assertEquals(brewmaster, action.getSrcCharacter());
        assertEquals(1, action.getMotions().size());
        assertEquals(CharacterMotionType.Move, action.getMotions().get(0).getMotionType());
    }

    @Test
    public void buildAction_shouldBuildBrewmasterAbilityAction() {
        Position allyPosition = new Position(3, 4);
        Position destination = new Position(3, 5);

        placeCharacter(CharacterType.Hermit, TeamColor.Black, allyPosition);

        CharacterActionBuilder builder = createBuilder();
        builder.addResult(createPositionResult(TargetCategory.ActiveAbilityTargetPosition, allyPosition));
        builder.addResult(createPositionResult(TargetCategory.ActiveAbilityDestination, destination));

        InteractionFeedback feedback = resolver.getNextFeedback(builder);

        assertNotNull(feedback);

        builder.addFeedback(feedback);

        CharacterAction action = resolver.buildAction(builder);

        assertEquals(brewmaster, action.getSrcCharacter());
        assertEquals(1, action.getMotions().size());
        assertEquals(CharacterMotionType.Move, action.getMotions().get(0).getMotionType());
        assertEquals(allyPosition, action.getMotions().get(0).getTargets().get(0).getOriginPos());
        assertEquals(destination, action.getMotions().get(0).getTargets().get(0).getDestPos());
    }

    @Test
    public void getNextInteraction_shouldIgnoreAdjacentEnemyAsBrewmasterTarget() {
        Position enemyPosition = new Position(3, 4);

        placeCharacter(CharacterType.Hermit, TeamColor.White, enemyPosition);

        InteractionRequest request = resolver.getNextInteraction(createBuilder());

        assertNotNull(request);
        assertFalse(containsTarget(request, TargetCategory.ActiveAbilityTargetPosition, enemyPosition));
    }

    @Test
    public void getNextInteraction_shouldNotExposeOccupiedDestination() {
        Position allyPosition = new Position(3, 4);
        Position occupiedDestination = new Position(3, 5);

        placeCharacter(CharacterType.Hermit, TeamColor.Black, allyPosition);
        placeCharacter(CharacterType.Rider, TeamColor.White, occupiedDestination);

        CharacterActionBuilder builder = createBuilder();
        builder.addResult(createPositionResult(TargetCategory.ActiveAbilityTargetPosition, allyPosition));

        InteractionRequest request = resolver.getNextInteraction(builder);

        assertNotNull(request);
        assertFalse(containsTarget(request, TargetCategory.ActiveAbilityTargetPosition, occupiedDestination));
    }
}