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

public class BruiserActionResolverTest {

    private static final Position BRUISER_POSITION = new Position(3, 3);
    private static final Position TARGET_POSITION = new Position(3, 4);

    private Game game;
    private Character bruiser;
    private Character target;
    private BruiserActionResolver resolver;

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
        return new CharacterActionBuilder(bruiser);
    }

    @Before
    public void setUp() {
        game = createTestGame();

        Character leader = Character.create(CharacterType.LeaderKing, TeamColor.Black);
        game.getBoard().getCell(new Position(0, 0)).setCharacter(leader);

        bruiser = Character.create(CharacterType.Bruiser, TeamColor.Black);
        game.getBoard().getCell(BRUISER_POSITION).setCharacter(bruiser);

        target = Character.create(CharacterType.Hermit, TeamColor.White);
        game.getBoard().getCell(TARGET_POSITION).setCharacter(target);

        resolver = new BruiserActionResolver(game, createTestGameHistory(), bruiser);
    }

    @Test
    public void getNextInteraction_shouldRequestNormalMovementAndBruiserTargets() {
        Position normalDestination = new Position(3, 2);

        InteractionRequest request = resolver.getNextInteraction(createBuilder());

        assertNotNull(request);
        assertEquals(InteractionType.PositionExpected, request.getRequestType());
        assertTrue(request.getLegalResults().contains(InteractionResultType.PositionChosen));
        assertTrue(request.getLegalResults().contains(InteractionResultType.CancelAction));

        assertTrue(containsTarget(request, TargetCategory.MovementDestination, normalDestination));
        assertTrue(containsTarget(request, TargetCategory.ActiveAbilityTargetPosition, TARGET_POSITION));
    }

    @Test
    public void getNextInteraction_shouldNotExposeAdjacentAllyAsBruiserTarget() {
        Position allyPosition = TARGET_POSITION;

        game.getBoard().getCell(TARGET_POSITION).setCharacter(null);
        Character ally = Character.create(CharacterType.Hermit, TeamColor.Black);
        game.getBoard().getCell(allyPosition).setCharacter(ally);

        InteractionRequest request = resolver.getNextInteraction(createBuilder());

        assertNotNull(request);
        assertFalse(containsTarget(request, TargetCategory.ActiveAbilityTargetPosition, allyPosition));
    }

    @Test
    public void getNextInteraction_shouldRequestPushDestinationAfterEnemySelected() {
        CharacterActionBuilder builder = createBuilder();
        builder.addResult(createPositionResult(TargetCategory.ActiveAbilityTargetPosition, TARGET_POSITION));

        InteractionRequest request = resolver.getNextInteraction(builder);

        assertNotNull(request);
        assertEquals(InteractionType.PositionExpected, request.getRequestType());
        assertTrue(request.getLegalResults().contains(InteractionResultType.PositionChosen));
        assertTrue(request.getLegalResults().contains(InteractionResultType.CancelAction));

        assertTrue(containsTarget(request, TargetCategory.ActiveAbilityDestination, new Position(2, 4)));
        assertTrue(containsTarget(request, TargetCategory.ActiveAbilityDestination, new Position(4, 4)));
        assertTrue(containsTarget(request, TargetCategory.ActiveAbilityDestination, new Position(3, 5)));

        assertFalse(containsTarget(request, TargetCategory.ActiveAbilityDestination, BRUISER_POSITION));
    }

    @Test
    public void getNextInteraction_shouldDelegateNormalMovementToParent() {
        Position destination = new Position(3, 2);

        CharacterActionBuilder builder = createBuilder();
        builder.addResult(createPositionResult(TargetCategory.MovementDestination, destination));

        assertNull(resolver.getNextInteraction(builder));
    }

    @Test
    public void getNextFeedback_shouldReturnNullBeforePushDestinationIsSelected() {
        CharacterActionBuilder builder = createBuilder();
        builder.addResult(createPositionResult(TargetCategory.ActiveAbilityTargetPosition, TARGET_POSITION));

        assertNull(resolver.getNextFeedback(builder));
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
        assertEquals(bruiser, motion.getTargets().get(0).getCharacter());
        assertEquals(BRUISER_POSITION, motion.getTargets().get(0).getOriginPos());
        assertEquals(destination, motion.getTargets().get(0).getDestPos());
    }

    @Test
    public void getNextFeedback_shouldCreatePushFeedback() {
        Position destination = new Position(3, 5);

        CharacterActionBuilder builder = createBuilder();
        builder.addResult(createPositionResult(TargetCategory.ActiveAbilityTargetPosition, TARGET_POSITION));
        builder.addResult(createPositionResult(TargetCategory.ActiveAbilityDestination, destination));

        InteractionFeedback feedback = resolver.getNextFeedback(builder);

        assertNotNull(feedback);

        List<CharacterActionMotion> motions = feedback.getCharacterActionMotions();

        assertEquals(1, motions.size());
        CharacterActionMotion motion = motions.get(0);

        assertEquals(CharacterMotionType.Push, motion.getMotionType());
        assertEquals(2, motion.getTargets().size());

        assertEquals(bruiser, motion.getTargets().get(0).getCharacter());
        assertEquals(BRUISER_POSITION, motion.getTargets().get(0).getOriginPos());
        assertEquals(TARGET_POSITION, motion.getTargets().get(0).getDestPos());

        assertEquals(target, motion.getTargets().get(1).getCharacter());
        assertEquals(TARGET_POSITION, motion.getTargets().get(1).getOriginPos());
        assertEquals(destination, motion.getTargets().get(1).getDestPos());
    }

    @Test
    public void getNextFeedback_shouldReturnNullAfterFeedbackWasGenerated() {
        Position destination = new Position(3, 5);

        CharacterActionBuilder builder = createBuilder();
        builder.addResult(createPositionResult(TargetCategory.ActiveAbilityTargetPosition, TARGET_POSITION));
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

        assertEquals(bruiser, action.getSrcCharacter());
        assertEquals(1, action.getMotions().size());
        assertEquals(CharacterMotionType.Move, action.getMotions().get(0).getMotionType());
    }

    @Test
    public void buildAction_shouldBuildPushAction() {
        Position destination = new Position(3, 5);

        CharacterActionBuilder builder = createBuilder();
        builder.addResult(createPositionResult(TargetCategory.ActiveAbilityTargetPosition, TARGET_POSITION));
        builder.addResult(createPositionResult(TargetCategory.ActiveAbilityDestination, destination));

        InteractionFeedback feedback = resolver.getNextFeedback(builder);

        assertNotNull(feedback);

        builder.addFeedback(feedback);

        CharacterAction action = resolver.buildAction(builder);

        assertEquals(bruiser, action.getSrcCharacter());
        assertEquals(1, action.getMotions().size());

        CharacterActionMotion motion = action.getMotions().get(0);

        assertEquals(CharacterMotionType.Push, motion.getMotionType());
        assertEquals(2, motion.getTargets().size());
        assertEquals(bruiser, motion.getTargets().get(0).getCharacter());
        assertEquals(target, motion.getTargets().get(1).getCharacter());
    }

    @Test
    public void getNextInteraction_shouldNotExposeOccupiedPushDestination() {
        Position occupiedDestination = new Position(3, 5);

        Character occupant = Character.create(CharacterType.Hermit, TeamColor.Black);
        game.getBoard().getCell(occupiedDestination).setCharacter(occupant);

        CharacterActionBuilder builder = createBuilder();
        builder.addResult(createPositionResult(TargetCategory.ActiveAbilityTargetPosition, TARGET_POSITION));

        InteractionRequest request = resolver.getNextInteraction(builder);

        assertNotNull(request);
        assertFalse(containsTarget(request, TargetCategory.ActiveAbilityDestination, occupiedDestination));
    }

    @Test
    public void getNextInteraction_shouldRespectPassiveAbilities() {
        Character jailer = Character.create(CharacterType.Jailer, TeamColor.White);
        game.getBoard().getCell(new Position(2, 3)).setCharacter(jailer);

        Character protector = Character.create(CharacterType.Protector, TeamColor.Black);
        game.getBoard().getCell(new Position(3, 5)).setCharacter(protector);

        InteractionRequest request = resolver.getNextInteraction(createBuilder());

        assertNotNull(request);
        assertFalse(containsTarget(
                request,
                TargetCategory.ActiveAbilityTargetPosition,
                TARGET_POSITION
        ));
    }

    @NonNull
    private InteractionResult createPositionResult(@NonNull TargetCategory category,
                                                   @NonNull Position position) {
        return new InteractionResult(InteractionResultType.PositionChosen,
                new InteractionContext(bruiser),
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
}