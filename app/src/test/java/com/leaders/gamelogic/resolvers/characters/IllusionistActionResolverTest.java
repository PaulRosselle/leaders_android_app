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

public class IllusionistActionResolverTest {

    private static final Position ILLUSIONIST_POSITION = new Position(3, 3);
    private static final Position NORMAL_DESTINATION = new Position(3, 2);
    private static final Position TARGET_POSITION = new Position(3, 5);

    private Game game;
    private Character illusionist;
    private Character target;
    private IllusionistActionResolver resolver;

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
        return new CharacterActionBuilder(illusionist);
    }

    @Before
    public void setUp() {
        game = createTestGame();

        Character leader = Character.create(CharacterType.LeaderKing, TeamColor.Black);
        game.getBoard().getCell(new Position(0, 0)).setCharacter(leader);

        illusionist = Character.create(CharacterType.Illusionist, TeamColor.Black);
        game.getBoard().getCell(ILLUSIONIST_POSITION).setCharacter(illusionist);

        target = Character.create(CharacterType.Hermit, TeamColor.White);
        game.getBoard().getCell(TARGET_POSITION).setCharacter(target);

        resolver = new IllusionistActionResolver(game, createTestGameHistory(), illusionist);
    }

    @Test
    public void getNextInteraction_shouldRequestNormalMovementAndIllusionistTargets() {
        InteractionRequest request = resolver.getNextInteraction(createBuilder());

        assertNotNull(request);
        assertEquals(InteractionType.PositionExpected, request.getRequestType());
        assertTrue(request.getLegalResults().contains(InteractionResultType.PositionChosen));
        assertTrue(request.getLegalResults().contains(InteractionResultType.CancelAction));

        assertTrue(containsTarget(request, TargetCategory.MovementDestination, NORMAL_DESTINATION));
        assertTrue(containsTarget(request, TargetCategory.ActiveAbilityTargetPosition, TARGET_POSITION));
    }

    @Test
    public void getNextInteraction_shouldNotExposeAdjacentCharacterAsTarget() {
        Position adjacentPosition = new Position(3, 4);

        game.getBoard().getCell(TARGET_POSITION).setCharacter(null);
        game.getBoard().getCell(adjacentPosition).setCharacter(target);

        InteractionRequest request = resolver.getNextInteraction(createBuilder());

        assertNotNull(request);
        assertFalse(containsTarget(request, TargetCategory.ActiveAbilityTargetPosition, adjacentPosition));
    }

    @Test
    public void getNextInteraction_shouldNotExposeCharacterBehindBlockingCharacter() {
        Position blockingPosition = new Position(3, 4);

        Character blockingCharacter = Character.create(CharacterType.Hermit, TeamColor.White);
        game.getBoard().getCell(blockingPosition).setCharacter(blockingCharacter);

        InteractionRequest request = resolver.getNextInteraction(createBuilder());

        assertNotNull(request);
        assertFalse(containsTarget(request, TargetCategory.ActiveAbilityTargetPosition, TARGET_POSITION));
    }

    @Test
    public void getNextInteraction_shouldReturnNullAfterPositionChosen() {
        CharacterActionBuilder builder = createBuilder();
        builder.addResult(createPositionResult(
                TargetCategory.ActiveAbilityTargetPosition, TARGET_POSITION));

        assertNull(resolver.getNextInteraction(builder));
    }

    @Test
    public void getNextFeedback_shouldCreateNormalMoveFeedback() {
        CharacterActionBuilder builder = createBuilder();
        builder.addResult(createPositionResult(
                TargetCategory.MovementDestination, NORMAL_DESTINATION));

        InteractionFeedback feedback = resolver.getNextFeedback(builder);

        assertNotNull(feedback);

        List<CharacterActionMotion> motions = feedback.getCharacterActionMotions();

        assertEquals(1, motions.size());
        CharacterActionMotion motion = motions.get(0);

        assertEquals(CharacterMotionType.Move, motion.getMotionType());
        assertEquals(1, motion.getTargets().size());
        assertEquals(illusionist, motion.getTargets().get(0).getCharacter());
        assertEquals(ILLUSIONIST_POSITION, motion.getTargets().get(0).getOriginPos());
        assertEquals(NORMAL_DESTINATION, motion.getTargets().get(0).getDestPos());
    }

    @Test
    public void getNextFeedback_shouldCreateSwapFeedback() {
        CharacterActionBuilder builder = createBuilder();
        builder.addResult(createPositionResult(
                TargetCategory.ActiveAbilityTargetPosition, TARGET_POSITION));

        InteractionFeedback feedback = resolver.getNextFeedback(builder);

        assertNotNull(feedback);

        List<CharacterActionMotion> motions = feedback.getCharacterActionMotions();

        assertEquals(1, motions.size());
        CharacterActionMotion motion = motions.get(0);

        assertEquals(CharacterMotionType.Swap, motion.getMotionType());
        assertEquals(2, motion.getTargets().size());

        assertEquals(illusionist, motion.getTargets().get(0).getCharacter());
        assertEquals(ILLUSIONIST_POSITION, motion.getTargets().get(0).getOriginPos());
        assertEquals(TARGET_POSITION, motion.getTargets().get(0).getDestPos());

        assertEquals(target, motion.getTargets().get(1).getCharacter());
        assertEquals(TARGET_POSITION, motion.getTargets().get(1).getOriginPos());
        assertEquals(ILLUSIONIST_POSITION, motion.getTargets().get(1).getDestPos());
    }

    @Test
    public void getNextFeedback_shouldReturnNullAfterFeedbackWasGenerated() {
        CharacterActionBuilder builder = createBuilder();
        builder.addResult(createPositionResult(
                TargetCategory.ActiveAbilityTargetPosition, TARGET_POSITION));

        InteractionFeedback feedback = resolver.getNextFeedback(builder);

        assertNotNull(feedback);

        builder.addFeedback(feedback);

        assertNull(resolver.getNextFeedback(builder));
    }

    @Test
    public void buildAction_shouldBuildNormalMoveAction() {
        CharacterActionBuilder builder = createBuilder();
        builder.addResult(createPositionResult(
                TargetCategory.MovementDestination, NORMAL_DESTINATION));

        InteractionFeedback feedback = resolver.getNextFeedback(builder);

        assertNotNull(feedback);
        builder.addFeedback(feedback);

        CharacterAction action = resolver.buildAction(builder);

        assertEquals(illusionist, action.getSrcCharacter());
        assertEquals(1, action.getMotions().size());

        CharacterActionMotion motion = action.getMotions().get(0);

        assertEquals(CharacterMotionType.Move, motion.getMotionType());
        assertEquals(1, motion.getTargets().size());
        assertEquals(illusionist, motion.getTargets().get(0).getCharacter());
        assertEquals(ILLUSIONIST_POSITION, motion.getTargets().get(0).getOriginPos());
        assertEquals(NORMAL_DESTINATION, motion.getTargets().get(0).getDestPos());
    }

    @Test
    public void buildAction_shouldBuildSwapAction() {
        CharacterActionBuilder builder = createBuilder();
        builder.addResult(createPositionResult(
                TargetCategory.ActiveAbilityTargetPosition, TARGET_POSITION));

        InteractionFeedback feedback = resolver.getNextFeedback(builder);

        assertNotNull(feedback);
        builder.addFeedback(feedback);

        CharacterAction action = resolver.buildAction(builder);

        assertEquals(illusionist, action.getSrcCharacter());
        assertEquals(1, action.getMotions().size());

        CharacterActionMotion motion = action.getMotions().get(0);

        assertEquals(CharacterMotionType.Swap, motion.getMotionType());
        assertEquals(2, motion.getTargets().size());

        assertEquals(illusionist, motion.getTargets().get(0).getCharacter());
        assertEquals(ILLUSIONIST_POSITION, motion.getTargets().get(0).getOriginPos());
        assertEquals(TARGET_POSITION, motion.getTargets().get(0).getDestPos());

        assertEquals(target, motion.getTargets().get(1).getCharacter());
        assertEquals(TARGET_POSITION, motion.getTargets().get(1).getOriginPos());
        assertEquals(ILLUSIONIST_POSITION, motion.getTargets().get(1).getDestPos());
    }

    @Test
    public void getNextInteraction_shouldNotExposeInvalidSwapTarget() {
        Position targetPosition = new Position(3, 5);
        Character target = Character.create(CharacterType.Hermit, TeamColor.White);
        game.getBoard().getCell(targetPosition).setCharacter(target);

        InteractionRequest request = resolver.getNextInteraction(createBuilder());

        assertNotNull(request);
        assertTrue(containsTarget(
                request, TargetCategory.ActiveAbilityTargetPosition, targetPosition));
    }

    @NonNull
    private InteractionResult createPositionResult(@NonNull TargetCategory category,
                                                   @NonNull Position position) {
        return new InteractionResult(
                InteractionResultType.PositionChosen,
                new InteractionContext(illusionist),
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

    @Test
    public void etNextInteraction_shouldRespectPassiveAbilities() {
        Character jailer = Character.create(CharacterType.Jailer, TeamColor.White);
        game.getBoard().getCell(new Position(2, 3)).setCharacter(jailer);

        Character protector = Character.create(CharacterType.Protector, TeamColor.White);
        game.getBoard().getCell(new Position(3, 6)).setCharacter(protector);

        InteractionRequest request = resolver.getNextInteraction(createBuilder());

        assertNotNull(request);
        assertFalse(containsTarget(
                request,
                TargetCategory.ActiveAbilityTargetPosition,
                TARGET_POSITION
        ));
    }
}