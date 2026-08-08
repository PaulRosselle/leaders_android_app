package com.leaders.gamelogic.resolvers;

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

public class CharacterActionResolverTest {
    private Character character;
    private CharacterActionResolver resolver;

    private Game createTestGame() {
        return new Game(new Board(),
                new ArrayList<>(), // recruitableCards
                new ArrayList<>(), // recruitedCharacters
                new ArrayList<>(), // banishedCards
                new EnumMap<>(TeamColor.class) // playerWarnings
        );
    }

    private GameHistory createTestGameHistory() {
        // Build the minimal GameHistory state required by the tests.
        // This state is intentionally invalid as a real game state.
        ArrayList<Player> players = new ArrayList<>();
        players.add(new Player(TeamColor.Black, "Paul"));
        players.add(new Player(TeamColor.White, "Elise"));
        return new GameHistory(new GameConfig(
                players,
                players.get(1), // firstPlayer
                GameMode.Discovery,
                new ArrayList<>(), // initialRecruitableCards
                new ArrayList<>() // initialPlacements
        ), new ArrayList<>());
    }

    private CharacterActionBuilder createBuilder() {
        return new CharacterActionBuilder(character, new ArrayList<>(), new ArrayList<>());
    }

    @NonNull
    private InteractionResult createPositionResult(@NonNull Position position) {
        return new InteractionResult(
                InteractionResultType.PositionChosen,
                new InteractionTarget(TargetCategory.MovementDestination, position)
        );
    }

    @Before
    public void setUp() {
        Game game = createTestGame();
        GameHistory gameHistory = createTestGameHistory();
        Character leader = Character.create(CharacterType.LeaderKing, TeamColor.Black);
        game.getBoard().getCell(new Position(0, 0)).setCharacter(leader);
        // Hermit is chosen as a "default" character since he has resolver behavior
        character = Character.create(CharacterType.Hermit, TeamColor.Black);
        game.getBoard().getCell(new Position(3, 3)).setCharacter(character);
        resolver = new CharacterActionResolver(game, gameHistory, character);
    }

    @Test
    public void getNextInteraction_shouldRequestMovementDestination() {
        CharacterActionBuilder builder = createBuilder();

        InteractionRequest request = resolver.getNextInteraction(builder);

        assertNotNull(request);
        assertEquals(InteractionType.PositionExpected, request.getType());
        assertTrue(request.getLegalResults().contains(InteractionResultType.PositionChosen));
        assertTrue(request.getLegalResults().contains(InteractionResultType.CancelAction));

        for (InteractionTarget target : request.getLegalTargets()) {
            assertEquals(TargetCategory.MovementDestination, target.getCategory());
        }
    }

    @Test
    public void getNextInteraction_shouldReturnNullAfterPositionChosen() {
        CharacterActionBuilder builder = createBuilder();
        builder.addResult(createPositionResult(new Position(3, 1)));

        InteractionRequest request = resolver.getNextInteraction(builder);
        assertNull(request);
    }

    @Test
    public void getNextFeedback_shouldCreateMoveFeedback() {
        Position destination = new Position(3, 2);
        CharacterActionBuilder builder = createBuilder();
        builder.addResult(createPositionResult(destination));

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
    public void buildAction_shouldBuildActionFromFeedback() {
        Position destination = new Position(3, 1);
        CharacterActionBuilder builder = createBuilder();
        builder.addResult(createPositionResult(destination));

        InteractionFeedback feedback = resolver.getNextFeedback(builder);
        
        assertNotNull(feedback);

        builder.addFeedback(feedback);
        CharacterAction action = resolver.buildAction(builder);

        assertEquals(character, action.getSrcCharacter());
        assertEquals(1, action.getMotions().size());
        assertEquals(CharacterMotionType.Move,
                action.getMotions().get(0).getMotionType());
    }
}
