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

public class ManipulatorActionResolverTest {

    private static final Position MANIPULATOR_POSITION = new Position(3, 3);
    private static final Position TARGET_POSITION = new Position(3, 5);
    private static final Position TARGET_DESTINATION = new Position(3, 6);

    private Game game;
    private Character manipulator;
    private Character target;
    private ManipulatorActionResolver resolver;

    private Game createTestGame() {
        return new Game(new Board(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
                new EnumMap<>(TeamColor.class));
    }

    private GameHistory createTestGameHistory() {
        ArrayList<Player> players = new ArrayList<>();
        players.add(new Player(TeamColor.Black, "Paul"));
        players.add(new Player(TeamColor.White, "Elise"));
        return new GameHistory(new GameConfig(players, players.get(1), GameMode.Discovery,
                new ArrayList<>(), new ArrayList<>()), new ArrayList<>());
    }

    private CharacterActionBuilder createBuilder() {
        return new CharacterActionBuilder(manipulator, new ArrayList<>(), new ArrayList<>());
    }

    @Before
    public void setUp() {
        game = createTestGame();

        Character leader = Character.create(CharacterType.LeaderKing, TeamColor.Black);
        game.getBoard().getCell(new Position(0, 0)).setCharacter(leader);

        manipulator = Character.create(CharacterType.Manipulator, TeamColor.Black);
        game.getBoard().getCell(MANIPULATOR_POSITION).setCharacter(manipulator);

        target = Character.create(CharacterType.Hermit, TeamColor.White);
        game.getBoard().getCell(TARGET_POSITION).setCharacter(target);

        resolver = new ManipulatorActionResolver(game, createTestGameHistory(), manipulator);
    }

    @Test
    public void getNextInteraction_shouldRequestNormalMovementAndManipulatorTargets() {
        Position normalDestination = new Position(3, 2);

        InteractionRequest request = resolver.getNextInteraction(createBuilder());

        assertNotNull(request);
        assertEquals(InteractionType.PositionExpected, request.getType());
        assertTrue(request.getLegalResults().contains(InteractionResultType.PositionChosen));
        assertTrue(request.getLegalResults().contains(InteractionResultType.CancelAction));

        assertTrue(containsTarget(request, TargetCategory.MovementDestination, normalDestination));
        assertTrue(containsTarget(request, TargetCategory.ActiveAbilityTargetPosition, TARGET_POSITION));
    }

    @Test
    public void getNextInteraction_shouldNotExposeAdjacentEnemyAsManipulatorTarget() {
        Position adjacentPosition = new Position(3, 4);

        game.getBoard().getCell(TARGET_POSITION).setCharacter(null);
        game.getBoard().getCell(adjacentPosition).setCharacter(target);

        InteractionRequest request = resolver.getNextInteraction(createBuilder());

        assertNotNull(request);
        assertFalse(containsTarget(request, TargetCategory.ActiveAbilityTargetPosition, adjacentPosition));
    }

    @Test
    public void getNextInteraction_shouldNotExposeEnemyBehindAdjacentCharacter() {
        Position adjacentPosition = new Position(3, 4);

        Character blockingCharacter = Character.create(CharacterType.Hermit, TeamColor.White);
        game.getBoard().getCell(adjacentPosition).setCharacter(blockingCharacter);

        InteractionRequest request = resolver.getNextInteraction(createBuilder());

        assertNotNull(request);
        assertFalse(containsTarget(request, TargetCategory.ActiveAbilityTargetPosition, TARGET_POSITION));
    }

    @Test
    public void getNextInteraction_shouldRequestTargetDestinationAfterTargetSelected() {
        CharacterActionBuilder builder = createBuilder();
        builder.addResult(createPositionResult(
                TargetCategory.ActiveAbilityTargetPosition, TARGET_POSITION));

        InteractionRequest request = resolver.getNextInteraction(builder);

        assertNotNull(request);
        assertEquals(InteractionType.PositionExpected, request.getType());
        assertTrue(request.getLegalResults().contains(InteractionResultType.PositionChosen));
        assertTrue(request.getLegalResults().contains(InteractionResultType.CancelAction));

        assertTrue(containsTarget(request, TargetCategory.ActiveAbilityDestination, TARGET_DESTINATION));
    }

    @Test
    public void getNextFeedback_shouldReturnNullBeforeDestinationIsSelected() {
        CharacterActionBuilder builder = createBuilder();
        builder.addResult(createPositionResult(
                TargetCategory.ActiveAbilityTargetPosition, TARGET_POSITION));

        assertNull(resolver.getNextFeedback(builder));
    }

    @Test
    public void getNextFeedback_shouldDelegateNormalMovementToParent() {
        Position destination = new Position(3, 2);

        CharacterActionBuilder builder = createBuilder();
        builder.addResult(createPositionResult(
                TargetCategory.MovementDestination, destination));

        InteractionFeedback feedback = resolver.getNextFeedback(builder);

        assertNotNull(feedback);

        CharacterActionMotion motion = feedback.getCharacterActionMotion();

        assertEquals(CharacterMotionType.Move, motion.getMotionType());
        assertEquals(1, motion.getTargets().size());
        assertEquals(manipulator, motion.getTargets().get(0).getCharacter());
        assertEquals(MANIPULATOR_POSITION, motion.getTargets().get(0).getOriginPos());
        assertEquals(destination, motion.getTargets().get(0).getDestPos());
    }

    @Test
    public void getNextFeedback_shouldCreateMoveFeedbackForTarget() {
        CharacterActionBuilder builder = createBuilder();
        builder.addResult(createPositionResult(
                TargetCategory.ActiveAbilityTargetPosition, TARGET_POSITION));
        builder.addResult(createPositionResult(
                TargetCategory.ActiveAbilityDestination, TARGET_DESTINATION));

        InteractionFeedback feedback = resolver.getNextFeedback(builder);

        assertNotNull(feedback);

        CharacterActionMotion motion = feedback.getCharacterActionMotion();

        assertEquals(CharacterMotionType.Move, motion.getMotionType());
        assertEquals(1, motion.getTargets().size());
        assertEquals(target, motion.getTargets().get(0).getCharacter());
        assertEquals(TARGET_POSITION, motion.getTargets().get(0).getOriginPos());
        assertEquals(TARGET_DESTINATION, motion.getTargets().get(0).getDestPos());
    }

    @Test
    public void getNextFeedback_shouldReturnNullAfterFeedbackWasGenerated() {
        CharacterActionBuilder builder = createBuilder();
        builder.addResult(createPositionResult(
                TargetCategory.ActiveAbilityTargetPosition, TARGET_POSITION));
        builder.addResult(createPositionResult(
                TargetCategory.ActiveAbilityDestination, TARGET_DESTINATION));

        InteractionFeedback feedback = resolver.getNextFeedback(builder);

        assertNotNull(feedback);

        builder.addFeedback(feedback);

        assertNull(resolver.getNextFeedback(builder));
    }

    @Test
    public void buildAction_shouldBuildNormalMoveAction() {
        Position destination = new Position(3, 2);

        CharacterActionBuilder builder = createBuilder();
        builder.addResult(createPositionResult(
                TargetCategory.MovementDestination, destination));

        InteractionFeedback feedback = resolver.getNextFeedback(builder);

        assertNotNull(feedback);

        builder.addFeedback(feedback);

        CharacterAction action = resolver.buildAction(builder);

        assertEquals(manipulator, action.getSrcCharacter());
        assertEquals(1, action.getMotions().size());
        assertEquals(CharacterMotionType.Move, action.getMotions().get(0).getMotionType());
    }

    @Test
    public void buildAction_shouldBuildManipulatorMoveAction() {
        CharacterActionBuilder builder = createBuilder();
        builder.addResult(createPositionResult(
                TargetCategory.ActiveAbilityTargetPosition, TARGET_POSITION));
        builder.addResult(createPositionResult(
                TargetCategory.ActiveAbilityDestination, TARGET_DESTINATION));

        InteractionFeedback feedback = resolver.getNextFeedback(builder);

        assertNotNull(feedback);

        builder.addFeedback(feedback);

        CharacterAction action = resolver.buildAction(builder);

        assertEquals(manipulator, action.getSrcCharacter());
        assertEquals(1, action.getMotions().size());

        CharacterActionMotion motion = action.getMotions().get(0);

        assertEquals(CharacterMotionType.Move, motion.getMotionType());
        assertEquals(1, motion.getTargets().size());
        assertEquals(target, motion.getTargets().get(0).getCharacter());
        assertEquals(TARGET_POSITION, motion.getTargets().get(0).getOriginPos());
        assertEquals(TARGET_DESTINATION, motion.getTargets().get(0).getDestPos());
    }

    @Test
    public void getNextInteraction_shouldNotExposeOccupiedDestination() {
        Character occupant = Character.create(CharacterType.Hermit, TeamColor.Black);
        game.getBoard().getCell(TARGET_DESTINATION).setCharacter(occupant);

        CharacterActionBuilder builder = createBuilder();
        builder.addResult(createPositionResult(
                TargetCategory.ActiveAbilityTargetPosition, TARGET_POSITION));

        InteractionRequest request = resolver.getNextInteraction(builder);

        assertNotNull(request);
        assertFalse(containsTarget(request, TargetCategory.ActiveAbilityDestination, TARGET_DESTINATION));
    }

    @NonNull
    private InteractionResult createPositionResult(@NonNull TargetCategory category,
                                                   @NonNull Position position) {
        return new InteractionResult(
                InteractionResultType.PositionChosen,
                new InteractionTarget(category, position)
        );
    }

    private boolean containsTarget(@NonNull InteractionRequest request,
                                   @NonNull TargetCategory category,
                                   @NonNull Position position) {
        for (InteractionTarget target : request.getLegalTargets()) {
            if (target.getCategory() == category &&
                    position.equals(target.getChosenPosition())) {
                return true;
            }
        }

        return false;
    }
}