package com.leaders.gamelogic.resolvers.characters;

import static org.junit.Assert.assertEquals;
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

public class ClawLauncherActionResolverTest {

    private Character character;
    private Character target;
    private ClawLauncherActionResolver resolver;

    private Game createTestGame() {
        return new Game(new Board(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new EnumMap<>(TeamColor.class));
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
        return new CharacterActionBuilder(character, new ArrayList<>(), new ArrayList<>());
    }

    @NonNull
    private InteractionResult createResult(@NonNull TargetCategory category, @NonNull Position position) {
        return new InteractionResult(
                InteractionResultType.PositionChosen,
                new InteractionTarget(category, position)
        );
    }

    @Before
    public void setUp() {
        Game game = createTestGame();
        GameHistory gameHistory = createTestGameHistory();

        Character leader = Character.create(CharacterType.LeaderKing, TeamColor.Black);
        game.getBoard().getCell(new Position(0, 1)).setCharacter(leader);

        character = Character.create(CharacterType.ClawLauncher, TeamColor.Black);
        target = Character.create(CharacterType.Hermit, TeamColor.White);

        game.getBoard().getCell(new Position(3, 3)).setCharacter(character);
        game.getBoard().getCell(new Position(3, 0)).setCharacter(target);

        resolver = new ClawLauncherActionResolver(game, gameHistory, character);
    }

    @Test
    public void getNextInteraction_shouldRequestMovementAndAbilityTargets() {
        CharacterActionBuilder builder = createBuilder();

        InteractionRequest request = resolver.getNextInteraction(builder);

        assertNotNull(request);
        assertEquals(InteractionType.PositionExpected, request.getType());
        assertTrue(request.getLegalResults().contains(InteractionResultType.PositionChosen));
        assertTrue(request.getLegalResults().contains(InteractionResultType.CancelAction));

        boolean hasMovementDestination = false;
        boolean hasAbilityTarget = false;
        boolean hasAbilityDestination = false;

        for (InteractionTarget target : request.getLegalTargets()) {
            if (target.getCategory() == TargetCategory.MovementDestination) {
                hasMovementDestination = true;
            } else if (target.getCategory() == TargetCategory.ActiveAbilityTargetPosition) {
                hasAbilityTarget = true;
                assertEquals(new Position(3, 0), target.getChosenPosition());
            } else if (target.getCategory() == TargetCategory.ActiveAbilityDestination) {
                hasAbilityDestination = true;
                assertEquals(new Position(3, 1), target.getChosenPosition());
            }
        }

        assertTrue(hasMovementDestination);
        assertTrue(hasAbilityTarget);
        assertTrue(hasAbilityDestination);
    }

    @Test
    public void getNextInteraction_shouldNotReturnSecondInteraction() {
        CharacterActionBuilder builder = createBuilder();
        builder.addResult(createResult(
                TargetCategory.ActiveAbilityTargetPosition,
                new Position(3, 0)
        ));

        InteractionRequest request = resolver.getNextInteraction(builder);

        assertNull(request);
    }

    @Test
    public void getNextFeedback_shouldMoveTargetTowardClawLauncher() {
        Position targetPosition = new Position(3, 0);
        CharacterActionBuilder builder = createBuilder();
        builder.addResult(createResult(
                TargetCategory.ActiveAbilityTargetPosition,
                targetPosition
        ));

        InteractionFeedback feedback = resolver.getNextFeedback(builder);

        assertNotNull(feedback);

        CharacterActionMotion motion = feedback.getCharacterActionMotion();

        assertEquals(CharacterMotionType.Move, motion.getMotionType());
        assertEquals(1, motion.getTargets().size());
        assertEquals(target, motion.getTargets().get(0).getCharacter());
        assertEquals(targetPosition, motion.getTargets().get(0).getOriginPos());
        assertEquals(new Position(3, 2), motion.getTargets().get(0).getDestPos());
    }

    @Test
    public void getNextFeedback_shouldMoveClawLauncherTowardTarget() {
        Position destination = new Position(3, 1);
        CharacterActionBuilder builder = createBuilder();
        builder.addResult(createResult(
                TargetCategory.ActiveAbilityDestination,
                destination
        ));

        InteractionFeedback feedback = resolver.getNextFeedback(builder);

        assertNotNull(feedback);

        CharacterActionMotion motion = feedback.getCharacterActionMotion();

        assertEquals(CharacterMotionType.Move, motion.getMotionType());
        assertEquals(1, motion.getTargets().size());
        assertEquals(character, motion.getTargets().get(0).getCharacter());
        assertEquals(new Position(3, 3), motion.getTargets().get(0).getOriginPos());
        assertEquals(destination, motion.getTargets().get(0).getDestPos());
    }

    @Test
    public void getNextFeedback_shouldUseDefaultMovementForMovementDestination() {
        Position destination = new Position(3, 2);
        CharacterActionBuilder builder = createBuilder();
        builder.addResult(createResult(
                TargetCategory.MovementDestination,
                destination
        ));

        InteractionFeedback feedback = resolver.getNextFeedback(builder);

        assertNotNull(feedback);

        CharacterActionMotion motion = feedback.getCharacterActionMotion();

        assertEquals(CharacterMotionType.Move, motion.getMotionType());
        assertEquals(character, motion.getTargets().get(0).getCharacter());
        assertEquals(new Position(3, 3), motion.getTargets().get(0).getOriginPos());
        assertEquals(destination, motion.getTargets().get(0).getDestPos());
    }

    @Test
    public void getNextFeedback_shouldReturnNullAfterFeedbackWasGenerated() {
        Position destination = new Position(3, 1);
        CharacterActionBuilder builder = createBuilder();
        builder.addResult(createResult(
                TargetCategory.ActiveAbilityDestination,
                destination
        ));

        InteractionFeedback feedback = resolver.getNextFeedback(builder);
        assertNotNull(feedback);

        builder.addFeedback(feedback);

        assertNull(resolver.getNextFeedback(builder));
    }

    @Test
    public void buildAction_shouldBuildActionForTargetPull() {
        Position targetPosition = new Position(3, 0);
        CharacterActionBuilder builder = createBuilder();
        builder.addResult(createResult(
                TargetCategory.ActiveAbilityTargetPosition,
                targetPosition
        ));

        InteractionFeedback feedback = resolver.getNextFeedback(builder);
        assertNotNull(feedback);

        builder.addFeedback(feedback);

        CharacterAction action = resolver.buildAction(builder);

        assertEquals(character, action.getSrcCharacter());
        assertEquals(1, action.getMotions().size());

        CharacterActionMotion motion = action.getMotions().get(0);

        assertEquals(CharacterMotionType.Move, motion.getMotionType());
        assertEquals(target, motion.getTargets().get(0).getCharacter());
        assertEquals(targetPosition, motion.getTargets().get(0).getOriginPos());
        assertEquals(new Position(3, 2), motion.getTargets().get(0).getDestPos());
    }

    @Test
    public void buildAction_shouldBuildActionForClawLauncherPull() {
        Position destination = new Position(3, 1);
        CharacterActionBuilder builder = createBuilder();
        builder.addResult(createResult(
                TargetCategory.ActiveAbilityDestination,
                destination
        ));

        InteractionFeedback feedback = resolver.getNextFeedback(builder);
        assertNotNull(feedback);

        builder.addFeedback(feedback);

        CharacterAction action = resolver.buildAction(builder);

        assertEquals(character, action.getSrcCharacter());
        assertEquals(1, action.getMotions().size());

        CharacterActionMotion motion = action.getMotions().get(0);

        assertEquals(CharacterMotionType.Move, motion.getMotionType());
        assertEquals(character, motion.getTargets().get(0).getCharacter());
        assertEquals(new Position(3, 3), motion.getTargets().get(0).getOriginPos());
        assertEquals(destination, motion.getTargets().get(0).getDestPos());
    }

    @Test
    public void buildAction_shouldBuildActionForDefaultMovement() {
        Position destination = new Position(3, 2);
        CharacterActionBuilder builder = createBuilder();
        builder.addResult(createResult(
                TargetCategory.MovementDestination,
                destination
        ));

        InteractionFeedback feedback = resolver.getNextFeedback(builder);
        assertNotNull(feedback);

        builder.addFeedback(feedback);

        CharacterAction action = resolver.buildAction(builder);

        assertEquals(character, action.getSrcCharacter());
        assertEquals(1, action.getMotions().size());
        assertEquals(CharacterMotionType.Move,
                action.getMotions().get(0).getMotionType());
    }

    @Test
    public void getNextFeedback_shouldReturnNullAfterCancelAction() {
        CharacterActionBuilder builder = createBuilder();

        builder.addResult(new InteractionResult(
                InteractionResultType.CancelAction,
                null
        ));

        assertNull(resolver.getNextFeedback(builder));
    }
}