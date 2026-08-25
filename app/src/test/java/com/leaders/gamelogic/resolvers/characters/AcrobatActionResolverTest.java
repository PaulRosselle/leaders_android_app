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

public class AcrobatActionResolverTest {
    private Game game;
    private Character acrobat;
    private AcrobatActionResolver resolver;

    private final Position acrobatPosition = new Position(3, 3);

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

        return new GameHistory(
                new GameConfig(
                        players,
                        players.get(1),
                        GameMode.Discovery,
                        new ArrayList<>(),
                        new ArrayList<>()
                ),
                new ArrayList<>()
        );
    }

    private CharacterActionBuilder createBuilder() {
        return new CharacterActionBuilder(acrobat, new ArrayList<>(), new ArrayList<>());
    }

    private void placeCharacter(@NonNull Position position) {
        Character character = Character.create(CharacterType.Hermit, TeamColor.Black);
        game.getBoard().getCell(position).setCharacter(character);
    }

    @NonNull
    private InteractionResult createResult(@NonNull TargetCategory category, @NonNull Position position) {
        return new InteractionResult(InteractionResultType.PositionChosen,
                new InteractionContext(acrobat),
                new InteractionTarget(category, position));
    }

    private boolean containsTarget(@NonNull InteractionRequest request, @NonNull TargetCategory category, @NonNull Position position) {
        for (InteractionTarget target : request.getLegalTargets()) {
            if (target.getCategory() == category &&
                    position.equals(target.getChosenPosition())) {
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
        acrobat = Character.create(CharacterType.Acrobat, TeamColor.Black);
        game.getBoard().getCell(acrobatPosition).setCharacter(acrobat);

        resolver = new AcrobatActionResolver(
                game,
                createTestGameHistory(),
                acrobat
        );
    }

    @Test
    public void getNextInteraction_shouldExposeNormalAndAcrobatDestinations() {
        Position normalDestination = new Position(2, 2);
        Position jumpOverCharacterDestination = new Position(3, 1);

        placeCharacter(new Position(3, 2));

        InteractionRequest request = resolver.getNextInteraction(createBuilder());

        assertNotNull(request);
        assertEquals(InteractionType.PositionExpected, request.getRequestType());
        assertTrue(request.getLegalResults().contains(InteractionResultType.PositionChosen));
        assertTrue(request.getLegalResults().contains(InteractionResultType.CancelAction));
        assertTrue(containsTarget(request, TargetCategory.MovementDestination, normalDestination));
        assertTrue(containsTarget(request, TargetCategory.ActiveAbilityDestination, jumpOverCharacterDestination));
    }

    @Test
    public void getNextInteraction_shouldReturnNullAfterMovementChoice() {
        CharacterActionBuilder builder = createBuilder();
        builder.addResult(createResult(TargetCategory.MovementDestination, new Position(3, 2)));
        assertNull(resolver.getNextInteraction(builder));
    }

    @Test
    public void getNextInteraction_shouldReturnNullAfterAcrobatAbilityChoice() {
        CharacterActionBuilder builder = createBuilder();
        builder.addResult(createResult(TargetCategory.ActiveAbilityDestination, new Position(3, 1)));

        assertNull(resolver.getNextInteraction(builder));
    }

    @Test
    public void getNextFeedback_shouldDelegateNormalMovementToParent() {
        Position destination = new Position(3, 2);

        CharacterActionBuilder builder = createBuilder();
        builder.addResult(createResult(TargetCategory.MovementDestination, destination));

        InteractionFeedback feedback = resolver.getNextFeedback(builder);

        assertNotNull(feedback);

        List<CharacterActionMotion> motions = feedback.getCharacterActionMotions();

        assertEquals(1, motions.size());
        CharacterActionMotion motion = motions.get(0);

        assertEquals(CharacterMotionType.Move, motion.getMotionType());
        assertEquals(acrobat, motion.getTargets().get(0).getCharacter());
        assertEquals(acrobatPosition, motion.getTargets().get(0).getOriginPos());
        assertEquals(destination, motion.getTargets().get(0).getDestPos());
    }

    @Test
    public void getNextFeedback_shouldCreateJumpForAcrobatAbility() {
        Position destination = new Position(3, 1);

        CharacterActionBuilder builder = createBuilder();
        builder.addResult(createResult(TargetCategory.ActiveAbilityDestination, destination));

        InteractionFeedback feedback = resolver.getNextFeedback(builder);

        assertNotNull(feedback);

        List<CharacterActionMotion> motions = feedback.getCharacterActionMotions();

        assertEquals(1, motions.size());
        CharacterActionMotion motion = motions.get(0);

        assertEquals(CharacterMotionType.Jump, motion.getMotionType());
        assertEquals(1, motion.getTargets().size());
        assertEquals(acrobat, motion.getTargets().get(0).getCharacter());
        assertEquals(acrobatPosition, motion.getTargets().get(0).getOriginPos());
        assertEquals(destination, motion.getTargets().get(0).getDestPos());
    }

    @Test
    public void getNextFeedback_shouldReturnNullAfterFeedbackWasAlreadyGenerated() {
        CharacterActionBuilder builder = createBuilder();

        builder.addResult(createResult(TargetCategory.ActiveAbilityDestination, new Position(3, 1)));

        InteractionFeedback feedback = resolver.getNextFeedback(builder);

        assertNotNull(feedback);

        builder.addFeedback(feedback);

        assertNull(resolver.getNextFeedback(builder));
    }

    @Test
    public void getNextFeedback_shouldReturnNullWithoutInteractionResult() {
        CharacterActionBuilder builder = createBuilder();

        assertNull(resolver.getNextFeedback(builder));
    }

    @Test
    public void buildAction_shouldBuildJumpAction() {
        Position destination = new Position(3, 1);

        CharacterActionBuilder builder = createBuilder();
        builder.addResult(createResult(TargetCategory.ActiveAbilityDestination, destination));

        InteractionFeedback feedback = resolver.getNextFeedback(builder);

        assertNotNull(feedback);

        builder.addFeedback(feedback);

        CharacterAction action = resolver.buildAction(builder);

        assertEquals(acrobat, action.getSrcCharacter());
        assertEquals(1, action.getMotions().size());
        CharacterActionMotion motion = action.getMotions().get(0);
        assertEquals(CharacterMotionType.Jump, motion.getMotionType());
        assertEquals(acrobat, motion.getTargets().get(0).getCharacter());
        assertEquals(acrobatPosition, motion.getTargets().get(0).getOriginPos());
        assertEquals(destination, motion.getTargets().get(0).getDestPos());
    }

    @Test
    public void buildAction_shouldBuildNormalMoveAction() {
        Position destination = new Position(3, 2);

        CharacterActionBuilder builder = createBuilder();
        builder.addResult(createResult(TargetCategory.MovementDestination, destination));

        InteractionFeedback feedback = resolver.getNextFeedback(builder);

        assertNotNull(feedback);

        builder.addFeedback(feedback);

        CharacterAction action = resolver.buildAction(builder);

        assertEquals(acrobat, action.getSrcCharacter());
        assertEquals(1, action.getMotions().size());
        assertEquals(CharacterMotionType.Move, action.getMotions().get(0).getMotionType());
    }

    @Test
    public void getNextInteraction_shouldExposeFirstAndSecondJumpDestinations() {
        Position firstJumpDestination = new Position(3, 1);

        Position secondJumpDestination = new Position(1, 1);
        placeCharacter(new Position(3, 2));

        // Additional character allowing the Acrobat to jump again from (3,1) in another direction.
        placeCharacter(new Position(2, 1));

        InteractionRequest request = resolver.getNextInteraction(createBuilder());

        assertNotNull(request);
        assertTrue(containsTarget(request, TargetCategory.ActiveAbilityDestination, firstJumpDestination));
        assertTrue(containsTarget(request, TargetCategory.ActiveAbilityDestination, secondJumpDestination));
    }

    @Test
    public void getNextInteraction_shouldNotExposeDuplicateAcrobatDestinations() {
        // Arrange a board configuration where two different jump paths
        // lead to the same final destination.
        Position firstJumpDestination = new Position(3, 1);

        Position secondJumpDestination = new Position(1, 1);
        placeCharacter(new Position(3, 2));
        placeCharacter(new Position(4, 2));

        // Additional characters allowing the Acrobat to jump again to (1, 1).
        placeCharacter(new Position(2, 1));
        placeCharacter(new Position(4, 1));

        InteractionRequest request = resolver.getNextInteraction(createBuilder());

        // The resolver must expose that position only once.
        assertNotNull(request);

        int secondJumpDestinationCount = 0;
        for (InteractionTarget target : request.getLegalTargets()) {
            if (target.getCategory() == TargetCategory.ActiveAbilityDestination &&
                    firstJumpDestination.equals(target.getChosenPosition())) {
                secondJumpDestinationCount++;
            }
        }
        assertEquals(1, secondJumpDestinationCount);
    }

    @Test
    public void getNextInteraction_shouldNotExposeOppositeSecondJump() {
        /*
         * A second jump must not immediately return in the opposite
         * direction of the first jump.
         */
        Position firstJumpDestination = new Position(3, 1);

        placeCharacter(new Position(3, 2));

        InteractionRequest request = resolver.getNextInteraction(createBuilder());

        assertNotNull(request);
        assertTrue(containsTarget(request, TargetCategory.ActiveAbilityDestination, firstJumpDestination));
        assertFalse(containsTarget(request, TargetCategory.ActiveAbilityDestination, acrobatPosition));
    }

    @Test
    public void getNextInteraction_shouldFilterInvalidJumpDestinations() {
        // Leader is at (0, 0). The Acrobat should not be allowed to go to (1, 1) which would encircle them
        placeCharacter(new Position(0, 1));
        placeCharacter(new Position(1, 0));

        Position forbiddenDestination = new Position(1, 1);

        placeCharacter(new Position(2, 2));

        // Additional characters allowing the Acrobat to jump again from (3,1) to (1,1).
        placeCharacter(new Position(3, 2));
        placeCharacter(new Position(2, 1));

        InteractionRequest request = resolver.getNextInteraction(createBuilder());

        assertNotNull(request);
        assertFalse(containsTarget(request, TargetCategory.ActiveAbilityDestination, forbiddenDestination));
    }

    @Test
    public void getNextInteraction_shouldNotExposeAbilityWhenMuted() {
        Character jailer = Character.create(CharacterType.Jailer, TeamColor.White);
        game.getBoard().getCell(new Position(2, 3)).setCharacter(jailer);

        InteractionRequest request = resolver.getNextInteraction(createBuilder());

        assertNotNull(request);
        assertTrue(containsTarget(request, TargetCategory.MovementDestination, new Position(2, 2)));
        assertFalse(containsTarget(
                request,
                TargetCategory.ActiveAbilityDestination,
                new Position(3, 1)
        ));
    }
}