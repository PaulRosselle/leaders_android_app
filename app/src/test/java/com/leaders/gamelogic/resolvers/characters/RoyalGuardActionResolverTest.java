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

public class RoyalGuardActionResolverTest {

    private Game game;
    private Character character;
    private RoyalGuardActionResolver resolver;

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
    private InteractionResult createPositionResult(@NonNull TargetCategory category,
                                                   @NonNull Position position) {
        return new InteractionResult(
                InteractionResultType.PositionChosen,
                new InteractionContext(character),
                new InteractionTarget(category, position)
        );
    }

    @Before
    public void setUp() {
        game = createTestGame();
        GameHistory gameHistory = createTestGameHistory();

        Character leader = Character.create(CharacterType.LeaderKing, TeamColor.Black);
        game.getBoard().getCell(new Position(0, 0)).setCharacter(leader);

        character = Character.create(CharacterType.RoyalGuard, TeamColor.Black);
        game.getBoard().getCell(new Position(3, 3)).setCharacter(character);

        resolver = new RoyalGuardActionResolver(game, gameHistory, character);
    }

    @Test
    public void getNextInteraction_shouldRequestMovementAndAbilityDestinations() {
        CharacterActionBuilder builder = createBuilder();

        InteractionRequest request = resolver.getNextInteraction(builder);

        assertNotNull(request);
        assertEquals(InteractionType.PositionExpected, request.getRequestType());
        assertTrue(request.getLegalResults().contains(InteractionResultType.PositionChosen));
        assertTrue(request.getLegalResults().contains(InteractionResultType.CancelAction));

        boolean hasMovementDestination = false;
        boolean hasAbilityDestination = false;

        for (InteractionTarget target : request.getLegalTargets()) {
            if (target.getCategory() == TargetCategory.MovementDestination) {
                hasMovementDestination = true;
            } else if (target.getCategory() == TargetCategory.ActiveAbilityDestination) {
                hasAbilityDestination = true;
            }
        }

        assertTrue(hasMovementDestination);
        assertTrue(hasAbilityDestination);
    }

    @Test
    public void getNextInteraction_shouldNotReturnSecondInteraction() {
        CharacterActionBuilder builder = createBuilder();
        builder.addResult(createPositionResult(
                TargetCategory.ActiveAbilityDestination,
                new Position(1, 1)
        ));

        InteractionRequest request = resolver.getNextInteraction(builder);

        assertNull(request);
    }

    @Test
    public void getNextFeedback_shouldUseDefaultMovementForMovementDestination() {
        Position destination = new Position(3, 2);
        CharacterActionBuilder builder = createBuilder();
        builder.addResult(createPositionResult(
                TargetCategory.MovementDestination,
                destination
        ));

        InteractionFeedback feedback = resolver.getNextFeedback(builder);

        assertNotNull(feedback);

        List<CharacterActionMotion> motions = feedback.getCharacterActionMotions();

        assertEquals(1, motions.size());
        CharacterActionMotion motion = motions.get(0);

        assertEquals(CharacterMotionType.Move, motion.getMotionType());
        assertEquals(1, motion.getTargets().size());
        assertEquals(character, motion.getTargets().get(0).getCharacter());
        assertEquals(new Position(3, 3), motion.getTargets().get(0).getOriginPos());
        assertEquals(destination, motion.getTargets().get(0).getDestPos());
    }

    @Test
    public void getNextFeedback_shouldBuildRoyalGuardMovement() {
        Position teleportPos = new Position(1, 0);
        Position destination = new Position(2, 1);
        CharacterActionBuilder builder = createBuilder();
        builder.addResult(createPositionResult(
                TargetCategory.ActiveAbilityDestination,
                destination
        ));

        InteractionFeedback feedback = resolver.getNextFeedback(builder);

        assertNotNull(feedback);

        List<CharacterActionMotion> motions = feedback.getCharacterActionMotions();

        assertEquals(2, motions.size());
        CharacterActionMotion teleportMotion = motions.get(0);

        assertEquals(CharacterMotionType.Teleport, teleportMotion.getMotionType());
        assertEquals(1, teleportMotion.getTargets().size());
        assertEquals(character, teleportMotion.getTargets().get(0).getCharacter());
        assertEquals(new Position(3, 3), teleportMotion.getTargets().get(0).getOriginPos());
        assertEquals(teleportPos, teleportMotion.getTargets().get(0).getDestPos());


        CharacterActionMotion moveMotion = motions.get(1);
        assertEquals(CharacterMotionType.Move, moveMotion.getMotionType());
        assertEquals(1, moveMotion.getTargets().size());
        assertEquals(character, moveMotion.getTargets().get(0).getCharacter());
        assertEquals(teleportPos, moveMotion.getTargets().get(0).getOriginPos());
        assertEquals(destination, moveMotion.getTargets().get(0).getDestPos());
    }

    @Test
    public void getNextFeedback_shouldReturnNullAfterFeedbackWasGenerated() {
        Position destination = new Position(1, 1);
        CharacterActionBuilder builder = createBuilder();
        builder.addResult(createPositionResult(
                TargetCategory.ActiveAbilityDestination,
                destination
        ));

        InteractionFeedback feedback = resolver.getNextFeedback(builder);
        assertNotNull(feedback);

        builder.addFeedback(feedback);

        assertNull(resolver.getNextFeedback(builder));
    }

    @Test
    public void getNextFeedback_shouldReturnNullAfterCancelAction() {
        CharacterActionBuilder builder = createBuilder();
        builder.addResult(new InteractionResult(
                InteractionResultType.CancelAction,
                new InteractionContext(character),
                null
        ));

        assertNull(resolver.getNextFeedback(builder));
    }

    @Test
    public void buildAction_shouldBuildActionForRoyalGuardAbility() {
        Position destination = new Position(0, 1);
        CharacterActionBuilder builder = createBuilder();
        builder.addResult(createPositionResult(
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

        assertEquals(CharacterMotionType.Teleport, motion.getMotionType());
        assertEquals(character, motion.getTargets().get(0).getCharacter());
        assertEquals(new Position(3, 3), motion.getTargets().get(0).getOriginPos());
        assertEquals(destination, motion.getTargets().get(0).getDestPos());
    }

    @Test
    public void buildAction_shouldBuildActionForDefaultMovement() {
        Position destination = new Position(3, 2);
        CharacterActionBuilder builder = createBuilder();
        builder.addResult(createPositionResult(
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
    public void getNextInteraction_shouldNotExposeAbilityWhenMuted() {
        Character jailer = Character.create(CharacterType.Jailer, TeamColor.White);
        game.getBoard().getCell(new Position(2, 3)).setCharacter(jailer);

        InteractionRequest request = resolver.getNextInteraction(createBuilder());

        assertNotNull(request);

        boolean hasAbilityDestination = false;
        for (InteractionTarget target : request.getLegalTargets()) {
            if (target.getCategory() == TargetCategory.ActiveAbilityDestination &&
                    new Position(1, 1).equals(target.getChosenPosition())) {
                hasAbilityDestination = true;
                break;
            }
        }

        assertFalse(hasAbilityDestination);
    }
}