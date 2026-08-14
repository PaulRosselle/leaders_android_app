package com.leaders.gamelogic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.actions.BanishmentAction;
import com.leaders.gamelogic.actions.CharacterAction;
import com.leaders.gamelogic.actions.IGameAction;
import com.leaders.gamelogic.actions.RecruitmentAction;
import com.leaders.gamelogic.actions.RecruitmentActionMotion;
import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.entities.Game;
import com.leaders.gamelogic.entities.GameConfig;
import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.gamelogic.entities.GamePhase;
import com.leaders.gamelogic.entities.Player;
import com.leaders.gamelogic.entities.Position;
import com.leaders.gamelogic.enums.CharacterCard;
import com.leaders.gamelogic.enums.CharacterType;
import com.leaders.gamelogic.enums.GameMode;
import com.leaders.gamelogic.enums.GamePhaseType;
import com.leaders.gamelogic.enums.RecruitmentMotionType;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.gamelogic.historyentries.segments.ActionsPhase;
import com.leaders.gamelogic.historyentries.segments.BanishmentPhase;
import com.leaders.gamelogic.historyentries.segments.RecruitmentPhase;
import com.leaders.gamelogic.historyentries.segments.Turn;
import com.leaders.gamelogic.historyentries.segments.TurnEndPhase;
import com.leaders.gamelogic.historyentries.segments.TurnPhase;
import com.leaders.gamelogic.historyentries.segments.TurnStartPhase;
import com.leaders.gamelogic.interactions.IGameFlowListener;
import com.leaders.gamelogic.interactions.InteractionContext;
import com.leaders.gamelogic.interactions.InteractionFeedback;
import com.leaders.gamelogic.interactions.InteractionRequest;
import com.leaders.gamelogic.interactions.InteractionResult;
import com.leaders.gamelogic.interactions.InteractionResultType;
import com.leaders.gamelogic.interactions.InteractionTarget;
import com.leaders.gamelogic.interactions.InteractionType;
import com.leaders.gamelogic.interactions.TargetCategory;

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
        private int inputRequiredCount;
        private int gameStartedCount;
        private InteractionRequest lastInputRequired;
        private InteractionResult inputRequiredResult;
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
            inputRequiredCount++;
            lastInputRequired = request;
            return CompletableFuture.completedFuture(inputRequiredResult);
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

        int getInputRequiredCount() {
            return inputRequiredCount;
        }

        void setInputRequiredResult(@NonNull InteractionResult result) {
            inputRequiredResult = result;
        }

        InteractionRequest getLastInputRequired() {
            return lastInputRequired;
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
        assertEquals(0, listener.getInputRequiredCount());
        assertTrue(turnStartPhase.getActions().isEmpty());

        assertNotNull(listener.getLastPhaseChanged());
        assertEquals(GamePhaseType.Actions, listener.getLastPhaseChanged().getPhaseType());
    }

    @Test
    public void doAction_shouldApplyActionAndAppendItToCurrentPhase() throws Exception {
        GameHistory history = createGameHistory(
                GameMode.Strategist,
                createPlayers(),
                Collections.singletonList(CharacterCard.HermitAndCub)
        );
        BanishmentPhase phase = new BanishmentPhase(TeamColor.Black);
        phase.start();
        history.getEntries().add(phase);

        GameHandler gameHandler = new GameHandler(history, new TestGameFlowListener(history));
        BanishmentAction action = new BanishmentAction(CharacterCard.HermitAndCub, TeamColor.Black);

        invokeDoAction(gameHandler, new GamePhase(GamePhaseType.Banishment,
                history.getConfig().getPlayers().get(0)), action);

        assertEquals(0, gameHandler.getCurrentGame().getRecruitableCards().size());
        assertEquals(1, gameHandler.getCurrentGame().getBanishedCards(TeamColor.Black).size());
        assertEquals(CharacterCard.HermitAndCub, gameHandler.getCurrentGame().getBanishedCards(TeamColor.Black).get(0));

        assertEquals(1, phase.getActions().size());
        assertSame(action, phase.getActions().get(0));
    }

    @Test
    public void doAction_shouldThrowWhenNoCurrentPhaseExists() throws Exception {
        GameHistory history = createGameHistory(
                GameMode.Strategist,
                createPlayers(),
                Collections.singletonList(CharacterCard.HermitAndCub)
        );
        GameHandler gameHandler = new GameHandler(history, new TestGameFlowListener(history));
        BanishmentAction action = new BanishmentAction(CharacterCard.HermitAndCub, TeamColor.Black);

        try {
            invokeDoAction(gameHandler, new GamePhase(
                    GamePhaseType.Banishment, history.getConfig().getPlayers().get(0)), action);
            fail("Expected IllegalStateException");
        } catch (IllegalStateException exception) {
            assertEquals("Cannot do an action outside of a game phase", exception.getMessage());
        }

        assertEquals(1, gameHandler.getCurrentGame().getRecruitableCards().size());
        assertEquals(CharacterCard.HermitAndCub, gameHandler.getCurrentGame().getRecruitableCards().get(0));
        assertTrue(history.getEntries().isEmpty());
    }

    @Test
    public void undoLastAction_shouldUndoAndRemoveLastAction() throws Exception {
        GameHistory history = createGameHistory(
                GameMode.Strategist,
                createPlayers(),
                Collections.singletonList(CharacterCard.HermitAndCub)
        );
        BanishmentPhase phase = new BanishmentPhase(TeamColor.Black);
        phase.start();
        history.getEntries().add(phase);

        GameHandler gameHandler = new GameHandler(history, new TestGameFlowListener(history));
        BanishmentAction action = new BanishmentAction(CharacterCard.HermitAndCub, TeamColor.Black);

        invokeDoAction(gameHandler, new GamePhase(GamePhaseType.Banishment,
                history.getConfig().getPlayers().get(0)), action);
        invokeUndoLastAction(gameHandler);

        assertTrue(phase.getActions().isEmpty());
        assertEquals(1, gameHandler.getCurrentGame().getRecruitableCards().size());
        assertEquals(CharacterCard.HermitAndCub,
                gameHandler.getCurrentGame().getRecruitableCards().get(0));
        assertTrue(gameHandler.getCurrentGame().getBanishedCards(TeamColor.Black).isEmpty());
    }

    @Test
    public void undoLastAction_shouldThrowWhenNoCurrentPhaseExists() throws Exception {
        GameHistory history = createGameHistory();
        GameHandler gameHandler = new GameHandler(history, new TestGameFlowListener(history));

        try {
            invokeUndoLastAction(gameHandler);
            fail("Expected IllegalStateException");
        } catch (IllegalStateException exception) {
            assertEquals("Cannot undo an action outside of a game phase or within an empty phase",
                    exception.getMessage());
        }
    }

    @Test
    public void undoLastAction_shouldThrowWhenCurrentPhaseIsEmpty() throws Exception {
        GameHistory history = createGameHistory();
        BanishmentPhase phase = new BanishmentPhase(TeamColor.Black);
        phase.start();
        history.getEntries().add(phase);

        GameHandler gameHandler = new GameHandler(history, new TestGameFlowListener(history));

        try {
            invokeUndoLastAction(gameHandler);
            fail("Expected IllegalStateException");
        } catch (IllegalStateException exception) {
            assertEquals("Cannot undo an action outside of a game phase or within an empty phase",
                    exception.getMessage());
        }
    }

    @Test
    public void doAction_thenUndoLastAction_shouldRestorePreviousGameState() throws Exception {
        GameHistory history = createGameHistory(
                GameMode.Strategist,
                createPlayers(),
                Collections.singletonList(CharacterCard.HermitAndCub)
        );
        BanishmentPhase phase = new BanishmentPhase(TeamColor.Black);
        phase.start();
        history.getEntries().add(phase);

        GameHandler gameHandler = new GameHandler(history, new TestGameFlowListener(history));

        int initialRecruitableCardsCount = gameHandler.getCurrentGame().getRecruitableCards().size();
        List<CharacterCard> initialBanishedCards =
                new ArrayList<>(gameHandler.getCurrentGame().getBanishedCards(TeamColor.Black));

        BanishmentAction action = new BanishmentAction(CharacterCard.HermitAndCub, TeamColor.Black);

        invokeDoAction(gameHandler, new GamePhase(GamePhaseType.Banishment,
                history.getConfig().getPlayers().get(0)), action);

        assertEquals(initialRecruitableCardsCount - 1,
                gameHandler.getCurrentGame().getRecruitableCards().size());
        assertEquals(initialBanishedCards.size() + 1,
                gameHandler.getCurrentGame().getBanishedCards(TeamColor.Black).size());
        assertEquals(1, phase.getActions().size());

        invokeUndoLastAction(gameHandler);

        assertEquals(initialRecruitableCardsCount,
                gameHandler.getCurrentGame().getRecruitableCards().size());
        assertEquals(initialBanishedCards.size(),
                gameHandler.getCurrentGame().getBanishedCards(TeamColor.Black).size());
        assertTrue(gameHandler.getCurrentGame().getBanishedCards(TeamColor.Black).containsAll(initialBanishedCards));
        assertTrue(phase.getActions().isEmpty());
    }

    @Test
    public void runCurrentPhaseAsync_shouldCompleteTurnStartPhaseWithoutInputOrAction() {
        GameHistory history = createGameHistory(GameMode.Discovery);

        Turn turn = new Turn(TeamColor.Black);
        history.getEntries().add(turn);

        TurnStartPhase turnStartPhase = (TurnStartPhase) turn.getSubPhase(GamePhaseType.TurnStart);
        turnStartPhase.start();

        TestGameFlowListener listener = new TestGameFlowListener(history);
        GameHandler gameHandler = new GameHandler(history, listener);
        GamePhase currentPhase = new GamePhase(
                GamePhaseType.TurnStart,
                history.getConfig().getPlayers().get(0)
        );

        CompletableFuture<Void> result = invokeRunCurrentPhase(gameHandler, currentPhase);

        result.join();

        assertTrue(turnStartPhase.hasStarted());
        assertTrue(turnStartPhase.hasEnded());
        assertTrue(turnStartPhase.getActions().isEmpty());
        assertEquals(0, listener.getInputRequiredCount());
        assertFalse(turn.hasEnded());
    }

    @Test
    public void runSelectPlayableCharacterAsync_shouldRequestPlayableCharacters() throws Exception {
        List<Character> characters = new ArrayList<>();
        GameHistory history = createPlayableCharacterGameHistory(characters);
        TestGameFlowListener listener = new TestGameFlowListener(history);

        InteractionResult expectedResult = new InteractionResult(
                InteractionResultType.EndPhase,
                new InteractionContext(),
                null
        );
        listener.setInputRequiredResult(expectedResult);

        GameHandler gameHandler = new GameHandler(history, listener);
        GamePhase currentPhase = new GamePhase(
                GamePhaseType.Actions,
                history.getConfig().getPlayers().get(0)
        );

        InteractionResult result = invokeRunSelectPlayableCharacterAsync(gameHandler, currentPhase);

        assertSame(expectedResult, result);
        assertEquals(1, listener.getInputRequiredCount());

        InteractionRequest request = listener.getLastInputRequired();

        assertNotNull(request);
        assertEquals(InteractionType.PositionExpected, request.getRequestType());
        assertEquals(
                Arrays.asList(
                        InteractionResultType.PlayableCharacterChosen,
                        InteractionResultType.EndPhase
                ),
                request.getLegalResults()
        );

        assertEquals(2, request.getLegalTargets().size());

        for (InteractionTarget target : request.getLegalTargets()) {
            assertEquals(TargetCategory.PlayableCharacter, target.getCategory());
            assertNotNull(target.getChosenCharacterPlayableState());
        }

        assertTrue(request.getLegalTargets().stream()
                .anyMatch(target -> {
                    assertNotNull(target.getChosenCharacterPlayableState());
                    return target.getChosenCharacterPlayableState().getCharacter().getCharacterType()
                            == CharacterType.Acrobat;
                }));

        assertTrue(request.getLegalTargets().stream()
                .anyMatch(target -> {
                    assertNotNull(target.getChosenCharacterPlayableState());
                    return target.getChosenCharacterPlayableState().getCharacter().getCharacterType()
                            == CharacterType.Archer;
                }));
    }

    @Test
    public void runSelectPlayableCharacterAsync_shouldAllowUndoWhenActionsPhaseContainsAction() throws Exception {
        List<Character> characters = new ArrayList<>();
        GameHistory history = createPlayableCharacterGameHistory(characters);

        Turn turn = (Turn) history.getEntries().get(0);
        ActionsPhase actionsPhase = (ActionsPhase) turn.getSubPhase(GamePhaseType.Actions);

        actionsPhase.getActions().add(new CharacterAction(characters.get(0), new ArrayList<>()));

        TestGameFlowListener listener = new TestGameFlowListener(history);
        InteractionResult expectedResult = new InteractionResult(
                InteractionResultType.UndoLastAction,
                new InteractionContext(),
                null
        );
        listener.setInputRequiredResult(expectedResult);

        GameHandler gameHandler = new GameHandler(history, listener);
        GamePhase currentPhase = new GamePhase(
                GamePhaseType.Actions,
                history.getConfig().getPlayers().get(0)
        );

        InteractionResult result = invokeRunSelectPlayableCharacterAsync(gameHandler, currentPhase);

        assertSame(expectedResult, result);

        InteractionRequest request = listener.getLastInputRequired();

        assertNotNull(request);
        assertEquals(
                Arrays.asList(
                        InteractionResultType.PlayableCharacterChosen,
                        InteractionResultType.UndoLastAction,
                        InteractionResultType.EndPhase
                ),
                request.getLegalResults()
        );
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

    private void invokeDoAction(GameHandler gameHandler, GamePhase currentPhase, BanishmentAction action) throws Exception {
        Method method = GameHandler.class.getDeclaredMethod("doAction", GamePhase.class, IGameAction.class);
        method.setAccessible(true);

        try {
            method.invoke(gameHandler, currentPhase, action);
        } catch (InvocationTargetException exception) {
            Throwable cause = exception.getCause();
            if (cause instanceof Exception) {
                throw (Exception) cause;
            }
            throw exception;
        }
    }

    private void invokeUndoLastAction(GameHandler gameHandler) throws Exception {
        Method method = GameHandler.class.getDeclaredMethod("undoLastAction");
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

    @SuppressWarnings("unchecked")
    private CompletableFuture<Void> invokeRunCurrentPhase(GameHandler gameHandler, GamePhase currentPhase) {
        try {
            Method method = GameHandler.class.getDeclaredMethod("runCurrentPhaseAsync", GamePhase.class);
            method.setAccessible(true);
            return (CompletableFuture<Void>) method.invoke(gameHandler, currentPhase);
        } catch (InvocationTargetException exception) {
            Throwable cause = exception.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            throw new RuntimeException(cause);
        } catch (ReflectiveOperationException exception) {
            throw new RuntimeException(exception);
        }
    }

    @SuppressWarnings("unchecked")
    private InteractionResult invokeRunSelectPlayableCharacterAsync(GameHandler gameHandler,
                                                                    GamePhase currentPhase) throws Exception {
        Method method = GameHandler.class.getDeclaredMethod(
                "runSelectPlayableCharacterAsync",
                GamePhase.class
        );
        method.setAccessible(true);

        try {
            CompletableFuture<InteractionResult> result =
                    (CompletableFuture<InteractionResult>) method.invoke(gameHandler, currentPhase);
            assertNotNull(result);
            return result.join();
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
        return createGameHistory(gameMode, players, Collections.emptyList());
    }

    private GameHistory createGameHistory(GameMode gameMode, List<Player> players,
                                          List<CharacterCard> initialRecruitableCards) {
        GameConfig config = new GameConfig(
                players,
                players.get(0),
                gameMode,
                initialRecruitableCards,
                Collections.emptyList()
        );
        return new GameHistory(config, new ArrayList<>());
    }

    private GameHistory createPlayableCharacterGameHistory(@NonNull List<Character> characters) {
        List<Player> players = createPlayers();

        Character acrobat = Character.create(CharacterType.Acrobat, TeamColor.Black);
        Character archer = Character.create(CharacterType.Archer, TeamColor.Black);

        characters.add(acrobat);
        characters.add(archer);

        RecruitmentAction initialPlacement = new RecruitmentAction(Arrays.asList(
                new RecruitmentActionMotion(RecruitmentMotionType.Add, acrobat, new Position(3, 3)),
                new RecruitmentActionMotion(RecruitmentMotionType.Add, archer, new Position(3, 4))
        ));

        GameConfig config = new GameConfig(
                players,
                players.get(0),
                GameMode.Discovery,
                Arrays.asList(CharacterCard.Acrobat, CharacterCard.Archer),
                Collections.singletonList(initialPlacement)
        );

        GameHistory history = new GameHistory(config, new ArrayList<>());

        Turn turn = new Turn(TeamColor.Black);
        history.getEntries().add(turn);

        turn.getSubPhase(GamePhaseType.TurnStart).start();
        turn.getSubPhase(GamePhaseType.TurnStart).end();
        turn.getSubPhase(GamePhaseType.Actions).start();

        return history;
    }

    private List<Player> createPlayers() {
        return Arrays.asList(
                new Player(TeamColor.Black, "Black Player"),
                new Player(TeamColor.White, "White Player")
        );
    }
}