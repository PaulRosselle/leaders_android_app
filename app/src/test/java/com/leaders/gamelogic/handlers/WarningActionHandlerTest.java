package com.leaders.gamelogic.handlers;

import static org.junit.Assert.assertEquals;

import com.leaders.gamelogic.actions.WarningAction;
import com.leaders.gamelogic.entities.Board;
import com.leaders.gamelogic.entities.Game;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.gamelogic.enums.WarningType;

import org.junit.Test;

import java.util.ArrayList;
import java.util.EnumMap;

public class WarningActionHandlerTest {

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

    @Test
    public void doAction_shouldIncreaseBarrageWarningCount() {
        Game game = createTestGame();
        WarningAction action = new WarningAction(WarningType.Barrage, TeamColor.Black, 1);

        new WarningActionHandler(game, action).doAction();
        assertEquals(1, game.getPlayerWarningCount(TeamColor.Black, WarningType.Barrage));
    }

    @Test
    public void doAction_shouldDecreaseBarrageWarningCount() {
        Game game = createTestGame();
        game.setPlayerWarningCount(TeamColor.Black, WarningType.Barrage, 2);

        WarningAction action = new WarningAction(WarningType.Barrage, TeamColor.Black, -1);
        new WarningActionHandler(game, action).doAction();

        assertEquals(1, game.getPlayerWarningCount(TeamColor.Black, WarningType.Barrage));
    }

    @Test
    public void doAction_shouldOnlyModifyTargetTeamWarningCount() {
        Game game = createTestGame();
        game.setPlayerWarningCount(TeamColor.Black, WarningType.Barrage, 1);
        game.setPlayerWarningCount(TeamColor.White, WarningType.Barrage, 2);

        WarningAction action = new WarningAction(WarningType.Barrage, TeamColor.Black, 1);
        new WarningActionHandler(game, action).doAction();

        assertEquals(2, game.getPlayerWarningCount(TeamColor.Black, WarningType.Barrage));
        assertEquals(2, game.getPlayerWarningCount(TeamColor.White, WarningType.Barrage));
    }

    @Test
    public void doActionThenUndoAction_shouldRestoreGameState() {
        Game game = createTestGame();
        game.setPlayerWarningCount(TeamColor.Black, WarningType.Barrage, 2);
        game.setPlayerWarningCount(TeamColor.White, WarningType.Barrage, 3);

        WarningAction action = new WarningAction(WarningType.Barrage, TeamColor.Black, 1);

        WarningActionHandler handler = new WarningActionHandler(game, action);

        handler.doAction();
        handler.undoAction();

        assertEquals(2, game.getPlayerWarningCount(TeamColor.Black, WarningType.Barrage));
        assertEquals(3, game.getPlayerWarningCount(TeamColor.White, WarningType.Barrage));
    }
}