package com.leaders.gamelogic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.entities.Game;
import com.leaders.gamelogic.entities.GameConfig;
import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.gamelogic.entities.GamePhase;
import com.leaders.gamelogic.entities.Player;
import com.leaders.gamelogic.enums.GameMode;
import com.leaders.gamelogic.enums.GamePhaseType;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.gamelogic.historyentries.segments.ActionsPhase;
import com.leaders.gamelogic.historyentries.segments.BanishmentPhase;
import com.leaders.gamelogic.historyentries.segments.RecruitmentPhase;
import com.leaders.gamelogic.historyentries.segments.Turn;
import com.leaders.gamelogic.historyentries.segments.TurnEndPhase;
import com.leaders.gamelogic.historyentries.segments.TurnPhase;
import com.leaders.gamelogic.historyentries.segments.TurnStartPhase;
import com.leaders.gamelogic.interactions.IGameFlowListener;
import com.leaders.gamelogic.interactions.InteractionFeedback;
import com.leaders.gamelogic.interactions.InteractionRequest;
import com.leaders.gamelogic.interactions.InteractionResult;

import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class GameHandlerTest {
    private static class TestGameFlowListener implements IGameFlowListener {
        private final GameHistory history;
        private GamePhase lastPhaseChanged;
        private boolean phaseWasStartedWhenNotified;
        private int gameStartedCount;
        private int gameEndedCount;
        private CompletableFuture<Void> gameStartedResult = CompletableFuture.completedFuture(null);
        private CompletableFuture<Void> phaseChangedResult = CompletableFuture.completedFuture(null);

        private TestGameFlowListener(GameHistory history) {
            this.history = history;
        }

        @NonNull
        @Override
        public CompletableFuture<Void> onGameStarted(@NonNull Game game) {
            gameStartedCount++;
            return CompletableFuture.completedFuture(null);
        }

        @NonNull
        @Override
        public CompletableFuture<Void> onGameEnded(@NonNull Player winner) {
            gameEndedCount++;
            return CompletableFuture.completedFuture(null);
        }

        @NonNull
        @Override
        public CompletableFuture<Void> onPhaseChanged(@NonNull GamePhase phase) {
            lastPhaseChanged = phase;
            phaseWasStartedWhenNotified = isCurrentPhaseStarted();
            return phaseChangedResult;
        }

        @NonNull
        @Override
        public CompletableFuture<InteractionResult> onInputRequired(@NonNull InteractionRequest request) {
            return CompletableFuture.completedFuture(null);
        }

        @NonNull
        @Override
        public CompletableFuture<Void> onFeedback(@NonNull InteractionFeedback feedback) {
            return CompletableFuture.completedFuture(null);
        }

        private boolean isCurrentPhaseStarted() {
            if (history.getEntries().isEmpty()) {
                return false;
            }

            Object lastEntry = history.getEntries().get(history.getEntries().size() - 1);
            if (lastEntry instanceof Turn) {
                return ((Turn) lastEntry).getSubPhase(GamePhaseType.TurnStart).hasStarted();
            }
            return lastEntry instanceof BanishmentPhase && ((BanishmentPhase) lastEntry).hasStarted();
        }

        GamePhase getLastPhaseChanged() {
            return lastPhaseChanged;
        }

        boolean wasPhaseStartedWhenNotified() {
            return phaseWasStartedWhenNotified;
        }
    }

    @Test
    public void constructor_createsGameHandler() {
        GameHistory history = createGameHistory();
        IGameFlowListener listener = new TestGameFlowListener(history);

        GameHandler gameHandler = new GameHandler(history, listener);

        assertNotNull(gameHandler);
    }

    @Test
    public void getCurrentGame_returnsCurrentGame() {
        GameHistory history = createGameHistory();
        GameHandler gameHandler = new GameHandler(history, new TestGameFlowListener(history));

        Game game = gameHandler.getCurrentGame();

        assertNotNull(game);
    }

    @Test
    public void getCurrentHistory_returnsCurrentHistory() {
        GameHistory history = createGameHistory();
        GameHandler gameHandler = new GameHandler(history, new TestGameFlowListener(history));

        assertSame(history, gameHandler.getCurrentHistory());
    }

    @Test
    public void getGameMode_returnsCurrentGameMode() {
        GameMode expectedGameMode = GameMode.Strategist;
        GameHistory history = createGameHistory(expectedGameMode);
        GameHandler gameHandler = new GameHandler(history, new TestGameFlowListener(history));

        assertSame(expectedGameMode, gameHandler.getGameMode());
    }

    @Test
    public void getPlayers_returnsCurrentPlayers() {
        List<Player> expectedPlayers = Arrays.asList(
                new Player(TeamColor.Black, "Black Player"),
                new Player(TeamColor.White, "White Player")
        );
        GameHistory history = createGameHistory(GameMode.Discovery, expectedPlayers);
        GameHandler gameHandler = new GameHandler(history, new TestGameFlowListener(history));

        List<Player> actualPlayers = gameHandler.getPlayers();

        assertNotSame(expectedPlayers, actualPlayers);
        assertEquals(expectedPlayers.size(), actualPlayers.size());

        for (int playerIdx = 0; playerIdx < expectedPlayers.size(); playerIdx++) {
            assertSame(expectedPlayers.get(playerIdx), actualPlayers.get(playerIdx));
        }
    }

    @Test
    public void startNextPhaseAsync_shouldStartTurnStartPhase() throws Exception {
        GameHistory history = createGameHistory(GameMode.Discovery);
        GameHandler gameHandler = new GameHandler(history, new TestGameFlowListener(history));

        CompletableFuture<Void> result = invokeStartNextPhase(gameHandler);

        result.join();

        assertEquals(1, history.getEntries().size());
        assertTrue(history.getEntries().get(0) instanceof Turn);

        Turn turn = (Turn) history.getEntries().get(0);
        assertTrue(turn.hasStarted());
        assertTrue(turn.getSubPhase(GamePhaseType.TurnStart).hasStarted());
    }

    @Test
    public void startNextPhaseAsync_shouldStartBanishmentPhase() throws Exception {
        GameHistory history = createGameHistory(GameMode.Strategist);
        GameHandler gameHandler = new GameHandler(history, new TestGameFlowListener(history));

        CompletableFuture<Void> result = invokeStartNextPhase(gameHandler);

        result.join();

        assertEquals(1, history.getEntries().size());
        assertTrue(history.getEntries().get(0) instanceof BanishmentPhase);

        BanishmentPhase banishmentPhase = (BanishmentPhase) history.getEntries().get(0);

        assertTrue(banishmentPhase.hasStarted());
    }

    @Test
    public void startNextPhaseAsync_shouldStartNextTurnPhase() throws Exception {
        GameHistory history = createGameHistory(GameMode.Discovery);
        Turn turn = new Turn(TeamColor.Black);
        history.getEntries().add(turn);

        TurnPhase turnStartPhase = turn.getSubPhase(GamePhaseType.TurnStart);
        turnStartPhase.start();
        turnStartPhase.end();

        GameHandler gameHandler = new GameHandler(history, new TestGameFlowListener(history));

        CompletableFuture<Void> result = invokeStartNextPhase(gameHandler);

        result.join();

        assertEquals(1, history.getEntries().size());
        assertSame(turn, history.getEntries().get(0));

        ActionsPhase actionsPhase = (ActionsPhase) turn.getSubPhase(GamePhaseType.Actions);
        assertTrue(actionsPhase.hasStarted());
        assertFalse(actionsPhase.hasEnded());
    }

    @Test
    public void startNextPhaseAsync_shouldNotifyAfterStartingPhase() throws Exception {
        GameHistory history = createGameHistory(GameMode.Discovery);
        TestGameFlowListener listener = new TestGameFlowListener(history);
        GameHandler gameHandler = new GameHandler(history, listener);

        CompletableFuture<Void> result = invokeStartNextPhase(gameHandler);

        result.join();

        assertNotNull(listener.getLastPhaseChanged());
        assertEquals(GamePhaseType.TurnStart, listener.getLastPhaseChanged().getPhaseType());
        assertTrue(listener.wasPhaseStartedWhenNotified());
    }

    @Test
    public void endCurrentPhase_shouldEndTurnStartPhase() throws Exception {
        GameHistory history = createGameHistory(GameMode.Discovery);
        Turn turn = new Turn(TeamColor.Black);
        history.getEntries().add(turn);

        TurnStartPhase turnStartPhase = (TurnStartPhase) turn.getSubPhase(GamePhaseType.TurnStart);
        turnStartPhase.start();

        GameHandler gameHandler = new GameHandler(history, new TestGameFlowListener(history));

        invokeEndCurrentPhase(gameHandler);

        assertTrue(turnStartPhase.hasStarted());
        assertTrue(turnStartPhase.hasEnded());
        assertFalse(turn.hasEnded());
    }

    @Test
    public void endCurrentPhase_shouldEndActionsPhase() throws Exception {
        GameHistory history = createGameHistory(GameMode.Discovery);
        Turn turn = new Turn(TeamColor.Black);
        history.getEntries().add(turn);

        ActionsPhase actionsPhase = (ActionsPhase) turn.getSubPhase(GamePhaseType.Actions);
        actionsPhase.start();

        GameHandler gameHandler = new GameHandler(history, new TestGameFlowListener(history));

        invokeEndCurrentPhase(gameHandler);

        assertTrue(actionsPhase.hasStarted());
        assertTrue(actionsPhase.hasEnded());
        assertFalse(turn.hasEnded());
    }

    @Test
    public void endCurrentPhase_shouldEndRecruitmentPhase() throws Exception {
        GameHistory history = createGameHistory(GameMode.Discovery);
        Turn turn = new Turn(TeamColor.Black);
        history.getEntries().add(turn);

        RecruitmentPhase recruitmentPhase = (RecruitmentPhase) turn.getSubPhase(GamePhaseType.Recruitment);
        recruitmentPhase.start();

        GameHandler gameHandler = new GameHandler(history, new TestGameFlowListener(history));

        invokeEndCurrentPhase(gameHandler);

        assertTrue(recruitmentPhase.hasStarted());
        assertTrue(recruitmentPhase.hasEnded());
        assertFalse(turn.hasEnded());
    }

    @Test
    public void endCurrentPhase_shouldEndBanishmentPhase() throws Exception {
        GameHistory history = createGameHistory(GameMode.Strategist);
        BanishmentPhase banishmentPhase = new BanishmentPhase(TeamColor.Black);
        history.getEntries().add(banishmentPhase);
        banishmentPhase.start();

        GameHandler gameHandler = new GameHandler(history, new TestGameFlowListener(history));

        invokeEndCurrentPhase(gameHandler);

        assertTrue(banishmentPhase.hasStarted());
        assertTrue(banishmentPhase.hasEnded());
    }

    @Test
    public void endCurrentPhase_shouldEndTurnEndPhaseAndTurn() throws Exception {
        GameHistory history = createGameHistory(GameMode.Discovery);
        Turn turn = new Turn(TeamColor.Black);
        history.getEntries().add(turn);

        TurnEndPhase turnEndPhase = (TurnEndPhase) turn.getSubPhase(GamePhaseType.TurnEnd);
        turnEndPhase.start();

        GameHandler gameHandler = new GameHandler(history, new TestGameFlowListener(history));

        invokeEndCurrentPhase(gameHandler);

        assertTrue(turnEndPhase.hasStarted());
        assertTrue(turnEndPhase.hasEnded());
        assertTrue(turn.hasEnded());
    }

    @Test
    public void endCurrentPhase_shouldThrowWhenNoCurrentPhaseExists() throws Exception {
        GameHistory history = createGameHistory();
        GameHandler gameHandler = new GameHandler(history, new TestGameFlowListener(history));

        assertThrows(IllegalStateException.class, () -> invokeEndCurrentPhase(gameHandler));
    }

    @Test
    public void runAsync_shouldCallOnGameStartedOnce() {
        GameHistory history = createGameHistory(GameMode.Discovery);
        TestGameFlowListener listener = new TestGameFlowListener(history);

        CompletableFuture<Void> failedStart = new CompletableFuture<>();
        failedStart.completeExceptionally(new IllegalStateException("Test failure"));
        listener.gameStartedResult = failedStart;

        GameHandler gameHandler = new GameHandler(history, listener);

        CompletableFuture<Void> result = gameHandler.runAsync();

        assertTrue(result.isCompletedExceptionally());
        assertEquals(1, listener.gameStartedCount);
    }

    @Test
    public void runAsync_shouldStartNextPhaseWhenNoPhaseIsActive() {
        GameHistory history = createGameHistory(GameMode.Discovery);
        TestGameFlowListener listener = new TestGameFlowListener(history);
        listener.phaseChangedResult = new CompletableFuture<>();

        GameHandler gameHandler = new GameHandler(history, listener);

        CompletableFuture<Void> result = gameHandler.runAsync();

        assertFalse(result.isDone());
        assertEquals(1, history.getEntries().size());
        assertTrue(history.getEntries().get(0) instanceof Turn);
        assertNotNull(listener.getLastPhaseChanged());
        assertEquals(GamePhaseType.TurnStart, listener.getLastPhaseChanged().getPhaseType());
        assertTrue(listener.wasPhaseStartedWhenNotified());
    }

    @Test
    public void runAsync_shouldRunCurrentPhaseWhenPhaseIsActive() {
        GameHistory history = createGameHistory(GameMode.Discovery);

        Turn turn = new Turn(TeamColor.Black);
        history.getEntries().add(turn);

        TurnStartPhase turnStartPhase = (TurnStartPhase) turn.getSubPhase(GamePhaseType.TurnStart);
        turnStartPhase.start();

        TestGameFlowListener listener = new TestGameFlowListener(history);
        listener.phaseChangedResult = new CompletableFuture<>();

        GameHandler gameHandler = new GameHandler(history, listener);

        CompletableFuture<Void> result = gameHandler.runAsync();

        assertFalse(result.isDone());
        assertTrue(turnStartPhase.hasStarted());
        assertTrue(turnStartPhase.hasEnded());

        assertNotNull(listener.getLastPhaseChanged());
        assertEquals(GamePhaseType.Actions, listener.getLastPhaseChanged().getPhaseType());
    }

    @SuppressWarnings("unchecked")
    private CompletableFuture<Void> invokeStartNextPhase(GameHandler gameHandler) throws Exception {
        Method method = GameHandler.class.getDeclaredMethod("startNextPhaseAsync");
        method.setAccessible(true);

        try {
            return (CompletableFuture<Void>) method.invoke(gameHandler);
        } catch (InvocationTargetException exception) {
            Throwable cause = exception.getCause();
            if (cause instanceof Exception) {
                throw (Exception) cause;
            }
            throw exception;
        }
    }

    private void invokeEndCurrentPhase(GameHandler gameHandler) throws Exception {
        Method method = GameHandler.class.getDeclaredMethod("endCurrentPhase");
        method.setAccessible(true);

        try {
            method.invoke(gameHandler);
        } catch (InvocationTargetException exception) {
            Throwable cause = exception.getCause();
            if (cause instanceof Exception) {
                throw (Exception) cause;
            }
            throw exception;
        }
    }

    private GameHistory createGameHistory() {
        return createGameHistory(GameMode.Discovery, createPlayers());
    }

    private GameHistory createGameHistory(GameMode gameMode) {
        return createGameHistory(gameMode, createPlayers());
    }

    private GameHistory createGameHistory(GameMode gameMode, List<Player> players) {
        GameConfig config = new GameConfig(
                players,
                players.get(0),
                gameMode,
                Collections.emptyList(),
                Collections.emptyList()
        );
        return new GameHistory(config, new ArrayList<>());
    }

    private List<Player> createPlayers() {
        return Arrays.asList(
                new Player(TeamColor.Black, "Black Player"),
                new Player(TeamColor.White, "White Player")
        );
    }
}