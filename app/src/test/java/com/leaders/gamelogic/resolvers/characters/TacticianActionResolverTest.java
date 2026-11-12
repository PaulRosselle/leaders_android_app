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
import com.leaders.gamelogic.queries.BoardQuery;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;

public class TacticianActionResolverTest {

    private static final Position TACTICIAN_POSITION = new Position(3, 3);
    private static final Position ENEMY_LEADER_POSITION = new Position(0, 0);
    private static final Position ALLY_LEADER_POSITION = new Position(6, 3);
    private static final Position ALLY_POSITION = new Position(3, 5);
    private static final Position ALLY_DESTINATION = new Position(3, 6);

    private Game game;
    private Character tactician;
    private TacticianActionResolver resolver;

    private Game createTestGame() {
        return new Game(new Board(),
                new ArrayList<>(), // recruitableCards
                new EnumMap<>(TeamColor.class), // playerRecruitedCharacters
                new EnumMap<>(TeamColor.class), // playerBanishedCards
                new EnumMap<>(TeamColor.class), // playerWarnings
                new HashMap<>() // charactersTransformedState
        );
    }

    private GameHistory createTestGameHistory() {
        ArrayList<Player> players = new ArrayList<>();
        players.add(new Player(TeamColor.Black, "Paul"));
        players.add(new Player(TeamColor.White, "Elise"));

        GameHistory history = new GameHistory(
                new GameConfig(players, players.get(1), GameMode.Discovery, new ArrayList<>(), new ArrayList<>()),
                new ArrayList<>()
        );

        Turn turn = new Turn(TeamColor.White);
        history.getEntries().add(turn);

        turn.getSubPhase(GamePhaseType.TurnStart).start();
        turn.getSubPhase(GamePhaseType.TurnStart).end();
        turn.getSubPhase(GamePhaseType.Actions).start();

        return history;
    }

    private CharacterActionBuilder createBuilder() {
        return new CharacterActionBuilder(tactician);
    }

    @NonNull
    private Character placeCharacter(@NonNull CharacterType type,
                                     @NonNull TeamColor teamColor,
                                     @NonNull Position position) {
        Character character = Character.create(type, teamColor);
        game.getBoard().getCell(position).setCharacter(character);
        return character;
    }

    private void occupyAdjacentCells(@NonNull Position position) {
        for (Direction direction : Direction.values()) {

            Cell cell = BoardQuery.findAdjacentCell(game.getBoard(), position, direction);

            if (cell != null && cell.getCharacter() == null) {
                placeCharacter(CharacterType.Hermit, TeamColor.Black, cell.getPosition());
            }
        }
    }

    @NonNull
    private InteractionResult createPositionResult(@NonNull TargetCategory category,
                                                   @NonNull Position position) {
        return new InteractionResult(
                InteractionResultType.PositionChosen,
                new InteractionContext(tactician),
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

    @Before
    public void setUp() {
        game = createTestGame();

        Character leader = Character.create(CharacterType.LeaderKing, TeamColor.Black);
        game.getBoard().getCell(ALLY_LEADER_POSITION).setCharacter(leader);

        Character enemyLeader = Character.create(CharacterType.LeaderKing, TeamColor.White);
        game.getBoard().getCell(ENEMY_LEADER_POSITION).setCharacter(enemyLeader);

        tactician = Character.create(CharacterType.Tactician, TeamColor.Black);
        game.getBoard().getCell(TACTICIAN_POSITION).setCharacter(tactician);

        resolver = new TacticianActionResolver(game, createTestGameHistory(), tactician);
    }

    /**
     * Places an ally adjacent to the enemy leader, enabling the ability.
     */
    private void enableAbility() {
        Position adjacentToLeader = new Position(0, 1);
        placeCharacter(CharacterType.Hermit, TeamColor.Black, adjacentToLeader);
    }

    @Test
    public void getNextInteraction_shouldRequestNormalMovementWithoutAbilityWhenNoAllyIsAdjacentToEnemyLeader() {
        InteractionRequest request = resolver.getNextInteraction(createBuilder());

        assertNotNull(request);
        assertEquals(InteractionType.PositionExpected, request.getRequestType());
        assertTrue(containsTarget(request, TargetCategory.MovementDestination, new Position(3, 2)));
        assertFalse(containsTarget(request, TargetCategory.ActiveAbilityTargetPosition, TACTICIAN_POSITION));
    }

    @Test
    public void getNextInteraction_shouldExposeOtherNonLeaderAlliesAsTargets() {
        enableAbility();

        Position allyPosition = ALLY_POSITION;
        placeCharacter(CharacterType.Hermit, TeamColor.Black, allyPosition);

        InteractionRequest request = resolver.getNextInteraction(createBuilder());

        assertNotNull(request);
        assertFalse(containsTarget(request, TargetCategory.ActiveAbilityTargetPosition, TACTICIAN_POSITION));
        assertFalse(containsTarget(request, TargetCategory.ActiveAbilityTargetPosition, ALLY_LEADER_POSITION));
        assertTrue(containsTarget(request, TargetCategory.ActiveAbilityTargetPosition, allyPosition));
    }

    @Test
    public void getNextInteraction_shouldNotExposeEnemyCharactersAsTargets() {
        enableAbility();

        Position enemyPosition = new Position(3, 5);
        placeCharacter(CharacterType.Hermit, TeamColor.White, enemyPosition);

        InteractionRequest request = resolver.getNextInteraction(createBuilder());

        assertNotNull(request);
        assertFalse(containsTarget(request, TargetCategory.ActiveAbilityTargetPosition, enemyPosition));
    }

    @Test
    public void getNextInteraction_shouldNotExposeTargetWithoutValidDestination() {
        enableAbility();

        Position allyPosition = ALLY_POSITION;
        placeCharacter(CharacterType.Hermit, TeamColor.Black, allyPosition);

        occupyAdjacentCells(allyPosition);

        InteractionRequest request = resolver.getNextInteraction(createBuilder());

        assertNotNull(request);
        assertFalse(containsTarget(request, TargetCategory.ActiveAbilityTargetPosition, allyPosition));
    }

    @Test
    public void getNextInteraction_shouldRequestDestinationAfterTargetSelected() {
        enableAbility();

        placeCharacter(CharacterType.Hermit, TeamColor.Black, ALLY_POSITION);

        CharacterActionBuilder builder = createBuilder();
        builder.addResult(createPositionResult(TargetCategory.ActiveAbilityTargetPosition, ALLY_POSITION));

        InteractionRequest request = resolver.getNextInteraction(builder);

        assertNotNull(request);
        assertEquals(InteractionType.PositionExpected, request.getRequestType());
        assertTrue(containsTarget(request, TargetCategory.ActiveAbilityDestination, ALLY_DESTINATION));
    }

    @Test
    public void getNextInteraction_shouldNotExposeOccupiedDestination() {
        enableAbility();

        placeCharacter(CharacterType.Hermit, TeamColor.Black, ALLY_POSITION);

        placeCharacter(CharacterType.Rider, TeamColor.White, ALLY_DESTINATION);

        CharacterActionBuilder builder = createBuilder();
        builder.addResult(createPositionResult(TargetCategory.ActiveAbilityTargetPosition, ALLY_POSITION));

        InteractionRequest request = resolver.getNextInteraction(builder);

        assertNotNull(request);

        assertFalse(containsTarget(request, TargetCategory.ActiveAbilityDestination, ALLY_DESTINATION));
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
        assertEquals(tactician, motion.getTargets().get(0).getCharacter());
        assertEquals(TACTICIAN_POSITION, motion.getTargets().get(0).getOriginPos());
        assertEquals(destination, motion.getTargets().get(0).getDestPos());
    }

    @Test
    public void getNextFeedback_shouldReturnNullBeforeDestinationIsSelected() {
        enableAbility();

        placeCharacter(CharacterType.Hermit, TeamColor.Black, ALLY_POSITION);

        CharacterActionBuilder builder = createBuilder();
        builder.addResult(createPositionResult(TargetCategory.ActiveAbilityTargetPosition, ALLY_POSITION));

        assertNull(resolver.getNextFeedback(builder));
    }

    @Test
    public void getNextFeedback_shouldCreateMoveFeedbackForTarget() {
        enableAbility();

        Character ally = placeCharacter(CharacterType.Hermit, TeamColor.Black, ALLY_POSITION);

        CharacterActionBuilder builder = createBuilder();

        builder.addResult(createPositionResult(TargetCategory.ActiveAbilityTargetPosition, ALLY_POSITION));

        builder.addResult(createPositionResult(TargetCategory.ActiveAbilityDestination, ALLY_DESTINATION));

        InteractionFeedback feedback = resolver.getNextFeedback(builder);

        assertNotNull(feedback);

        List<CharacterActionMotion> motions = feedback.getCharacterActionMotions();

        assertEquals(1, motions.size());

        CharacterActionMotion motion = motions.get(0);

        assertEquals(CharacterMotionType.Move, motion.getMotionType());
        assertEquals(1, motion.getTargets().size());
        assertEquals(ally, motion.getTargets().get(0).getCharacter());
        assertEquals(ALLY_POSITION, motion.getTargets().get(0).getOriginPos());
        assertEquals(ALLY_DESTINATION, motion.getTargets().get(0).getDestPos());
    }

    @Test
    public void getNextFeedback_shouldReturnNullAfterFeedbackWasGenerated() {
        enableAbility();

        placeCharacter(CharacterType.Hermit, TeamColor.Black, ALLY_POSITION);

        CharacterActionBuilder builder = createBuilder();

        builder.addResult(createPositionResult(TargetCategory.ActiveAbilityTargetPosition, ALLY_POSITION));
        builder.addResult(createPositionResult(TargetCategory.ActiveAbilityDestination, ALLY_DESTINATION));

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

        assertEquals(tactician, action.getSrcCharacter());
        assertEquals(1, action.getMotions().size());
        assertEquals(CharacterMotionType.Move, action.getMotions().get(0).getMotionType());
    }

    @Test
    public void buildAction_shouldBuildTacticianAbilityAction() {
        enableAbility();

        Character ally = placeCharacter(CharacterType.Hermit, TeamColor.Black, ALLY_POSITION);

        CharacterActionBuilder builder = createBuilder();

        builder.addResult(createPositionResult(TargetCategory.ActiveAbilityTargetPosition, ALLY_POSITION));
        builder.addResult(createPositionResult(TargetCategory.ActiveAbilityDestination, ALLY_DESTINATION));

        InteractionFeedback feedback = resolver.getNextFeedback(builder);

        assertNotNull(feedback);

        builder.addFeedback(feedback);

        CharacterAction action = resolver.buildAction(builder);

        assertEquals(tactician, action.getSrcCharacter());
        assertEquals(1, action.getMotions().size());

        CharacterActionMotion motion = action.getMotions().get(0);

        assertEquals(CharacterMotionType.Move, motion.getMotionType());
        assertEquals(1, motion.getTargets().size());
        assertEquals(ally, motion.getTargets().get(0).getCharacter());
        assertEquals(ALLY_POSITION, motion.getTargets().get(0).getOriginPos());
        assertEquals(ALLY_DESTINATION, motion.getTargets().get(0).getDestPos());
    }

    @Test
    public void getNextInteraction_shouldNotExposeAbilityWhenMuted() {
        enableAbility();

        placeCharacter(CharacterType.Jailer, TeamColor.White, new Position(2, 3));

        InteractionRequest request = resolver.getNextInteraction(createBuilder());

        assertNotNull(request);

        assertFalse(containsTarget(
                request,
                TargetCategory.ActiveAbilityTargetPosition,
                TACTICIAN_POSITION
        ));
    }
}