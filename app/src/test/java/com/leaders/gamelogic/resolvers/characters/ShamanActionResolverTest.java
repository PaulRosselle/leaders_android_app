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

public class ShamanActionResolverTest {

    private static final Position SHAMAN_POSITION = new Position(3, 3);
    private static final Position NORMAL_DESTINATION = new Position(3, 2);
    private static final Position TARGET_POSITION = new Position(3, 5);

    private Game game;
    private Character shaman;
    private Character target;
    private ShamanActionResolver resolver;

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
        return new CharacterActionBuilder(shaman);
    }

    @Before
    public void setUp() {
        game = createTestGame();

        Character leader = Character.create(CharacterType.LeaderKing, TeamColor.Black);
        game.getBoard().getCell(new Position(0, 0)).setCharacter(leader);

        shaman = Character.create(CharacterType.Shaman, TeamColor.Black);
        game.getBoard().getCell(SHAMAN_POSITION).setCharacter(shaman);

        target = Character.create(CharacterType.Hermit, TeamColor.White);
        game.getBoard().getCell(TARGET_POSITION).setCharacter(target);

        resolver = new ShamanActionResolver(game, createTestGameHistory(), shaman);
    }

    // ---------------------------------------------------------------------
    // getNextInteraction
    // ---------------------------------------------------------------------

    @Test
    public void getNextInteraction_shouldRequestNormalMovementAndShamanTargets() {
        InteractionRequest request = resolver.getNextInteraction(createBuilder());

        assertNotNull(request);
        assertEquals(InteractionType.PositionExpected, request.getRequestType());
        assertTrue(request.getLegalResults().contains(InteractionResultType.PositionChosen));
        assertTrue(request.getLegalResults().contains(InteractionResultType.CancelAction));

        assertTrue(containsTarget(request, TargetCategory.MovementDestination, NORMAL_DESTINATION));
        assertTrue(containsTarget(request, TargetCategory.ActiveAbilityTargetPosition, TARGET_POSITION));
    }

    @Test
    public void getNextInteraction_shouldAllowTargetingAllyCharacter() {
        Character ally = Character.create(CharacterType.Hermit, TeamColor.Black);
        game.getBoard().getCell(TARGET_POSITION).setCharacter(ally);

        InteractionRequest request = resolver.getNextInteraction(createBuilder());

        assertNotNull(request);
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
    public void getNextInteraction_shouldNotExposeLeaderAsTarget() {
        Character enemyLeader = Character.create(CharacterType.LeaderKing, TeamColor.White);
        game.getBoard().getCell(TARGET_POSITION).setCharacter(enemyLeader);

        InteractionRequest request = resolver.getNextInteraction(createBuilder());

        assertNotNull(request);
        assertFalse(containsTarget(request, TargetCategory.ActiveAbilityTargetPosition, TARGET_POSITION));
    }

    @Test
    public void getNextInteraction_shouldNotExposeTargetsOutsideStraightLine() {
        // (4, 5) is not aligned with the Shaman at (3, 3)
        Position notAlignedPosition = new Position(4, 5);

        game.getBoard().getCell(TARGET_POSITION).setCharacter(null);
        game.getBoard().getCell(notAlignedPosition).setCharacter(target);

        InteractionRequest request = resolver.getNextInteraction(createBuilder());

        assertNotNull(request);
        assertFalse(containsTarget(request, TargetCategory.ActiveAbilityTargetPosition, notAlignedPosition));
    }

    @Test
    public void getNextInteraction_shouldReturnNullAfterPositionChosen() {
        CharacterActionBuilder builder = createBuilder();
        builder.addResult(createPositionResult(
                TargetCategory.ActiveAbilityTargetPosition, TARGET_POSITION));

        assertNull(resolver.getNextInteraction(builder));
    }

    @Test
    public void getNextInteraction_shouldRespectPassiveAbilities() {
        // A Jailer adjacent to the Shaman prevents him from using his active ability
        Character jailer = Character.create(CharacterType.Jailer, TeamColor.White);
        game.getBoard().getCell(new Position(2, 3)).setCharacter(jailer);

        InteractionRequest request = resolver.getNextInteraction(createBuilder());

        assertNotNull(request);
        assertFalse(containsTarget(request, TargetCategory.ActiveAbilityTargetPosition, TARGET_POSITION));
    }

    // ---------------------------------------------------------------------
    // getNextFeedback
    // ---------------------------------------------------------------------

    @Test
    public void getNextFeedback_shouldReturnNullWithoutResult() {
        assertNull(resolver.getNextFeedback(createBuilder()));
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
        assertEquals(shaman, motion.getTargets().get(0).getCharacter());
        assertEquals(SHAMAN_POSITION, motion.getTargets().get(0).getOriginPos());
        assertEquals(NORMAL_DESTINATION, motion.getTargets().get(0).getDestPos());
    }

    @Test
    public void getNextFeedback_shouldCreateTransformFeedback() {
        CharacterActionBuilder builder = createBuilder();
        builder.addResult(createPositionResult(
                TargetCategory.ActiveAbilityTargetPosition, TARGET_POSITION));

        InteractionFeedback feedback = resolver.getNextFeedback(builder);

        assertNotNull(feedback);

        List<CharacterActionMotion> motions = feedback.getCharacterActionMotions();

        // No frog exists yet, so there is only the transformation motion
        assertEquals(1, motions.size());
        CharacterActionMotion motion = motions.get(0);

        assertEquals(CharacterMotionType.Transform, motion.getMotionType());
        assertEquals(2, motion.getTargets().size());

        // Character before transformation
        assertEquals(target, motion.getTargets().get(0).getCharacter());
        assertEquals(TARGET_POSITION, motion.getTargets().get(0).getOriginPos());
        assertNull(motion.getTargets().get(0).getDestPos());

        // Character after transformation
        Character transformed = motion.getTargets().get(1).getCharacter();
        assertEquals(CharacterType.Frog, transformed.getCharacterType());
        assertEquals(target.getTeamColor(), transformed.getTeamColor());
        assertNull(motion.getTargets().get(1).getOriginPos());
        assertEquals(TARGET_POSITION, motion.getTargets().get(1).getDestPos());
    }

    @Test
    public void getNextFeedback_shouldNotModifyBoardWhenBuildingFeedback() {
        CharacterActionBuilder builder = createBuilder();
        builder.addResult(createPositionResult(
                TargetCategory.ActiveAbilityTargetPosition, TARGET_POSITION));

        assertNotNull(resolver.getNextFeedback(builder));

        assertEquals(target, game.getBoard().getCell(TARGET_POSITION).getCharacter());
        assertEquals(shaman, game.getBoard().getCell(SHAMAN_POSITION).getCharacter());
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
    public void getNextFeedback_shouldReturnNullAfterCancelAction() {
        CharacterActionBuilder builder = createBuilder();

        builder.addResult(new InteractionResult(
                InteractionResultType.CancelAction,
                new InteractionContext(shaman),
                null
        ));

        assertNull(resolver.getNextFeedback(builder));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getNextFeedback_shouldThrowForInvalidTargetCategory() {
        CharacterActionBuilder builder = createBuilder();
        builder.addResult(createPositionResult(
                TargetCategory.ActiveAbilityDestination, TARGET_POSITION));

        resolver.getNextFeedback(builder);
    }

    // ---------------------------------------------------------------------
    // buildAction
    // ---------------------------------------------------------------------

    @Test
    public void buildAction_shouldBuildNormalMoveAction() {
        CharacterActionBuilder builder = createBuilder();
        builder.addResult(createPositionResult(
                TargetCategory.MovementDestination, NORMAL_DESTINATION));

        InteractionFeedback feedback = resolver.getNextFeedback(builder);

        assertNotNull(feedback);
        builder.addFeedback(feedback);

        CharacterAction action = resolver.buildAction(builder);

        assertEquals(shaman, action.getSrcCharacter());
        assertEquals(1, action.getMotions().size());

        CharacterActionMotion motion = action.getMotions().get(0);

        assertEquals(CharacterMotionType.Move, motion.getMotionType());
        assertEquals(1, motion.getTargets().size());
        assertEquals(shaman, motion.getTargets().get(0).getCharacter());
        assertEquals(SHAMAN_POSITION, motion.getTargets().get(0).getOriginPos());
        assertEquals(NORMAL_DESTINATION, motion.getTargets().get(0).getDestPos());
    }

    @Test
    public void buildAction_shouldBuildTransformAction() {
        CharacterActionBuilder builder = createBuilder();
        builder.addResult(createPositionResult(
                TargetCategory.ActiveAbilityTargetPosition, TARGET_POSITION));

        InteractionFeedback feedback = resolver.getNextFeedback(builder);

        assertNotNull(feedback);
        builder.addFeedback(feedback);

        CharacterAction action = resolver.buildAction(builder);

        assertEquals(shaman, action.getSrcCharacter());
        assertEquals(1, action.getMotions().size());

        CharacterActionMotion motion = action.getMotions().get(0);

        assertEquals(CharacterMotionType.Transform, motion.getMotionType());
        assertEquals(2, motion.getTargets().size());

        assertEquals(target, motion.getTargets().get(0).getCharacter());
        assertEquals(TARGET_POSITION, motion.getTargets().get(0).getOriginPos());

        assertEquals(CharacterType.Frog, motion.getTargets().get(1).getCharacter().getCharacterType());
        assertEquals(TARGET_POSITION, motion.getTargets().get(1).getDestPos());
    }

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------

    @NonNull
    private InteractionResult createPositionResult(@NonNull TargetCategory category,
                                                   @NonNull Position position) {
        return new InteractionResult(
                InteractionResultType.PositionChosen,
                new InteractionContext(shaman),
                new InteractionTarget(category, position)
        );
    }

    private boolean containsTarget(@NonNull InteractionRequest request,
                                   @NonNull TargetCategory category,
                                   @NonNull Position position) {
        for (InteractionTarget legalTarget : request.getLegalTargets()) {
            if (legalTarget.getCategory() == category &&
                    position.equals(legalTarget.getChosenPosition())) {
                return true;
            }
        }

        return false;
    }
}