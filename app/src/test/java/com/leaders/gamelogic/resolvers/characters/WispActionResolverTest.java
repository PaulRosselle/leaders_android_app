
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

public class WispActionResolverTest {

    private static final Position WISP_POSITION = new Position(3, 3);
    private static final Position NORMAL_DESTINATION = new Position(3, 2);
    private static final Position DISTANT_DESTINATION = new Position(1, 1);
    private static final Position OCCUPIED_POSITION = new Position(6, 3);

    private Character wisp;
    private WispActionResolver resolver;

    private Game createTestGame() {
        // Build the minimal Game state required by the tests.
        // This state is intentionally invalid as a real game state.
        return new Game(new Board(),
                new ArrayList<>(), // recruitableCards
                new EnumMap<>(TeamColor.class), // playerRecruitedCharacters
                new EnumMap<>(TeamColor.class), // playerBanishedCards
                new EnumMap<>(TeamColor.class) // playerWarnings
        );
    }

    private GameHistory createTestGameHistory() {
        ArrayList<Player> players = new ArrayList<>();
        players.add(new Player(TeamColor.Black, "Paul"));
        players.add(new Player(TeamColor.White, "Elise"));

        return new GameHistory(
                new GameConfig(players, players.get(1), GameMode.Discovery, new ArrayList<>(), new ArrayList<>()),
                new ArrayList<>()
        );
    }

    private CharacterActionBuilder createBuilder() {
        return new CharacterActionBuilder(wisp);
    }

    @Before
    public void setUp() {
        Game game = createTestGame();

        Character leader = Character.create(CharacterType.LeaderKing, TeamColor.Black);
        game.getBoard().getCell(new Position(0, 0)).setCharacter(leader);

        wisp = Character.create(CharacterType.Wisp, TeamColor.Black);
        game.getBoard().getCell(WISP_POSITION).setCharacter(wisp);

        Character enemy = Character.create(CharacterType.Hermit, TeamColor.White);
        game.getBoard().getCell(OCCUPIED_POSITION).setCharacter(enemy);

        resolver = new WispActionResolver(game, createTestGameHistory(), wisp);
    }

    @Test
    public void getNextInteraction_shouldRequestAllEmptyCellsAsMovementDestinations() {
        InteractionRequest request = resolver.getNextInteraction(createBuilder());

        assertNotNull(request);
        assertEquals(InteractionType.PositionExpected, request.getRequestType());

        assertTrue(request.getLegalResults().contains(InteractionResultType.PositionChosen));
        assertTrue(request.getLegalResults().contains(InteractionResultType.CancelAction));

        assertTrue(containsTarget(request, NORMAL_DESTINATION));
        assertTrue(containsTarget(request, DISTANT_DESTINATION));
    }

    @Test
    public void getNextInteraction_shouldAllowFlyingToDistantCells() {
        InteractionRequest request = resolver.getNextInteraction(createBuilder());

        assertNotNull(request);

        assertTrue(containsTarget(request, DISTANT_DESTINATION));
    }

    @Test
    public void getNextInteraction_shouldNotExposeOccupiedCells() {
        InteractionRequest request = resolver.getNextInteraction(createBuilder());

        assertNotNull(request);

        assertFalse(containsTarget(request, OCCUPIED_POSITION));
    }

    @Test
    public void getNextInteraction_shouldNotExposeWispCurrentPosition() {
        InteractionRequest request = resolver.getNextInteraction(createBuilder());

        assertNotNull(request);

        assertFalse(containsTarget(request, WISP_POSITION));
    }

    @Test
    public void getNextInteraction_shouldReturnNullAfterPositionChosen() {
        CharacterActionBuilder builder = createBuilder();

        builder.addResult(createPositionResult());

        assertNull(resolver.getNextInteraction(builder));
    }

    @Test
    public void getNextFeedback_shouldCreateFlyFeedback() {
        CharacterActionBuilder builder = createBuilder();

        builder.addResult(createPositionResult());

        InteractionFeedback feedback = resolver.getNextFeedback(builder);

        assertNotNull(feedback);

        List<CharacterActionMotion> motions = feedback.getCharacterActionMotions();

        assertEquals(1, motions.size());

        CharacterActionMotion motion = motions.get(0);

        assertEquals(CharacterMotionType.Fly, motion.getMotionType());
        assertEquals(1, motion.getTargets().size());

        assertEquals(wisp, motion.getTargets().get(0).getCharacter());
        assertEquals(WISP_POSITION, motion.getTargets().get(0).getOriginPos());
        assertEquals(DISTANT_DESTINATION, motion.getTargets().get(0).getDestPos());
    }

    @Test
    public void getNextFeedback_shouldReturnNullAfterFeedbackWasGenerated() {
        CharacterActionBuilder builder = createBuilder();

        builder.addResult(createPositionResult());

        InteractionFeedback feedback = resolver.getNextFeedback(builder);

        assertNotNull(feedback);

        builder.addFeedback(feedback);

        assertNull(resolver.getNextFeedback(builder));
    }

    @Test
    public void buildAction_shouldBuildFlyAction() {
        CharacterActionBuilder builder = createBuilder();

        builder.addResult(createPositionResult());

        InteractionFeedback feedback = resolver.getNextFeedback(builder);

        assertNotNull(feedback);

        builder.addFeedback(feedback);

        CharacterAction action = resolver.buildAction(builder);

        assertEquals(wisp, action.getSrcCharacter());
        assertEquals(1, action.getMotions().size());

        CharacterActionMotion motion = action.getMotions().get(0);

        assertEquals(CharacterMotionType.Fly, motion.getMotionType());
        assertEquals(1, motion.getTargets().size());

        assertEquals(wisp, motion.getTargets().get(0).getCharacter());
        assertEquals(WISP_POSITION, motion.getTargets().get(0).getOriginPos());
        assertEquals(DISTANT_DESTINATION, motion.getTargets().get(0).getDestPos());
    }

    @NonNull
    private InteractionResult createPositionResult() {
        return new InteractionResult(
                InteractionResultType.PositionChosen,
                new InteractionContext(wisp),
                new InteractionTarget(TargetCategory.MovementDestination, WispActionResolverTest.DISTANT_DESTINATION)
        );
    }

    private boolean containsTarget(@NonNull InteractionRequest request,
                                   @NonNull Position position) {
        for (InteractionTarget target : request.getLegalTargets()) {
            if (target.getCategory() == TargetCategory.MovementDestination &&
                    position.equals(target.getChosenPosition())) {
                return true;
            }
        }

        return false;
    }
}
