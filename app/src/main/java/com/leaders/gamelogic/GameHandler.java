package com.leaders.gamelogic;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.actions.IGameAction;
import com.leaders.gamelogic.entities.Game;
import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.gamelogic.entities.GamePhase;
import com.leaders.gamelogic.entities.PlayableCharacter;
import com.leaders.gamelogic.entities.Player;
import com.leaders.gamelogic.enums.GameMode;
import com.leaders.gamelogic.enums.GamePhaseType;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.gamelogic.factories.GameActionHandlerFactory;
import com.leaders.gamelogic.factories.GameFactory;
import com.leaders.gamelogic.handlers.GameActionHandler;
import com.leaders.gamelogic.historyentries.IPhase;
import com.leaders.gamelogic.historyentries.Segment;
import com.leaders.gamelogic.historyentries.segments.BanishmentPhase;
import com.leaders.gamelogic.historyentries.segments.Turn;
import com.leaders.gamelogic.historyentries.segments.TurnEndPhase;
import com.leaders.gamelogic.historyentries.segments.TurnPhase;
import com.leaders.gamelogic.interactions.IGameFlowListener;
import com.leaders.gamelogic.interactions.InteractionContext;
import com.leaders.gamelogic.interactions.InteractionRequest;
import com.leaders.gamelogic.interactions.InteractionResult;
import com.leaders.gamelogic.interactions.InteractionResultType;
import com.leaders.gamelogic.interactions.InteractionTarget;
import com.leaders.gamelogic.interactions.InteractionType;
import com.leaders.gamelogic.interactions.TargetCategory;
import com.leaders.gamelogic.queries.GameHistoryQuery;
import com.leaders.gamelogic.queries.PhaseTransitionQuery;
import com.leaders.gamelogic.queries.PlayabilityQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class GameHandler {
    private static final class GameEndedException extends RuntimeException {
        @NonNull
        private final Player winner;

        private GameEndedException(@NonNull Player winner) {
            this.winner = winner;
        }

        @NonNull
        private Player getWinner() {
            return winner;
        }
    }

    @NonNull
    private final Game currentGame;

    @NonNull
    private final GameHistory currentHistory;

    @NonNull
    private final IGameFlowListener gameFlowListener;

    public GameHandler(@NonNull GameHistory currentHistory,
                       @NonNull IGameFlowListener gameFlowListener) {
        this.currentHistory = currentHistory;
        this.currentGame = GameFactory.create(currentHistory);
        this.gameFlowListener = gameFlowListener;
    }

    @NonNull
    public Game getCurrentGame() {
        return currentGame;
    }

    @NonNull
    public GameHistory getCurrentHistory() {
        return currentHistory;
    }

    @NonNull
    public GameMode getGameMode() {
        return currentHistory.getConfig().getGameMode();
    }

    @NonNull
    public List<Player> getPlayers() {
        return currentHistory.getConfig().getPlayers();
    }

    /**
     * Runs the game until it ends.
     *
     * @return a future completed when the game has ended
     */
    @NonNull
    public CompletableFuture<Void> runAsync() {
        // Start the game before scheduling the asynchronous game loop.
        return gameFlowListener.onGameStarted(currentGame)
                .thenCompose(ignored -> runGameLoopAsync())
                // handle() allows asynchronous exceptions to be handled at the
                // end of the future chain without blocking on its completion.
                .handle(this::handleGameLoopResult)
                .thenCompose(future -> future);
    }

    /**
     * Runs the current phase or starts the next phase when none is active.
     *
     * @return a future completed when the current phase has been handled
     */
    @NonNull
    private CompletableFuture<Void> runGameLoopAsync() {
        IPhase currentPhase = GameHistoryQuery.findCurrentPhase(currentHistory);
        CompletableFuture<Void> phaseExecution;

        if (currentPhase != null) {
            GamePhase currentGamePhase = new GamePhase(
                    GamePhaseType.getFromTransitionTarget(GameHistoryQuery.getPhaseTransitionTarget(currentPhase)),
                    GameHistoryQuery.getPlayerFromTeam(currentHistory, GameHistoryQuery.getPhaseTeamColor(currentPhase))
            );
            phaseExecution = runCurrentPhaseAsync(currentGamePhase);
        } else {
            phaseExecution = startNextPhaseAsync();
        }

        // Chain the next loop iteration instead of blocking with join() or get().
        // Each iteration starts only after the current asynchronous operation completes.
        return phaseExecution.thenCompose(ignored -> runGameLoopAsync());
    }

    /**
     * Handles the result of the game loop and propagates unexpected failures.
     *
     * @param result completed game loop result
     * @param throwable failure raised by the game loop, if any
     * @return a future completed when the game has ended, or failed with an unexpected exception
     */
    @NonNull
    private CompletableFuture<Void> handleGameLoopResult(Void result, Throwable throwable) {
        CompletableFuture<Void> handledResult;

        if (throwable == null) {
            handledResult = CompletableFuture.completedFuture(null);
        } else {
            // Asynchronous failures may be wrapped in CompletionException.
            // Unwrap it before checking the type of the actual failure.
            Throwable cause = throwable;
            if (throwable instanceof java.util.concurrent.CompletionException
                    && throwable.getCause() != null) {
                cause = throwable.getCause();
            }

            if (cause instanceof GameEndedException) {
                // GameEndedException is the internal signal used to stop the loop
                // and notify the listener with the winning player.
                GameEndedException gameEndedException = (GameEndedException) cause;
                handledResult = gameFlowListener.onGameEnded(gameEndedException.getWinner());
            } else {
                // Only GameEndedException is handled here. All other failures
                // remain exceptional so they are propagated to the caller.
                CompletableFuture<Void> failedResult = new CompletableFuture<>();
                failedResult.completeExceptionally(cause);
                handledResult = failedResult;
            }
        }

        return handledResult;
    }

    /**
     * Starts the next phase and notifies the game flow listener.
     *
     * @return a future completed when the phase change has been acknowledged
     * @throws IllegalStateException if no current turn exists for a turn-bound phase
     */
    private CompletableFuture<Void> startNextPhaseAsync() {
        GamePhase nextPhase = PhaseTransitionQuery.getNextPhase(currentGame, currentHistory);
        TeamColor nextPhaseTeamColor = nextPhase.getPhasePlayer().getTeamColor();
        if (nextPhase.getPhaseType() == GamePhaseType.TurnStart) {
            Turn nextTurn = new Turn(nextPhaseTeamColor);
            currentHistory.getEntries().add(nextTurn);
            nextTurn.start();
            nextTurn.getSubPhase(GamePhaseType.TurnStart).start();
        } else if (nextPhase.getPhaseType() == GamePhaseType.Banishment) {
            BanishmentPhase banishmentPhase = new BanishmentPhase(nextPhaseTeamColor);
            currentHistory.getEntries().add(banishmentPhase);
            banishmentPhase.start();
        } else {
            Turn currentTurn = GameHistoryQuery.findCurrentTurn(currentHistory);
            if (currentTurn == null) {
                throw new IllegalStateException("All transitions besides \"TurnStart\" and \"Banishment\" should imply a turn in progress");
            }
            currentTurn.getSubPhase(nextPhase.getPhaseType()).start();
        }

        return gameFlowListener.onPhaseChanged(nextPhase);
    }

    /**
     * Runs the current phase and closes it when execution completes successfully.
     *
     * @param currentPhase current game phase
     * @return a future completed when the phase has been executed and ended
     */
    private CompletableFuture<Void> runCurrentPhaseAsync(@NonNull GamePhase currentPhase) {
        // Check for game end before executing any phase-specific behavior.
        // This is common to all game phases and is therefore handled here.
        checkGameEnded(currentPhase);

        CompletableFuture<Void> phaseExecution;
        switch (currentPhase.getPhaseType()) {
            case TurnStart: phaseExecution = runTurnStartPhaseAsync(currentPhase); break;
            case Actions: phaseExecution = runActionsPhaseAsync(currentPhase); break;
            case Recruitment: phaseExecution = runRecruitmentPhaseAsync(currentPhase); break;
            case TurnEnd: phaseExecution = runTurnEndPhaseAsync(currentPhase); break;
            case Banishment: phaseExecution = runBanishmentPhaseAsync(currentPhase); break;
            default: throw new IllegalStateException("Unsupported game phase type " + currentPhase.getPhaseType());
        }

        return phaseExecution.thenRun(this::endCurrentPhase);
    }

    /**
     * Ends the current phase segment.
     *
     * <p>When ending the turn end phase, the parent turn is ended as well.</p>
     *
     * @throws IllegalStateException if the current history state is invalid
     */
    private void endCurrentPhase() {
        IPhase currentPhase = GameHistoryQuery.findCurrentPhase(currentHistory);
        if (currentPhase instanceof TurnEndPhase) {
            Turn currentTurn = GameHistoryQuery.findCurrentTurn(currentHistory);
            if (currentTurn == null) {
                throw new IllegalStateException("Cannot end TurnEnd phase without a current turn");
            }
            ((TurnEndPhase) currentPhase).end();
            currentTurn.end();
        } else if (currentPhase instanceof Segment) {
            ((Segment) currentPhase).end();
        } else {
            throw new IllegalStateException(currentPhase == null ?
                    "Cannot end a phase when no current phase exists" : "Current phase is not a segment");
        }
    }

    private CompletableFuture<Void> runTurnStartPhaseAsync(@NonNull GamePhase currentPhase) {
        // There is no automatic action to perform during turn start at the moment
        return CompletableFuture.completedFuture(null);
    }

    private CompletableFuture<Void> runActionsPhaseAsync(@NonNull GamePhase currentPhase) {
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Determines the legal results for playable character selection.
     *
     * @param currentPhase current actions phase
     * @param playableCharacters characters currently playable
     * @return the legal interaction results
     */
    private List<InteractionResultType> getPlayableCharacterSelectionLegalResults(@NonNull GamePhase currentPhase,
                                                                                  @NonNull List<PlayableCharacter> playableCharacters) {
        List<InteractionResultType> legalResults = new ArrayList<>();

        legalResults.add(InteractionResultType.PlayableCharacterChosen);

        // An action can only be undone when the current turn actions list isn't empty.
        Turn currentTurn = GameHistoryQuery.findCurrentTurn(currentHistory);
        if (currentTurn == null) {
            throw new IllegalStateException("Character selection is only allowed within the actions phase of a turn");
        }
        TurnPhase currentTurnPhase = currentTurn.getSubPhase(currentPhase.getPhaseType());
        if (!currentTurnPhase.getActions().isEmpty()) {
            legalResults.add(InteractionResultType.UndoLastAction);
        }

        // The actions phase can only end when no playable character is mandatory.
        boolean canEndTurn = true;
        for (PlayableCharacter playableCharacter : playableCharacters) {
            if (playableCharacter.isMandatory()) {
                canEndTurn = false;
                break;
            }
        }
        if (canEndTurn) {
            legalResults.add(InteractionResultType.EndPhase);
        }

        return legalResults;
    }

    /**
     * Requests the player to select one of the currently playable characters.
     * Determines the legal interaction results from the current game state.
     *
     * @param currentPhase current game phase
     * @return the interaction result returned by the game flow listener
     */
    private CompletableFuture<InteractionResult> runSelectPlayableCharacterAsync(@NonNull GamePhase currentPhase) {
        List<PlayableCharacter> playableCharacters = PlayabilityQuery.getPlayableCharacters(currentGame, currentHistory);

        List<InteractionTarget> legalTargets = new ArrayList<>();
        for (PlayableCharacter playableCharacter : playableCharacters) {
            legalTargets.add(new InteractionTarget(TargetCategory.PlayableCharacter, playableCharacter));
        }

        List<InteractionResultType> legalResults = getPlayableCharacterSelectionLegalResults(
                currentPhase,
                playableCharacters
        );

        InteractionRequest request = new InteractionRequest(
                InteractionType.PositionExpected,
                new InteractionContext(),
                legalTargets,
                legalResults
        );

        return gameFlowListener.onInputRequired(request);
    }

    private CompletableFuture<Void> runRecruitmentPhaseAsync(@NonNull GamePhase currentPhase) {
        return CompletableFuture.completedFuture(null);
    }

    private CompletableFuture<Void> runTurnEndPhaseAsync(@NonNull GamePhase currentPhase) {
        return CompletableFuture.completedFuture(null);
    }

    private CompletableFuture<Void> runBanishmentPhaseAsync(@NonNull GamePhase currentPhase) {
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Applies the given action to the current game and appends it to the current phase history.
     *
     * @param currentGamePhase current game phase
     * @param action action to apply
     * @throws IllegalStateException if no game phase is currently active
     */
    private void doAction(@NonNull GamePhase currentGamePhase, @NonNull IGameAction action) {
        IPhase currentPhase = GameHistoryQuery.findCurrentPhase(currentHistory);
        if (currentPhase == null) {
            throw new IllegalStateException("Cannot do an action outside of a game phase");
        }

        GameActionHandler actionHandler = GameActionHandlerFactory.create(currentGame, action);
        actionHandler.doAction();
        currentPhase.getActions().add(action);
        // Check for game end after each action, as every action represents a game state change
        // that may trigger the victory condition.
        checkGameEnded(currentGamePhase);
    }

    /**
     * Undoes the last action of the current phase.
     *
     * @throws IllegalStateException if no current phase exists or the current phase has no actions
     */
    private void undoLastAction() {
        IPhase currentPhase = GameHistoryQuery.findCurrentPhase(currentHistory);
        if (currentPhase == null || currentPhase.getActions().isEmpty()) {
            throw new IllegalStateException("Cannot undo an action outside of a game phase or within an empty phase");
        }

        List<IGameAction> actions = currentPhase.getActions();
        IGameAction lastAction = actions.get(actions.size() - 1);
        actions.remove(actions.size() - 1);

        GameActionHandler actionHandler = GameActionHandlerFactory.create(currentGame, lastAction);
        actionHandler.undoAction();
    }

    /**
     * Checks whether the current game has ended.
     *
     * @param currentPhase current game phase
     */
    private void checkGameEnded(@NonNull GamePhase currentPhase) {
        // Not implemented yet
    }
}