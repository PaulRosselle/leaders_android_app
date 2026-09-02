package com.leaders.gamelogic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
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
import com.leaders.gamelogic.actions.WarningAction;
import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.entities.Game;
import com.leaders.gamelogic.entities.GameConfig;
import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.gamelogic.entities.GamePhase;
import com.leaders.gamelogic.entities.PlayableCharacter;
import com.leaders.gamelogic.entities.Player;
import com.leaders.gamelogic.entities.Position;
import com.leaders.gamelogic.entities.SelectableCharacterCard;
import com.leaders.gamelogic.enums.CharacterCard;
import com.leaders.gamelogic.enums.CharacterCardSelectionStatus;
import com.leaders.gamelogic.enums.CharacterType;
import com.leaders.gamelogic.enums.GameMode;
import com.leaders.gamelogic.enums.GamePhaseType;
import com.leaders.gamelogic.enums.RecruitmentMotionType;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.gamelogic.enums.WarningType;
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
import java.util.concurrent.CompletionException;

public class GameHandlerTest {
    private static class TestGameFlowListener implements IGameFlowListener {
        private final GameHistory history;
        private GamePhase lastPhaseChanged;
        private boolean phaseWasStartedWhenNotified;
        private int inputRequiredCount;
        private int actionUndoneCount;
        private Game lastActionUndoneGame;
        private int gameStartedCount;
        private InteractionRequest lastInputRequired;
        private final List<InteractionResult> inputRequiredResults = new ArrayList<>();
        private InteractionFeedback lastFeedback;
        private int feedbackCount;
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
        public CompletableFuture<Void> onActionUndone(@NonNull Game game) {
            actionUndoneCount++;
            lastActionUndoneGame = game;
            return CompletableFuture.completedFuture(null);
        }

        @NonNull
        @Override
        public CompletableFuture<InteractionResult> onInputRequired(@NonNull InteractionRequest request) {
            inputRequiredCount++;
            lastInputRequired = request;

            if (inputRequiredResults.isEmpty()) {
                throw new IllegalStateException(
                        "No test result available for interaction #" +
                                inputRequiredCount +
                                ": " +
                                request.getRequestType()
                );
            }
            return CompletableFuture.completedFuture(inputRequiredResults.remove(0));
        }

        @NonNull
        @Override
        public CompletableFuture<Void> onFeedback(@NonNull InteractionFeedback feedback) {
            feedbackCount++;
            lastFeedback = feedback;
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

        InteractionRequest getLastInputRequired() {
            return lastInputRequired;
        }

        int getFeedbackCount() {
            return feedbackCount;
        }

        InteractionFeedback getLastFeedback() {
            return lastFeedback;
        }

        int getActionUndoneCount() {
            return actionUndoneCount;
        }

        Game getLastActionUndoneGame() {
            return lastActionUndoneGame;
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
    public void endCurrentPhase_shouldThrowWhenNoCurrentPhaseExists() {
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
        listener.inputRequiredResults.add(expectedResult);

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
        assertEquals(InteractionType.PlayableCharacterExpected, request.getRequestType());
        assertEquals(
                Arrays.asList(
                        InteractionResultType.PlayableCharacterChosen,
                        InteractionResultType.EndPhase
                ),
                request.getLegalResults()
        );

        assertEquals(3, request.getLegalTargets().size());

        for (InteractionTarget target : request.getLegalTargets()) {
            assertEquals(TargetCategory.PlayableCharacter, target.getCategory());
            assertNotNull(target.getChosenPlayableCharacter());
        }

        assertTrue(request.getLegalTargets().stream()
                .anyMatch(target -> {
                    assertNotNull(target.getChosenPlayableCharacter());
                    return target.getChosenPlayableCharacter().getCharacter().getCharacterType()
                            == CharacterType.Acrobat;
                }));

        assertTrue(request.getLegalTargets().stream()
                .anyMatch(target -> {
                    assertNotNull(target.getChosenPlayableCharacter());
                    return target.getChosenPlayableCharacter().getCharacter().getCharacterType()
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
        listener.inputRequiredResults.add(expectedResult);

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

    @Test
    public void runPlayCharacterAsync_shouldRejectMissingPlayableCharacter() {
        GameHistory history = createPlayableCharacterGameHistory(new ArrayList<>());
        TestGameFlowListener listener = new TestGameFlowListener(history);
        GameHandler gameHandler = new GameHandler(history, listener);
        GamePhase currentPhase = new GamePhase(GamePhaseType.Actions, history.getConfig().getPlayers().get(0));

        assertThrows(
                IllegalArgumentException.class,
                () -> invokeRunPlayCharacterAsync(gameHandler, currentPhase, null)
        );

        assertEquals(0, listener.getFeedbackCount());
    }

    @Test
    public void runPlayCharacterAsync_shouldRequestActionInformation() {
        List<Character> characters = new ArrayList<>();
        GameHistory history = createPlayableCharacterGameHistory(characters);
        TestGameFlowListener listener = new TestGameFlowListener(history);

        InteractionResult result = new InteractionResult(
                InteractionResultType.CancelAction,
                new InteractionContext(characters.get(0)),
                null
        );
        listener.inputRequiredResults.add(result);

        GameHandler gameHandler = new GameHandler(history, listener);
        GamePhase currentPhase = new GamePhase(
                GamePhaseType.Actions,
                history.getConfig().getPlayers().get(0)
        );
        PlayableCharacter playableCharacter = new PlayableCharacter(
                characters.get(0),
                new Position(3, 3),
                false,
                false
        );

        invokeRunPlayCharacterAsync(gameHandler, currentPhase, playableCharacter).join();

        assertEquals(1, listener.getInputRequiredCount());

        InteractionRequest request = listener.getLastInputRequired();
        assertNotNull(request);
        assertEquals(InteractionType.PositionExpected, request.getRequestType());
        assertEquals(
                characters.get(0),
                request.getContext().getCharacter()
        );
        assertTrue(request.getLegalResults().contains(InteractionResultType.PositionChosen));
        assertTrue(request.getLegalResults().contains(InteractionResultType.CancelAction));

        assertEquals(0, listener.getFeedbackCount());
    }

    @Test
    public void runPlayCharacterAsync_shouldCancelWithoutApplyingAction() {
        List<Character> characters = new ArrayList<>();
        GameHistory history = createPlayableCharacterGameHistory(characters);
        TestGameFlowListener listener = new TestGameFlowListener(history);

        listener.inputRequiredResults.add(new InteractionResult(
                InteractionResultType.CancelAction,
                new InteractionContext(characters.get(0)),
                null
        ));

        GameHandler gameHandler = new GameHandler(history, listener);
        GamePhase currentPhase = new GamePhase(
                GamePhaseType.Actions,
                history.getConfig().getPlayers().get(0)
        );
        PlayableCharacter playableCharacter = new PlayableCharacter(
                characters.get(0),
                new Position(3, 3),
                false,
                false
        );

        ActionsPhase actionsPhase = (ActionsPhase)
                ((Turn) history.getEntries().get(0)).getSubPhase(GamePhaseType.Actions);

        gameHandler.getCurrentGame().getBoard().getCell(new Position(3, 3))
                .setCharacter(characters.get(0));

        invokeRunPlayCharacterAsync(gameHandler, currentPhase, playableCharacter).join();

        assertEquals(1, listener.getInputRequiredCount());
        assertTrue(actionsPhase.getActions().isEmpty());
        assertSame(
                characters.get(0),
                gameHandler.getCurrentGame().getBoard()
                        .getCell(new Position(3, 3))
                        .getCharacter()
        );
        assertNull(
                gameHandler.getCurrentGame().getBoard()
                        .getCell(new Position(3, 2))
                        .getCharacter()
        );

        assertEquals(0, listener.getFeedbackCount());
    }

    @Test
    public void runPlayCharacterAsync_shouldApplyCompletedAction() {
        List<Character> characters = new ArrayList<>();
        GameHistory history = createPlayableCharacterGameHistory(characters);
        TestGameFlowListener listener = new TestGameFlowListener(history);

        Position origin = new Position(3, 3);
        Position destination = new Position(3, 2);

        listener.inputRequiredResults.add(new InteractionResult(
                InteractionResultType.PositionChosen,
                new InteractionContext(characters.get(0)),
                new InteractionTarget(TargetCategory.MovementDestination, destination)
        ));

        GameHandler gameHandler = new GameHandler(history, listener);
        GamePhase currentPhase = new GamePhase(
                GamePhaseType.Actions,
                history.getConfig().getPlayers().get(0)
        );
        PlayableCharacter playableCharacter = new PlayableCharacter(
                characters.get(0),
                origin,
                false,
                false
        );

        ActionsPhase actionsPhase = (ActionsPhase)
                ((Turn) history.getEntries().get(0)).getSubPhase(GamePhaseType.Actions);

        invokeRunPlayCharacterAsync(gameHandler, currentPhase, playableCharacter).join();

        assertEquals(1, listener.getInputRequiredCount());
        assertEquals(1, actionsPhase.getActions().size());
        assertTrue(actionsPhase.getActions().get(0) instanceof CharacterAction);

        CharacterAction action = (CharacterAction) actionsPhase.getActions().get(0);
        assertSame(characters.get(0), action.getSrcCharacter());
        assertEquals(1, action.getMotions().size());
        assertEquals(origin, action.getMotions().get(0).getTargets().get(0).getOriginPos());
        assertEquals(destination, action.getMotions().get(0).getTargets().get(0).getDestPos());

        assertNull(
                gameHandler.getCurrentGame().getBoard()
                        .getCell(origin)
                        .getCharacter()
        );
        assertSame(
                characters.get(0),
                gameHandler.getCurrentGame().getBoard()
                        .getCell(destination)
                        .getCharacter()
        );

        assertEquals(1, listener.getFeedbackCount());
        assertNotNull(listener.getLastFeedback());
        assertEquals(1, listener.getLastFeedback().getCharacterActionMotions().size());
    }

    @Test
    public void runActionsPhaseAsync_shouldEndWhenEndPhaseIsSelected() throws Exception {
        List<Character> characters = new ArrayList<>();
        GameHistory history = createPlayableCharacterGameHistory(characters);
        TestGameFlowListener listener = new TestGameFlowListener(history);

        listener.inputRequiredResults.add(new InteractionResult(
                InteractionResultType.EndPhase,
                new InteractionContext(),
                null
        ));

        GameHandler gameHandler = new GameHandler(history, listener);
        GamePhase currentPhase = new GamePhase(
                GamePhaseType.Actions,
                history.getConfig().getPlayers().get(0)
        );

        invokeRunActionsPhase(gameHandler, currentPhase).join();

        assertEquals(1, listener.getInputRequiredCount());

        ActionsPhase actionsPhase = (ActionsPhase)
                ((Turn) history.getEntries().get(0)).getSubPhase(GamePhaseType.Actions);
        assertTrue(actionsPhase.getActions().isEmpty());
        assertFalse(actionsPhase.hasEnded());
    }

    @Test
    public void runActionsPhaseAsync_shouldNotifyListenerWhenActionIsUndone() throws Exception {
        List<Character> characters = new ArrayList<>();
        GameHistory history = createPlayableCharacterGameHistory(characters);

        Turn turn = (Turn) history.getEntries().get(0);
        ActionsPhase actionsPhase = (ActionsPhase) turn.getSubPhase(GamePhaseType.Actions);

        CharacterAction action = new CharacterAction(characters.get(0), new ArrayList<>());
        actionsPhase.getActions().add(action);

        TestGameFlowListener listener = new TestGameFlowListener(history);

        // First interaction: undo the existing action.
        listener.inputRequiredResults.add(
                new InteractionResult(
                        InteractionResultType.UndoLastAction,
                        new InteractionContext(),
                        null
                )
        );

        // Second interaction: stop the phase.
        listener.inputRequiredResults.add(
                new InteractionResult(
                        InteractionResultType.EndPhase,
                        new InteractionContext(),
                        null
                )
        );

        GameHandler gameHandler = new GameHandler(history, listener);
        GamePhase currentPhase = new GamePhase(
                GamePhaseType.Actions,
                history.getConfig().getPlayers().get(0)
        );

        invokeRunActionsPhase(gameHandler, currentPhase).join();

        // The listener must have been notified exactly once.
        assertEquals(1, listener.getActionUndoneCount());

        // The notified game must be the current game.
        assertSame(gameHandler.getCurrentGame(), listener.getLastActionUndoneGame());

        // The action must actually have been undone.
        assertTrue(actionsPhase.getActions().isEmpty());

        // The actions phase must have continued after the undo.
        assertEquals(2, listener.getInputRequiredCount());
    }


    @Test
    public void runActionsPhaseAsync_shouldResolveCharacterActionAndStartNextIteration() throws Exception {
        List<Character> characters = new ArrayList<>();
        GameHistory history = createPlayableCharacterGameHistory(characters);
        TestGameFlowListener listener = new TestGameFlowListener(history);

        PlayableCharacter acrobat = new PlayableCharacter(
                characters.get(0),
                new Position(3, 3),
                false,
                false
        );
        PlayableCharacter archer = new PlayableCharacter(
                characters.get(1),
                new Position(3, 4),
                false,
                false
        );


        listener.inputRequiredResults.add(
                new InteractionResult(
                        InteractionResultType.PlayableCharacterChosen,
                        new InteractionContext(),
                        new InteractionTarget(TargetCategory.PlayableCharacter, acrobat)
                )
        );
        listener.inputRequiredResults.add(
                new InteractionResult(
                        InteractionResultType.PositionChosen,
                        new InteractionContext(characters.get(0)),
                        new InteractionTarget(
                                TargetCategory.MovementDestination,
                                new Position(3, 2)
                        )
                )
        );
        listener.inputRequiredResults.add(
                new InteractionResult(
                        InteractionResultType.PlayableCharacterChosen,
                        new InteractionContext(),
                        new InteractionTarget(TargetCategory.PlayableCharacter, archer)
                )
        );
        listener.inputRequiredResults.add(
                new InteractionResult(
                        InteractionResultType.CancelAction,
                        new InteractionContext(characters.get(1)),
                        null
                )
        );
        listener.inputRequiredResults.add(
                new InteractionResult(
                        InteractionResultType.EndPhase,
                        new InteractionContext(),
                        null
                )
        );

        GameHandler gameHandler = new GameHandler(history, listener);
        GamePhase currentPhase = new GamePhase(
                GamePhaseType.Actions,
                history.getConfig().getPlayers().get(0)
        );

        invokeRunActionsPhase(gameHandler, currentPhase).join();

        Turn turn = (Turn) history.getEntries().get(0);
        ActionsPhase actionsPhase = (ActionsPhase) turn.getSubPhase(GamePhaseType.Actions);

        assertEquals(5, listener.getInputRequiredCount());
        assertEquals(1, actionsPhase.getActions().size());
        assertSame(characters.get(0), ((CharacterAction) actionsPhase.getActions().get(0)).getSrcCharacter());

        assertNull(gameHandler.getCurrentGame().getBoard()
                .getCell(new Position(3, 3)).getCharacter());
        assertSame(characters.get(0), gameHandler.getCurrentGame().getBoard()
                .getCell(new Position(3, 2)).getCharacter());
        assertSame(characters.get(1), gameHandler.getCurrentGame().getBoard()
                .getCell(new Position(3, 4)).getCharacter());
    }

    @Test
    public void runActionsPhaseAsync_shouldThrowWhenInteractionResultIsIllegal() throws Exception {
        List<Character> characters = new ArrayList<>();
        GameHistory history = createPlayableCharacterGameHistory(characters);
        TestGameFlowListener listener = new TestGameFlowListener(history);

        listener.inputRequiredResults.add(new InteractionResult(
                InteractionResultType.PositionChosen,
                new InteractionContext(),
                new InteractionTarget(
                        TargetCategory.MovementDestination,
                        new Position(3, 2)
                )
        ));

        GameHandler gameHandler = new GameHandler(history, listener);
        GamePhase currentPhase = new GamePhase(
                GamePhaseType.Actions,
                history.getConfig().getPlayers().get(0)
        );

        CompletableFuture<Void> result = invokeRunActionsPhase(gameHandler, currentPhase);

        try {
            result.join();
            fail("Expected IllegalStateException");
        } catch (java.util.concurrent.CompletionException exception) {
            assertTrue(exception.getCause() instanceof IllegalStateException);
        }

        assertEquals(1, listener.getInputRequiredCount());
    }

    @Test
    public void runSelectRecruitmentCardAsync_shouldRequestRecruitableCards() throws Exception {
        GameHistory history = createSelectRecruitmentCardGameHistory();
        TestGameFlowListener listener = new TestGameFlowListener(history);

        SelectableCharacterCard expectedCard = new SelectableCharacterCard(
                CharacterCard.Archer, CharacterCardSelectionStatus.Recruitable);
        listener.inputRequiredResults.add(new InteractionResult(
                InteractionResultType.SelectableCharacterCardChosen,
                new InteractionContext(),
                new InteractionTarget(TargetCategory.RecruitmentCard, expectedCard)
        ));

        GameHandler gameHandler = new GameHandler(history, listener);
        GamePhase currentPhase = new GamePhase(GamePhaseType.Recruitment, history.getConfig().getPlayers().get(0));

        SelectableCharacterCard result =
                invokeRunSelectRecruitmentCardAsync(gameHandler, currentPhase).join();

        assertSame(expectedCard, result);
        assertEquals(1, listener.getInputRequiredCount());

        InteractionRequest request = listener.getLastInputRequired();

        assertNotNull(request);
        assertEquals(InteractionType.SelectableCharacterCardExpected, request.getRequestType());
        assertEquals(
                Collections.singletonList(InteractionResultType.SelectableCharacterCardChosen),
                request.getLegalResults()
        );

        assertEquals(2, request.getLegalTargets().size());
        assertTrue(request.getLegalTargets().stream()
                .allMatch(target -> target.getCategory() == TargetCategory.RecruitmentCard));
        assertTrue(request.getLegalTargets().stream()
                .anyMatch(target -> expectedCard.equals(target.getChosenSelectableCharacterCard())));
        assertTrue(request.getLegalTargets().stream()
                .anyMatch(target -> {
                    SelectableCharacterCard selectableCharacterCard = target.getChosenSelectableCharacterCard();
                    assertNotNull(selectableCharacterCard);
                    return selectableCharacterCard.getCharacterCard() == CharacterCard.Bruiser &&
                            selectableCharacterCard.getSelectionStatus() == CharacterCardSelectionStatus.Recruitable;
                }));
    }

    @Test
    public void runSelectRecruitmentCardAsync_shouldRejectIllegalInteractionResult()
            throws Exception {
        GameHistory history = createSelectRecruitmentCardGameHistory();
        TestGameFlowListener listener = new TestGameFlowListener(history);

        listener.inputRequiredResults.add(new InteractionResult(
                InteractionResultType.CancelAction,
                new InteractionContext(),
                null
        ));

        GameHandler gameHandler = new GameHandler(history, listener);
        GamePhase currentPhase = new GamePhase(GamePhaseType.Recruitment, history.getConfig().getPlayers().get(0));

        CompletableFuture<SelectableCharacterCard> result = invokeRunSelectRecruitmentCardAsync(gameHandler, currentPhase);

        try {
            result.join();
            fail("Expected IllegalStateException");
        } catch (java.util.concurrent.CompletionException exception) {
            assertTrue(exception.getCause() instanceof IllegalStateException);
            assertEquals(
                    "Invalid interaction result : illegal type \"CancelAction\" for recruitment card selection",
                    exception.getCause().getMessage()
            );
        }
    }

    @Test
    public void runSelectRecruitmentCardAsync_shouldRejectMissingSelectedCard()
            throws Exception {
        GameHistory history = createSelectRecruitmentCardGameHistory();
        TestGameFlowListener listener = new TestGameFlowListener(history);

        listener.inputRequiredResults.add(new InteractionResult(
                InteractionResultType.SelectableCharacterCardChosen,
                new InteractionContext(),
                null
        ));

        GameHandler gameHandler = new GameHandler(history, listener);
        GamePhase currentPhase = new GamePhase(GamePhaseType.Recruitment, history.getConfig().getPlayers().get(0));

        CompletableFuture<SelectableCharacterCard> result =
                invokeRunSelectRecruitmentCardAsync(gameHandler, currentPhase);

        try {
            result.join();
            fail("Expected IllegalStateException");
        } catch (java.util.concurrent.CompletionException exception) {
            assertTrue(exception.getCause() instanceof IllegalStateException);
            assertEquals(
                    "Invalid interaction result : chosen card missing for recruitment",
                    exception.getCause().getMessage()
            );
        }
    }

    @Test
    public void runSelectRecruitmentCardAsync_shouldRejectWrongTargetCategory()
            throws Exception {
        GameHistory history = createSelectRecruitmentCardGameHistory();
        TestGameFlowListener listener = new TestGameFlowListener(history);

        listener.inputRequiredResults.add(new InteractionResult(
                InteractionResultType.SelectableCharacterCardChosen,
                new InteractionContext(),
                new InteractionTarget(
                        TargetCategory.RecruitmentDestination, // Should be RecruitmentCard
                        new SelectableCharacterCard(
                                CharacterCard.Archer,
                                CharacterCardSelectionStatus.Recruitable
                        )
                )
        ));

        GameHandler gameHandler = new GameHandler(history, listener);
        GamePhase currentPhase = new GamePhase(GamePhaseType.Recruitment, history.getConfig().getPlayers().get(0));

        CompletableFuture<SelectableCharacterCard> result =
                invokeRunSelectRecruitmentCardAsync(gameHandler, currentPhase);

        try {
            result.join();
            fail("Expected IllegalStateException");
        } catch (java.util.concurrent.CompletionException exception) {
            assertTrue(exception.getCause() instanceof IllegalStateException);
            assertEquals(
                    "Invalid interaction result : chosen card missing for recruitment",
                    exception.getCause().getMessage()
            );
        }
    }

    @Test
    public void runSelectRecruitmentCardAsync_shouldUndoLastActionAndRequestAnotherCard() throws Exception {
        GameHistory history = createRecruitCardGameHistory();
        TestGameFlowListener listener = new TestGameFlowListener(history);

        Turn turn = (Turn) history.getEntries().get(0);
        RecruitmentPhase recruitmentPhase =
                (RecruitmentPhase) turn.getSubPhase(GamePhaseType.Recruitment);

        Character archer = Character.create(CharacterType.Archer, TeamColor.Black);
        Position recruitmentPosition = new Position(3, 2);

        RecruitmentAction recruitmentAction = new RecruitmentAction(Collections.singletonList(
                new RecruitmentActionMotion(
                        RecruitmentMotionType.Add,
                        archer,
                        recruitmentPosition
                )
        ));

        recruitmentPhase.getActions().add(recruitmentAction);

        GameHandler gameHandler = new GameHandler(history, listener);
        GamePhase currentPhase = new GamePhase(
                GamePhaseType.Recruitment,
                history.getConfig().getPlayers().get(0)
        );

        SelectableCharacterCard selectedCard = new SelectableCharacterCard(
                CharacterCard.Acrobat,
                CharacterCardSelectionStatus.Recruitable
        );

        listener.inputRequiredResults.add(new InteractionResult(
                InteractionResultType.UndoLastAction,
                new InteractionContext(),
                null
        ));

        listener.inputRequiredResults.add(new InteractionResult(
                InteractionResultType.SelectableCharacterCardChosen,
                new InteractionContext(),
                new InteractionTarget(
                        TargetCategory.RecruitmentCard,
                        selectedCard
                )
        ));

        SelectableCharacterCard result =
                invokeRunSelectRecruitmentCardAsync(gameHandler, currentPhase).join();

        assertEquals(selectedCard, result);
        assertEquals(2, listener.getInputRequiredCount());
        assertTrue(recruitmentPhase.getActions().isEmpty());
    }

    @Test
    public void runRecruitCardAsync_shouldApplyCompletedRecruitment() {
        GameHistory history = createRecruitCardGameHistory();
        TestGameFlowListener listener = new TestGameFlowListener(history);

        Position recruitmentPosition = new Position(3, 2);
        Character archer = Character.create(CharacterType.Archer, TeamColor.Black);
        listener.inputRequiredResults.add(new InteractionResult(
                InteractionResultType.PositionChosen,
                new InteractionContext(archer),
                new InteractionTarget(
                        TargetCategory.RecruitmentDestination,
                        recruitmentPosition
                )
        ));

        GameHandler gameHandler = new GameHandler(history, listener);
        GamePhase currentPhase = new GamePhase(
                GamePhaseType.Recruitment,
                history.getConfig().getPlayers().get(0)
        );

        invokeRunRecruitCardAsync(gameHandler, currentPhase, CharacterCard.Archer).join();

        RecruitmentPhase recruitmentPhase = (RecruitmentPhase)
                ((Turn) history.getEntries().get(0)).getSubPhase(GamePhaseType.Recruitment);

        assertEquals(1, listener.getInputRequiredCount());
        assertEquals(1, recruitmentPhase.getActions().size());
        assertTrue(recruitmentPhase.getActions().get(0) instanceof RecruitmentAction);

        RecruitmentAction action = (RecruitmentAction) recruitmentPhase.getActions().get(0);
        assertEquals(1, action.getMotions().size());
        assertEquals(CharacterType.Archer,
                action.getMotions().get(0).getCharacter().getCharacterType());
        assertEquals(recruitmentPosition,
                action.getMotions().get(0).getPosition());

        assertEquals(CharacterType.Archer,
                gameHandler.getCurrentGame().getBoard()
                        .getCell(recruitmentPosition)
                        .getCharacter()
                        .getCharacterType());
    }

    @Test
    public void runRecruitCardAsync_shouldCancelRecruitmentWithoutApplyingAction() {
        GameHistory history = createRecruitCardGameHistory();
        TestGameFlowListener listener = new TestGameFlowListener(history);

        listener.inputRequiredResults.add(new InteractionResult(
                InteractionResultType.CancelAction,
                new InteractionContext(),
                null
        ));

        GameHandler gameHandler = new GameHandler(history, listener);
        GamePhase currentPhase = new GamePhase(
                GamePhaseType.Recruitment,
                history.getConfig().getPlayers().get(0)
        );

        invokeRunRecruitCardAsync(gameHandler, currentPhase, CharacterCard.Archer).join();

        RecruitmentPhase recruitmentPhase = (RecruitmentPhase)
                ((Turn) history.getEntries().get(0)).getSubPhase(GamePhaseType.Recruitment);

        assertEquals(1, listener.getInputRequiredCount());
        assertTrue(recruitmentPhase.getActions().isEmpty());
        assertEquals(0, listener.getFeedbackCount());
    }

    @Test
    public void runRecruitCardAsync_shouldHandlePartialRecruitmentCancellation() {
        GameHistory history = createRecruitCardGameHistory();
        TestGameFlowListener listener = new TestGameFlowListener(history);

        Character hermit = Character.create(CharacterType.Hermit, TeamColor.Black);
        Position firstRecruitmentPosition = new Position(3, 2);

        listener.inputRequiredResults.add(new InteractionResult(
                InteractionResultType.PositionChosen,
                new InteractionContext(hermit),
                new InteractionTarget(
                        TargetCategory.RecruitmentDestination,
                        firstRecruitmentPosition
                )
        ));

        listener.inputRequiredResults.add(new InteractionResult(
                InteractionResultType.CancelAction,
                new InteractionContext(),
                null
        ));

        GameHandler gameHandler = new GameHandler(history, listener);
        GamePhase currentPhase = new GamePhase(
                GamePhaseType.Recruitment,
                history.getConfig().getPlayers().get(0)
        );

        invokeRunRecruitCardAsync(
                gameHandler,
                currentPhase,
                CharacterCard.HermitAndCub
        ).join();

        RecruitmentPhase recruitmentPhase = (RecruitmentPhase)
                ((Turn) history.getEntries().get(0)).getSubPhase(GamePhaseType.Recruitment);

        assertEquals(2, listener.getInputRequiredCount());
        assertEquals(2, listener.getFeedbackCount());
        assertNotNull(listener.getLastFeedback());
        assertTrue(recruitmentPhase.getActions().isEmpty());
    }

    @Test
    public void runRecruitCardAsync_shouldRecruitTwoCharactersForHermitAndCub() {
        GameHistory history = createRecruitCardGameHistory();
        TestGameFlowListener listener = new TestGameFlowListener(history);

        Character hermit = Character.create(CharacterType.Hermit, TeamColor.Black);
        Position firstRecruitmentPosition = new Position(3, 2);
        Character cub = Character.create(CharacterType.Cub, TeamColor.Black);
        Position secondRecruitmentPosition = new Position(4, 2);

        listener.inputRequiredResults.add(new InteractionResult(
                InteractionResultType.PositionChosen,
                new InteractionContext(hermit),
                new InteractionTarget(
                        TargetCategory.RecruitmentDestination,
                        firstRecruitmentPosition
                )
        ));

        listener.inputRequiredResults.add(new InteractionResult(
                InteractionResultType.PositionChosen,
                new InteractionContext(cub),
                new InteractionTarget(
                        TargetCategory.RecruitmentDestination,
                        secondRecruitmentPosition
                )
        ));

        GameHandler gameHandler = new GameHandler(history, listener);
        GamePhase currentPhase = new GamePhase(
                GamePhaseType.Recruitment,
                history.getConfig().getPlayers().get(0)
        );

        invokeRunRecruitCardAsync(
                gameHandler,
                currentPhase,
                CharacterCard.HermitAndCub
        ).join();

        RecruitmentPhase recruitmentPhase = (RecruitmentPhase)
                ((Turn) history.getEntries().get(0)).getSubPhase(GamePhaseType.Recruitment);

        assertEquals(2, listener.getInputRequiredCount());
        assertEquals(1, recruitmentPhase.getActions().size());

        RecruitmentAction action =
                (RecruitmentAction) recruitmentPhase.getActions().get(0);

        assertEquals(2, action.getMotions().size());
        assertEquals(firstRecruitmentPosition, action.getMotions().get(0).getPosition());
        assertEquals(secondRecruitmentPosition, action.getMotions().get(1).getPosition());
    }

    @Test
    public void runRecruitmentPhaseAsync_shouldApplyCompletedRecruitment() {
        GameHistory history = createRecruitCardGameHistory();
        TestGameFlowListener listener = new TestGameFlowListener(history);

        SelectableCharacterCard selectedCard = new SelectableCharacterCard(
                CharacterCard.Archer,
                CharacterCardSelectionStatus.Recruitable
        );
        Position recruitmentPosition = new Position(3, 2);
        Character archer = Character.create(CharacterType.Archer, TeamColor.Black);

        listener.inputRequiredResults.add(new InteractionResult(
                InteractionResultType.SelectableCharacterCardChosen,
                new InteractionContext(),
                new InteractionTarget(TargetCategory.RecruitmentCard, selectedCard)
        ));

        listener.inputRequiredResults.add(new InteractionResult(
                InteractionResultType.PositionChosen,
                new InteractionContext(archer),
                new InteractionTarget(
                        TargetCategory.RecruitmentDestination,
                        recruitmentPosition
                )
        ));

        GameHandler gameHandler = new GameHandler(history, listener);
        GamePhase currentPhase = new GamePhase(
                GamePhaseType.Recruitment,
                history.getConfig().getPlayers().get(0)
        );

        invokeRunRecruitmentPhaseAsync(gameHandler, currentPhase).join();

        RecruitmentPhase recruitmentPhase = (RecruitmentPhase)
                ((Turn) history.getEntries().get(0)).getSubPhase(GamePhaseType.Recruitment);

        assertEquals(2, listener.getInputRequiredCount());
        assertEquals(1, recruitmentPhase.getActions().size());
        assertTrue(recruitmentPhase.getActions().get(0) instanceof RecruitmentAction);

        RecruitmentAction action = (RecruitmentAction) recruitmentPhase.getActions().get(0);
        assertEquals(1, action.getMotions().size());
        assertEquals(CharacterType.Archer,
                action.getMotions().get(0).getCharacter().getCharacterType());
        assertEquals(recruitmentPosition,
                action.getMotions().get(0).getPosition());

        assertEquals(CharacterType.Archer,
                gameHandler.getCurrentGame().getBoard()
                        .getCell(recruitmentPosition)
                        .getCharacter()
                        .getCharacterType());
    }

    @Test
    public void runRecruitmentPhaseAsync_shouldApplyMultipleCharacterRecruitment() {
        GameHistory history = createRecruitmentPhaseGameHistory();
        TestGameFlowListener listener = new TestGameFlowListener(history);

        SelectableCharacterCard selectedCard = new SelectableCharacterCard(
                CharacterCard.HermitAndCub,
                CharacterCardSelectionStatus.Recruitable
        );

        Character hermit = Character.create(CharacterType.Hermit, TeamColor.Black);
        Character cub = Character.create(CharacterType.Cub, TeamColor.Black);

        Position firstRecruitmentPosition = new Position(3, 2);
        Position secondRecruitmentPosition = new Position(4, 2);

        listener.inputRequiredResults.add(new InteractionResult(
                InteractionResultType.SelectableCharacterCardChosen,
                new InteractionContext(),
                new InteractionTarget(TargetCategory.RecruitmentCard, selectedCard)
        ));

        listener.inputRequiredResults.add(new InteractionResult(
                InteractionResultType.PositionChosen,
                new InteractionContext(hermit),
                new InteractionTarget(
                        TargetCategory.RecruitmentDestination,
                        firstRecruitmentPosition
                )
        ));

        listener.inputRequiredResults.add(new InteractionResult(
                InteractionResultType.PositionChosen,
                new InteractionContext(cub),
                new InteractionTarget(
                        TargetCategory.RecruitmentDestination,
                        secondRecruitmentPosition
                )
        ));

        GameHandler gameHandler = new GameHandler(history, listener);
        GamePhase currentPhase = new GamePhase(
                GamePhaseType.Recruitment,
                history.getConfig().getPlayers().get(0)
        );

        invokeRunRecruitmentPhaseAsync(gameHandler, currentPhase).join();

        RecruitmentPhase recruitmentPhase = (RecruitmentPhase)
                ((Turn) history.getEntries().get(0)).getSubPhase(GamePhaseType.Recruitment);

        assertEquals(3, listener.getInputRequiredCount());
        assertEquals(1, recruitmentPhase.getActions().size());
        assertTrue(recruitmentPhase.getActions().get(0) instanceof RecruitmentAction);

        RecruitmentAction action = (RecruitmentAction) recruitmentPhase.getActions().get(0);

        assertEquals(2, action.getMotions().size());
        assertEquals(CharacterType.Hermit,
                action.getMotions().get(0).getCharacter().getCharacterType());
        assertEquals(firstRecruitmentPosition,
                action.getMotions().get(0).getPosition());
        assertEquals(CharacterType.Cub,
                action.getMotions().get(1).getCharacter().getCharacterType());
        assertEquals(secondRecruitmentPosition,
                action.getMotions().get(1).getPosition());

        assertEquals(CharacterType.Hermit,
                gameHandler.getCurrentGame().getBoard()
                        .getCell(firstRecruitmentPosition)
                        .getCharacter()
                        .getCharacterType());
        assertEquals(CharacterType.Cub,
                gameHandler.getCurrentGame().getBoard()
                        .getCell(secondRecruitmentPosition)
                        .getCharacter()
                        .getCharacterType());
    }

    @Test
    public void runRecruitmentPhaseAsync_shouldRestartCardSelectionAfterCancellation() {
        GameHistory history = createRecruitCardGameHistory();
        TestGameFlowListener listener = new TestGameFlowListener(history);

        SelectableCharacterCard cancelledCard = new SelectableCharacterCard(
                CharacterCard.Archer,
                CharacterCardSelectionStatus.Recruitable
        );
        SelectableCharacterCard selectedCard = new SelectableCharacterCard(
                CharacterCard.Acrobat,
                CharacterCardSelectionStatus.Recruitable
        );

        Character acrobat = Character.create(CharacterType.Acrobat, TeamColor.Black);
        Position recruitmentPosition = new Position(3, 2);

        // First recruitment attempt: select Archer, then cancel.
        listener.inputRequiredResults.add(new InteractionResult(
                InteractionResultType.SelectableCharacterCardChosen,
                new InteractionContext(),
                new InteractionTarget(TargetCategory.RecruitmentCard, cancelledCard)
        ));

        listener.inputRequiredResults.add(new InteractionResult(
                InteractionResultType.CancelAction,
                new InteractionContext(),
                null
        ));

        // Second recruitment attempt: select Acrobat and complete it.
        listener.inputRequiredResults.add(new InteractionResult(
                InteractionResultType.SelectableCharacterCardChosen,
                new InteractionContext(),
                new InteractionTarget(TargetCategory.RecruitmentCard, selectedCard)
        ));

        listener.inputRequiredResults.add(new InteractionResult(
                InteractionResultType.PositionChosen,
                new InteractionContext(acrobat),
                new InteractionTarget(
                        TargetCategory.RecruitmentDestination,
                        recruitmentPosition
                )
        ));

        GameHandler gameHandler = new GameHandler(history, listener);
        GamePhase currentPhase = new GamePhase(
                GamePhaseType.Recruitment,
                history.getConfig().getPlayers().get(0)
        );

        invokeRunRecruitmentPhaseAsync(gameHandler, currentPhase).join();

        RecruitmentPhase recruitmentPhase = (RecruitmentPhase)
                ((Turn) history.getEntries().get(0)).getSubPhase(GamePhaseType.Recruitment);

        assertEquals(4, listener.getInputRequiredCount());
        assertEquals(1, recruitmentPhase.getActions().size());
        assertTrue(recruitmentPhase.getActions().get(0) instanceof RecruitmentAction);

        RecruitmentAction action = (RecruitmentAction) recruitmentPhase.getActions().get(0);

        assertEquals(1, action.getMotions().size());
        assertEquals(CharacterType.Acrobat,
                action.getMotions().get(0).getCharacter().getCharacterType());
        assertEquals(recruitmentPosition,
                action.getMotions().get(0).getPosition());

        assertEquals(CharacterType.Acrobat,
                gameHandler.getCurrentGame().getBoard()
                        .getCell(recruitmentPosition)
                        .getCharacter()
                        .getCharacterType());
    }

    @Test
    public void runBanishmentPhaseAsync_shouldRequestAndApplySelectedCard() {
        GameHistory history = createGameHistory(
                GameMode.Strategist,
                createPlayers(),
                Collections.singletonList(CharacterCard.Archer)
        );
        BanishmentPhase banishmentPhase = new BanishmentPhase(TeamColor.Black);
        banishmentPhase.start();
        history.getEntries().add(banishmentPhase);

        TestGameFlowListener listener = new TestGameFlowListener(history);
        SelectableCharacterCard selectedCard = new SelectableCharacterCard(
                CharacterCard.Archer,
                CharacterCardSelectionStatus.Banishable
        );
        listener.inputRequiredResults.add(new InteractionResult(
                InteractionResultType.SelectableCharacterCardChosen,
                new InteractionContext(),
                new InteractionTarget(TargetCategory.BanishmentCard, selectedCard)
        ));

        GameHandler gameHandler = new GameHandler(history, listener);
        GamePhase currentPhase = new GamePhase(
                GamePhaseType.Banishment,
                history.getConfig().getPlayers().get(0)
        );

        invokeRunBanishmentPhaseAsync(gameHandler, currentPhase).join();

        assertEquals(1, listener.getInputRequiredCount());
        assertEquals(InteractionType.SelectableCharacterCardExpected,
                listener.getLastInputRequired().getRequestType());
        assertEquals(1, listener.getLastInputRequired().getLegalResults().size());
        assertEquals(InteractionResultType.SelectableCharacterCardChosen,
                listener.getLastInputRequired().getLegalResults().get(0));
        assertEquals(1, listener.getLastInputRequired().getLegalTargets().size());
        assertEquals(TargetCategory.BanishmentCard,
                listener.getLastInputRequired().getLegalTargets().get(0).getCategory());
        assertEquals(1, banishmentPhase.getActions().size());
        assertTrue(banishmentPhase.getActions().get(0) instanceof BanishmentAction);

        BanishmentAction action = (BanishmentAction) banishmentPhase.getActions().get(0);
        assertEquals(CharacterCard.Archer, action.getCharacterCard());
        assertEquals(TeamColor.Black, action.getTeamColor());
        assertFalse(gameHandler.getCurrentGame().getRecruitableCards().contains(CharacterCard.Archer));
        assertTrue(gameHandler.getCurrentGame().getBanishedCards(TeamColor.Black).contains(CharacterCard.Archer));
    }

    @Test
    public void runBanishmentPhaseAsync_shouldRejectIllegalInteractionResult() {
        GameHistory history = createGameHistory(
                GameMode.Strategist,
                createPlayers(),
                Collections.singletonList(CharacterCard.Archer)
        );
        BanishmentPhase banishmentPhase = new BanishmentPhase(TeamColor.Black);
        banishmentPhase.start();
        history.getEntries().add(banishmentPhase);

        TestGameFlowListener listener = new TestGameFlowListener(history);
        listener.inputRequiredResults.add(new InteractionResult(
                InteractionResultType.CancelAction,
                new InteractionContext(),
                null
        ));

        GameHandler gameHandler = new GameHandler(history, listener);
        GamePhase currentPhase = new GamePhase(
                GamePhaseType.Banishment,
                history.getConfig().getPlayers().get(0)
        );

        try {
            invokeRunBanishmentPhaseAsync(gameHandler, currentPhase).join();
            fail("Expected IllegalStateException");
        } catch (CompletionException exception) {
            assertTrue(exception.getCause() instanceof IllegalStateException);
        }
        assertTrue(banishmentPhase.getActions().isEmpty());
    }

    @Test
    public void runBanishmentPhaseAsync_shouldRejectMissingSelectedCard() {
        GameHistory history = createGameHistory(
                GameMode.Strategist,
                createPlayers(),
                Collections.singletonList(CharacterCard.Archer)
        );
        BanishmentPhase banishmentPhase = new BanishmentPhase(TeamColor.Black);
        banishmentPhase.start();
        history.getEntries().add(banishmentPhase);

        TestGameFlowListener listener = new TestGameFlowListener(history);
        listener.inputRequiredResults.add(new InteractionResult(
                InteractionResultType.SelectableCharacterCardChosen,
                new InteractionContext(),
                null
        ));

        GameHandler gameHandler = new GameHandler(history, listener);
        GamePhase currentPhase = new GamePhase(
                GamePhaseType.Banishment,
                history.getConfig().getPlayers().get(0)
        );

        try {
            invokeRunBanishmentPhaseAsync(gameHandler, currentPhase).join();
            fail("Expected IllegalStateException");
        } catch (CompletionException exception) {
            assertTrue(exception.getCause() instanceof IllegalStateException);
        }
        assertTrue(banishmentPhase.getActions().isEmpty());
    }

    @Test
    public void runTurnEndPhaseAsync_shouldRemoveBarrageWarningWhenNoBarrageIsDetected() {
        GameHistory history = createTurnEndGameHistory(false, 1);
        GameHandler gameHandler = new GameHandler(history, new TestGameFlowListener(history));
        GamePhase currentPhase = new GamePhase(
                GamePhaseType.TurnEnd,
                history.getConfig().getPlayers().get(0)
        );

        invokeRunTurnEndPhaseAsync(gameHandler, currentPhase).join();

        assertEquals(0, gameHandler.getCurrentGame()
                .getPlayerWarningCount(TeamColor.Black, WarningType.Barrage));

        TurnEndPhase turnEndPhase = (TurnEndPhase) ((Turn) history.getEntries().get(0))
                .getSubPhase(GamePhaseType.TurnEnd);
        assertEquals(2, turnEndPhase.getActions().size());
        assertTrue(turnEndPhase.getActions().get(1) instanceof WarningAction);
        assertEquals(-1, ((WarningAction) turnEndPhase.getActions().get(1)).getCountChange());
    }

    @Test
    public void runTurnEndPhaseAsync_shouldAddBarrageWarningWhenBarrageIsDetected() {
        GameHistory history = createTurnEndGameHistory(true, 0);
        GameHandler gameHandler = new GameHandler(history, new TestGameFlowListener(history));
        GamePhase currentPhase = new GamePhase(
                GamePhaseType.TurnEnd,
                history.getConfig().getPlayers().get(0)
        );

        invokeRunTurnEndPhaseAsync(gameHandler, currentPhase).join();

        assertEquals(1, gameHandler.getCurrentGame()
                .getPlayerWarningCount(TeamColor.Black, WarningType.Barrage));

        TurnEndPhase turnEndPhase = (TurnEndPhase) ((Turn) history.getEntries().get(0))
                .getSubPhase(GamePhaseType.TurnEnd);
        assertEquals(1, turnEndPhase.getActions().size());
        assertTrue(turnEndPhase.getActions().get(0) instanceof WarningAction);
        assertEquals(1, ((WarningAction) turnEndPhase.getActions().get(0)).getCountChange());
    }

    @Test
    public void runTurnEndPhaseAsync_shouldNotRemoveWarningWhenNoWarningExists() {
        GameHistory history = createTurnEndGameHistory(false, 0);
        GameHandler gameHandler = new GameHandler(history, new TestGameFlowListener(history));
        GamePhase currentPhase = new GamePhase(
                GamePhaseType.TurnEnd,
                history.getConfig().getPlayers().get(0)
        );

        invokeRunTurnEndPhaseAsync(gameHandler, currentPhase).join();

        assertEquals(0, gameHandler.getCurrentGame()
                .getPlayerWarningCount(TeamColor.Black, WarningType.Barrage));

        TurnEndPhase turnEndPhase = (TurnEndPhase) ((Turn) history.getEntries().get(0))
                .getSubPhase(GamePhaseType.TurnEnd);
        assertTrue(turnEndPhase.getActions().isEmpty());
    }

    @Test
    public void checkGameEnded_shouldNotThrowWhenThereIsNoWinner() throws Exception {
        GameHistory history = createCheckGameEndedGameHistory();
        GameHandler gameHandler = new GameHandler(history, new TestGameFlowListener(history));

        GamePhase currentPhase = new GamePhase(
                GamePhaseType.TurnEnd,
                history.getConfig().getPlayers().get(0)
        );

        invokeCheckGameEnded(gameHandler, currentPhase);
    }

    @Test
    public void checkGameEnded_shouldSignalWinnerWhenThereIsAWinner() throws Exception {
        List<Player> players = createPlayers();
        List<IGameAction> initialPlacements = Collections.singletonList(
                new WarningAction(WarningType.Barrage, TeamColor.Black, 2)
        );

        GameConfig config = new GameConfig(
                players,
                players.get(0),
                GameMode.Discovery,
                Collections.emptyList(),
                initialPlacements
        );
        GameHistory history = new GameHistory(config, new ArrayList<>());
        GameHandler gameHandler = new GameHandler(history, new TestGameFlowListener(history));

        GamePhase currentPhase = new GamePhase(
                GamePhaseType.TurnEnd,
                players.get(0)
        );

        try {
            invokeCheckGameEnded(gameHandler, currentPhase);
            fail("Expected GameEndedException");
        } catch (Exception exception) {
            assertEquals("GameEndedException", exception.getClass().getSimpleName());
            Method getWinner = exception.getClass().getDeclaredMethod("getWinner");
            getWinner.setAccessible(true);

            assertSame(players.get(1), getWinner.invoke(exception));
        }
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

    @SuppressWarnings("unchecked")
    private CompletableFuture<Void> invokeRunPlayCharacterAsync(GameHandler gameHandler,
                                                                GamePhase currentPhase,
                                                                PlayableCharacter playableCharacter) {
        try {
            Method method = GameHandler.class.getDeclaredMethod(
                    "runPlayCharacterAsync",
                    GamePhase.class,
                    PlayableCharacter.class
            );
            method.setAccessible(true);

            return (CompletableFuture<Void>) method.invoke(
                    gameHandler,
                    currentPhase,
                    playableCharacter
            );
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
    private CompletableFuture<Void> invokeRunActionsPhase(GameHandler gameHandler,
                                                          GamePhase currentPhase) throws Exception {
        Method method = GameHandler.class.getDeclaredMethod(
                "runActionsPhaseAsync",
                GamePhase.class
        );
        method.setAccessible(true);

        try {
            return (CompletableFuture<Void>) method.invoke(gameHandler, currentPhase);
        } catch (InvocationTargetException exception) {
            Throwable cause = exception.getCause();
            if (cause instanceof Exception) {
                throw (Exception) cause;
            }
            throw exception;
        }
    }

    @SuppressWarnings("unchecked")
    private CompletableFuture<SelectableCharacterCard> invokeRunSelectRecruitmentCardAsync(
            GameHandler gameHandler, @NonNull GamePhase currentPhase) throws Exception {
        Method method = GameHandler.class.getDeclaredMethod(
                "runSelectRecruitmentCardAsync",
                GamePhase.class
        );
        method.setAccessible(true);

        try {
            CompletableFuture<SelectableCharacterCard> result =
                    (CompletableFuture<SelectableCharacterCard>) method.invoke(gameHandler, currentPhase);
            assertNotNull(result);
            return result;
        } catch (InvocationTargetException exception) {
            Throwable cause = exception.getCause();
            if (cause instanceof Exception) {
                throw (Exception) cause;
            }
            throw exception;
        }
    }

    @SuppressWarnings("unchecked")
    private CompletableFuture<Void> invokeRunRecruitCardAsync(GameHandler gameHandler,
                                                              GamePhase currentPhase,
                                                              CharacterCard recruitedCard) {
        try {
            Method method = GameHandler.class.getDeclaredMethod(
                    "runRecruitCardAsync",
                    GamePhase.class,
                    CharacterCard.class
            );
            method.setAccessible(true);

            return (CompletableFuture<Void>) method.invoke(
                    gameHandler,
                    currentPhase,
                    recruitedCard
            );
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
    private CompletableFuture<Void> invokeRunRecruitmentPhaseAsync(
            GameHandler gameHandler,
            GamePhase currentPhase) {
        try {
            Method method = GameHandler.class.getDeclaredMethod(
                    "runRecruitmentPhaseAsync",
                    GamePhase.class
            );
            method.setAccessible(true);

            return (CompletableFuture<Void>) method.invoke(
                    gameHandler,
                    currentPhase
            );
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
    private CompletableFuture<Void> invokeRunBanishmentPhaseAsync(
            GameHandler gameHandler,
            GamePhase currentPhase) {
        try {
            Method method = GameHandler.class.getDeclaredMethod("runBanishmentPhaseAsync", GamePhase.class);
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
    private CompletableFuture<Void> invokeRunTurnEndPhaseAsync(GameHandler gameHandler,
                                                               GamePhase currentPhase) {
        try {
            Method method = GameHandler.class.getDeclaredMethod(
                    "runTurnEndPhaseAsync",
                    GamePhase.class
            );
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

    private void invokeCheckGameEnded(GameHandler gameHandler,
                                      GamePhase currentPhase) throws Exception {
        Method method = GameHandler.class.getDeclaredMethod(
                "checkGameEnded",
                GamePhase.class
        );
        method.setAccessible(true);

        try {
            method.invoke(gameHandler, currentPhase);
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
        Character leaderBlack = Character.create(CharacterType.LeaderKing, TeamColor.Black);
        Character leaderWhite = Character.create(CharacterType.LeaderQueen, TeamColor.White);
        RecruitmentAction initialPlacement = new RecruitmentAction(Arrays.asList(
                new RecruitmentActionMotion(RecruitmentMotionType.Add, leaderBlack, new Position(3, 0)),
                new RecruitmentActionMotion(RecruitmentMotionType.Add, leaderWhite, new Position(3, 6))
        ));

        GameConfig config = new GameConfig(
                players,
                players.get(0),
                gameMode,
                initialRecruitableCards,
                List.of(initialPlacement)
        );
        return new GameHistory(config, new ArrayList<>());
    }

    private GameHistory createPlayableCharacterGameHistory(@NonNull List<Character> characters) {
        List<Player> players = createPlayers();

        Character leaderBlack = Character.create(CharacterType.LeaderKing, TeamColor.Black);
        Character leaderWhite = Character.create(CharacterType.LeaderQueen, TeamColor.White);
        Character acrobat = Character.create(CharacterType.Acrobat, TeamColor.Black);
        Character archer = Character.create(CharacterType.Archer, TeamColor.Black);

        characters.add(acrobat);
        characters.add(archer);

        RecruitmentAction initialPlacement = new RecruitmentAction(Arrays.asList(
                new RecruitmentActionMotion(RecruitmentMotionType.Add, leaderBlack, new Position(3, 0)),
                new RecruitmentActionMotion(RecruitmentMotionType.Add, leaderWhite, new Position(3, 6)),
                new RecruitmentActionMotion(RecruitmentMotionType.Add, acrobat, new Position(3, 3)),
                new RecruitmentActionMotion(RecruitmentMotionType.Add, archer, new Position(3, 4))
        ));

        GameConfig config = new GameConfig(
                players,
                players.get(0),
                GameMode.Discovery,
                Arrays.asList(CharacterCard.Bruiser, CharacterCard.HermitAndCub),
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

    private GameHistory createSelectRecruitmentCardGameHistory() {
        GameHistory history = createGameHistory(
                GameMode.Discovery,
                createPlayers(),
                Arrays.asList(CharacterCard.Archer, CharacterCard.Bruiser)
        );

        Turn turn = new Turn(TeamColor.Black);
        history.getEntries().add(turn);
        turn.getSubPhase(GamePhaseType.TurnStart).start();
        turn.getSubPhase(GamePhaseType.TurnStart).end();
        turn.getSubPhase(GamePhaseType.Actions).start();
        turn.getSubPhase(GamePhaseType.Actions).end();
        turn.getSubPhase(GamePhaseType.Recruitment).start();

        return history;
    }


    private GameHistory createRecruitCardGameHistory() {
        List<Player> players = createPlayers();

        Character leaderBlack = Character.create(CharacterType.LeaderKing, TeamColor.Black);
        Character leaderWhite = Character.create(CharacterType.LeaderQueen, TeamColor.White);

        RecruitmentAction initialPlacement = new RecruitmentAction(Arrays.asList(
                new RecruitmentActionMotion(RecruitmentMotionType.Add, leaderBlack, new Position(3, 0)),
                new RecruitmentActionMotion(RecruitmentMotionType.Add, leaderWhite, new Position(3, 6))
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
        turn.getSubPhase(GamePhaseType.Actions).end();
        turn.getSubPhase(GamePhaseType.Recruitment).start();

        return history;
    }

    private GameHistory createRecruitmentPhaseGameHistory() {
        List<Player> players = createPlayers();

        Character leaderBlack = Character.create(CharacterType.LeaderKing, TeamColor.Black);
        Character leaderWhite = Character.create(CharacterType.LeaderQueen, TeamColor.White);

        RecruitmentAction initialPlacement = new RecruitmentAction(Arrays.asList(
                new RecruitmentActionMotion(RecruitmentMotionType.Add, leaderBlack, new Position(3, 0)),
                new RecruitmentActionMotion(RecruitmentMotionType.Add, leaderWhite, new Position(3, 6))
        ));

        GameConfig config = new GameConfig(
                players,
                players.get(0),
                GameMode.Discovery,
                Collections.singletonList(CharacterCard.HermitAndCub),
                Collections.singletonList(initialPlacement)
        );

        GameHistory history = new GameHistory(config, new ArrayList<>());

        Turn turn = new Turn(TeamColor.Black);
        history.getEntries().add(turn);

        turn.getSubPhase(GamePhaseType.TurnStart).start();
        turn.getSubPhase(GamePhaseType.TurnStart).end();
        turn.getSubPhase(GamePhaseType.Actions).start();
        turn.getSubPhase(GamePhaseType.Actions).end();
        turn.getSubPhase(GamePhaseType.Recruitment).start();

        return history;
    }

    private GameHistory createTurnEndGameHistory(boolean barrageDetected, int initialWarningCount) {
        List<Player> players = createPlayers();
        List<IGameAction> initialPlacements = new ArrayList<>();

        initialPlacements.add(new RecruitmentAction(Collections.singletonList(
                new RecruitmentActionMotion(
                        RecruitmentMotionType.Add,
                        Character.create(CharacterType.LeaderKing, TeamColor.Black),
                        new Position(0, 0)
                )
        )));
        initialPlacements.add(new RecruitmentAction(Collections.singletonList(
                new RecruitmentActionMotion(
                        RecruitmentMotionType.Add,
                        Character.create(CharacterType.LeaderKing, TeamColor.White),
                        new Position(6, 3)
                )
        )));

        if (barrageDetected) {
            for (int y = 0; y <= 6; y++) {
                initialPlacements.add(new RecruitmentAction(Collections.singletonList(
                        new RecruitmentActionMotion(
                                RecruitmentMotionType.Add,
                                Character.create(CharacterType.Archer, TeamColor.Black),
                                new Position(3, y)
                        )
                )));
            }
        }

        GameConfig config = new GameConfig(
                players,
                players.get(0),
                GameMode.Discovery,
                Collections.emptyList(),
                initialPlacements
        );
        GameHistory history = new GameHistory(config, new ArrayList<>());
        Turn turn = new Turn(TeamColor.Black);
        history.getEntries().add(turn);

        if (initialWarningCount > 0) {
            turn.getSubPhase(GamePhaseType.TurnEnd).getActions().add(
                    new WarningAction(WarningType.Barrage, TeamColor.Black, initialWarningCount)
            );
        }

        turn.getSubPhase(GamePhaseType.TurnEnd).start();

        return history;
    }

    private List<Player> createPlayers() {
        return Arrays.asList(
                new Player(TeamColor.Black, "Black Player"),
                new Player(TeamColor.White, "White Player")
        );
    }

    private GameHistory createCheckGameEndedGameHistory() {
        List<Player> players = createPlayers();

        Character leaderBlack = Character.create(CharacterType.LeaderKing, TeamColor.Black);
        Character leaderWhite = Character.create(CharacterType.LeaderQueen, TeamColor.White);

        RecruitmentAction initialPlacement = new RecruitmentAction(Arrays.asList(
                new RecruitmentActionMotion(RecruitmentMotionType.Add, leaderBlack, new Position(3, 0)),
                new RecruitmentActionMotion(RecruitmentMotionType.Add, leaderWhite, new Position(3, 6))
        ));

        GameConfig config = new GameConfig(
                players,
                players.get(0),
                GameMode.Discovery,
                Arrays.asList(CharacterCard.Bruiser, CharacterCard.HermitAndCub),
                Collections.singletonList(initialPlacement)
        );

        return new GameHistory(config, new ArrayList<>());
    }
}