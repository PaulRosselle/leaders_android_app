package com.leaders.gamelogic;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.gamelogic.actions.CharacterAction;
import com.leaders.gamelogic.actions.IGameAction;
import com.leaders.gamelogic.actions.RecruitmentAction;
import com.leaders.gamelogic.entities.Game;
import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.gamelogic.entities.GamePhase;
import com.leaders.gamelogic.entities.PlayableCharacter;
import com.leaders.gamelogic.entities.Player;
import com.leaders.gamelogic.entities.SelectableCharacterCard;
import com.leaders.gamelogic.enums.CharacterCard;
import com.leaders.gamelogic.enums.GameMode;
import com.leaders.gamelogic.enums.GamePhaseType;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.gamelogic.factories.CharacterActionResolverFactory;
import com.leaders.gamelogic.factories.GameActionHandlerFactory;
import com.leaders.gamelogic.factories.GameFactory;
import com.leaders.gamelogic.handlers.GameActionHandler;
import com.leaders.gamelogic.historyentries.IPhase;
import com.leaders.gamelogic.historyentries.Segment;
import com.leaders.gamelogic.historyentries.segments.BanishmentPhase;
import com.leaders.gamelogic.historyentries.segments.Turn;
import com.leaders.gamelogic.historyentries.segments.TurnEndPhase;
import com.leaders.gamelogic.historyentries.segments.TurnPhase;
import com.leaders.gamelogic.interactions.CharacterActionBuilder;
import com.leaders.gamelogic.interactions.IGameFlowListener;
import com.leaders.gamelogic.interactions.InteractionContext;
import com.leaders.gamelogic.interactions.InteractionFeedback;
import com.leaders.gamelogic.interactions.InteractionRequest;
import com.leaders.gamelogic.interactions.InteractionResult;
import com.leaders.gamelogic.interactions.InteractionResultType;
import com.leaders.gamelogic.interactions.InteractionTarget;
import com.leaders.gamelogic.interactions.InteractionType;
import com.leaders.gamelogic.interactions.RecruitmentActionBuilder;
import com.leaders.gamelogic.interactions.TargetCategory;
import com.leaders.gamelogic.queries.GameHistoryQuery;
import com.leaders.gamelogic.queries.PhaseTransitionQuery;
import com.leaders.gamelogic.queries.PlayabilityQuery;
import com.leaders.gamelogic.queries.RecruitmentQuery;
import com.leaders.gamelogic.resolvers.CharacterActionResolver;
import com.leaders.gamelogic.resolvers.RecruitmentActionResolver;

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

    /**
     * Runs the actions phase until the player chooses to end it.
     *
     * @param currentPhase current actions phase
     * @return a future completed when the actions phase should end
     */
    private CompletableFuture<Void> runActionsPhaseAsync(@NonNull GamePhase currentPhase) {
        return runSelectPlayableCharacterAsync(currentPhase).thenCompose(result -> {
            CompletableFuture<Void> iterationExecution;
            boolean continuePhase = true;

            switch (result.getResultType()) {
                case PlayableCharacterChosen:
                    // Each character action resolution creates its own builder,
                    // so every iteration starts with a fresh action state.
                    iterationExecution = runPlayCharacterAsync(currentPhase, getPlayableCharacterFromResult(result));
                    break;
                case UndoLastAction:
                    undoLastAction();
                    iterationExecution = CompletableFuture.completedFuture(null);
                    break;
                case EndPhase:
                    iterationExecution = CompletableFuture.completedFuture(null);
                    continuePhase = false;
                    break;
                default:
                    throw new IllegalStateException(
                            "Invalid interaction result : illegal type \"" +
                                    result.getResultType() + "\" for actions phase"
                    );
            }

            if (continuePhase) {
                iterationExecution = iterationExecution.thenCompose(
                        ignored -> runActionsPhaseAsync(currentPhase)
                );
            }

            return iterationExecution;
        });
    }

    /**
     * Returns the playable character selected by the interaction result.
     *
     * @param result the interaction result containing the selected playable character
     * @return the selected playable character
     * @throws IllegalStateException if the interaction result does not contain a valid playable character
     */
    @NonNull
    private static PlayableCharacter getPlayableCharacterFromResult(@NonNull InteractionResult result) {
        InteractionTarget chosenTarget = result.getChosenTarget();
        PlayableCharacter playableCharacter = chosenTarget == null ?
                null : chosenTarget.getChosenCharacterPlayableState();

        if (chosenTarget == null ||
                chosenTarget.getCategory() != TargetCategory.PlayableCharacter ||
                playableCharacter == null) {
            throw new IllegalStateException("Invalid interaction result : playable character missing");
        }
        return playableCharacter;
    }

    /**
     * Checks whether the current turn phase contains actions that can be undone.
     *
     * @param currentPhase the current game phase
     * @return {@code true} if the current turn phase contains at least one action,
     *         {@code false} otherwise
     * @throws IllegalStateException if no current turn is found
     */
    private boolean currentTurnPhaseContainsActions(@NonNull GamePhase currentPhase) {
        // An action can only be undone when the current turn actions list isn't empty.
        Turn currentTurn = GameHistoryQuery.findCurrentTurn(currentHistory);
        if (currentTurn == null) {
            throw new IllegalStateException("No current turn phase found to check for actions");
        }
        TurnPhase currentTurnPhase = currentTurn.getSubPhase(currentPhase.getPhaseType());
        return !currentTurnPhase.getActions().isEmpty();
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
        if (currentTurnPhaseContainsActions(currentPhase)) {
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

    /**
     * Resolves and executes the action of the selected playable character.
     *
     * @param currentPhase current game phase
     * @param playableCharacter selected playable character
     * @return a future completed when the action has been resolved or canceled
     * @throws IllegalArgumentException if the playable character is missing
     */
    private CompletableFuture<Void> runPlayCharacterAsync(@NonNull GamePhase currentPhase,
                                                          @Nullable PlayableCharacter playableCharacter) {
        if (playableCharacter == null) {
            throw new IllegalArgumentException("A playable character is required to resolve an action");
        }

        CharacterActionBuilder builder = new CharacterActionBuilder(
                playableCharacter.getCharacter(),
                new ArrayList<>(),
                new ArrayList<>()
        );

        CharacterActionResolver resolver = CharacterActionResolverFactory.create(
                currentGame,
                currentHistory,
                playableCharacter.getCharacter()
        );

        return resolveCharacterActionAsync(currentPhase, builder, resolver);
    }

    /**
     * Resolves a character action by requesting the required player inputs until the action
     * is complete or canceled.
     *
     * @param currentPhase current game phase
     * @param builder builder containing the current action state
     * @param resolver resolver providing the next interaction and resulting action
     * @return a future completed when the action has been resolved or canceled
     */
    @NonNull
    private CompletableFuture<Void> resolveCharacterActionAsync(@NonNull GamePhase currentPhase,
                                                                @NonNull CharacterActionBuilder builder,
                                                                @NonNull CharacterActionResolver resolver) {
        // Cancellation stops the resolution before the action is applied to the game.
        if (builder.isBuildCancelled()) {
            return CompletableFuture.completedFuture(null);
        }

        InteractionRequest request = resolver.getNextInteraction(builder);

        // No further input is required, so the resolver can build and execute the action.
        if (request == null) {
            CharacterAction action = resolver.buildAction(builder);
            doAction(currentPhase, action);
            return CompletableFuture.completedFuture(null);
        }

        // Request the next input required to continue resolving the action.
        return gameFlowListener.onInputRequired(request).thenCompose(result -> {
            builder.addResult(result);

            // A feedback may be generated from the newly received input.
            InteractionFeedback feedback = resolver.getNextFeedback(builder);
            if (feedback == null) {
                // No feedback is required, so continue directly with the next resolution step.
                return resolveCharacterActionAsync(currentPhase, builder, resolver);
            }

            // Store the feedback before notifying the listener and continuing the resolution.
            builder.addFeedback(feedback);
            return gameFlowListener.onFeedback(feedback)
                    .thenCompose(ignored -> resolveCharacterActionAsync(
                            currentPhase,
                            builder,
                            resolver
                    ));
        });
    }

    /**
     * Runs the recruitment phase until no further recruitment is possible.
     *
     * @param currentPhase current recruitment phase
     * @return a future completed when the recruitment phase is finished
     */
    private CompletableFuture<Void> runRecruitmentPhaseAsync(@NonNull GamePhase currentPhase) {
        TeamColor recruitmentTeamColor = currentPhase.getPhasePlayer().getTeamColor();

        if (!RecruitmentQuery.canRecruit(currentGame, currentHistory, recruitmentTeamColor)) {
            return CompletableFuture.completedFuture(null);
        }

        return runSelectRecruitmentCardAsync(currentPhase)
                .thenCompose(selectableCard ->
                        runRecruitCardAsync(currentPhase, selectableCard.getCharacterCard()))
                .thenCompose(ignored -> runRecruitmentPhaseAsync(currentPhase));
    }

    /**
     * Requests the player to select a card that can currently be recruited.
     *
     * @return the selected character card
     */
    @NonNull
    private CompletableFuture<SelectableCharacterCard> runSelectRecruitmentCardAsync(@NonNull GamePhase currentPhase) {
        List<SelectableCharacterCard> selectableRecruitmentCards =
                RecruitmentQuery.getSelectableRecruitmentCards(currentGame, currentHistory);

        List<InteractionTarget> legalTargets = new ArrayList<>();
        for (SelectableCharacterCard selectableRecruitmentCard : selectableRecruitmentCards) {
            legalTargets.add(new InteractionTarget(TargetCategory.RecruitmentCard, selectableRecruitmentCard));
        }

        List<InteractionResultType> legalResults = new ArrayList<>();
        legalResults.add(InteractionResultType.SelectableCharacterCardChosen);
        // A recruitment action can only be undone in Strategist mode.
        if (getGameMode() == GameMode.Strategist && currentTurnPhaseContainsActions(currentPhase)) {
            legalResults.add(InteractionResultType.UndoLastAction);
        }

        InteractionRequest request = new InteractionRequest(
                InteractionType.SelectableCharacterCardExpected,
                new InteractionContext(),
                legalTargets,
                legalResults
        );

        // Request an input to select the recruited card
        return gameFlowListener.onInputRequired(request).thenCompose(result -> {
            // Since recruitments are mandatory
            if (result.getResultType() == InteractionResultType.UndoLastAction) {
                undoLastAction();
                return runSelectRecruitmentCardAsync(currentPhase);
            }

            if (result.getResultType() != InteractionResultType.SelectableCharacterCardChosen) {
                throw new IllegalStateException(
                        "Invalid interaction result : illegal type \"" +
                                result.getResultType() + "\" for recruitment card selection"
                );
            }

            return CompletableFuture.completedFuture(getSelectableCharacterCardFromResult(result));
        });
    }

    /**
     * Retrieves the character card selected by the given interaction result.
     *
     * @param result the interaction result containing the selected recruitment target
     * @return the selected character card
     * @throws IllegalStateException if the interaction result does not contain a
     *                               valid recruitment target or if the selected
     *                               character card is missing
     */
    @NonNull
    private static SelectableCharacterCard getSelectableCharacterCardFromResult(@NonNull InteractionResult result) {
        InteractionTarget chosenTarget = result.getChosenTarget();
        SelectableCharacterCard chosenCard = chosenTarget == null ? null : chosenTarget.getChosenSelectableCharacterCard();

        if (chosenTarget == null ||
                chosenTarget.getCategory() != TargetCategory.RecruitmentCard ||
                chosenCard == null) {
            throw new IllegalStateException(
                    "Invalid interaction result : chosen card missing for recruitment"
            );
        }
        return chosenCard;
    }

    /**
     * Resolves the recruitment of the selected character card.
     *
     * @param currentPhase current game phase
     * @param recruitedCard card selected for recruitment
     * @return a future completed when the recruitment has been resolved
     */
    @NonNull
    private CompletableFuture<Void> runRecruitCardAsync(@NonNull GamePhase currentPhase,
                                                        @NonNull CharacterCard recruitedCard) {
        TeamColor recruitmentColor = currentPhase.getPhasePlayer().getTeamColor();
        RecruitmentActionBuilder builder = new RecruitmentActionBuilder(
                recruitedCard,
                recruitmentColor,
                new ArrayList<>(),
                new ArrayList<>()
        );

        RecruitmentActionResolver resolver = new RecruitmentActionResolver(currentGame, recruitedCard, recruitmentColor);

        return resolveRecruitmentActionAsync(currentPhase, builder, resolver);
    }

    /**
     * Resolves a recruitment action by requesting the required player inputs until the recruitment
     * is complete or canceled.
     *
     * @param currentPhase current game phase
     * @param builder builder containing the current recruitment state
     * @param resolver resolver providing the next interaction and resulting action
     * @return a future completed when the recruitment has been resolved
     */
    @NonNull
    private CompletableFuture<Void> resolveRecruitmentActionAsync(@NonNull GamePhase currentPhase,
                                                                  @NonNull RecruitmentActionBuilder builder,
                                                                  @NonNull RecruitmentActionResolver resolver) {
        // Cancellation stops the resolution before the action is applied to the game.
        if (builder.isBuildCancelled()) {
            return CompletableFuture.completedFuture(null);
        }

        InteractionRequest request = resolver.getNextInteraction(builder);

        // No further input is required, so the resolver can build and execute the action.
        if (request == null) {
            RecruitmentAction action = resolver.buildAction(builder);
            doAction(currentPhase, action);
            return CompletableFuture.completedFuture(null);
        }

        return gameFlowListener.onInputRequired(request).thenCompose(result -> {
            builder.addResult(result);

            InteractionFeedback feedback = resolver.getNextFeedback(builder);
            if (feedback == null) {
                return resolveRecruitmentActionAsync(currentPhase, builder, resolver);
            }

            builder.addFeedback(feedback);
            return gameFlowListener.onFeedback(feedback)
                    .thenCompose(ignored -> resolveRecruitmentActionAsync(
                            currentPhase,
                            builder,
                            resolver
                    ));
        });
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