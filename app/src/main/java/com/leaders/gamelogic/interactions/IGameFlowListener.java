package com.leaders.gamelogic.interactions;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.entities.Game;
import com.leaders.gamelogic.entities.Player;
import com.leaders.gamelogic.entities.GamePhase;

import java.util.concurrent.CompletableFuture;

/**
 * Observer interface to be implemented by an external component orchestrating a game session.
 * <p>
 * All methods are asynchronous, allowing the caller to control timing for animations
 * and UI transitions.
 */
public interface IGameFlowListener {

    /**
     * Fired once when the game session starts, before the first turn.
     *
     * @param game the current game state at session start
     * @return a future completed when the caller has finished handling the event
     */
    @NonNull
    CompletableFuture<Void> onGameStarted(@NonNull Game game);

    /**
     * Fired once when the game session ends.
     *
     * @param winner the winning player
     * @return a future completed when the caller has finished handling the event
     */
    @NonNull
    CompletableFuture<Void> onGameEnded(@NonNull Player winner);

    /**
     * Fired whenever the game enters a new phase.
     *
     * @param phase the incoming game phase
     * @return a future completed when the caller has finished handling the event
     */
    @NonNull
    CompletableFuture<Void> onPhaseChanged(@NonNull GamePhase phase);

    /**
     * Fired whenever an action is undone.
     *
     * @param game the game in which the action was undone
     * @return a future completed when the undo handling is complete
     */
    @NonNull
    CompletableFuture<Void> onActionUndone(@NonNull Game game);

    /**
     * Fired whenever the game requires an input from the caller.
     * <p>
     * The {@link InteractionRequest} carries the expected input type, legal values,
     * and accepted response types. The caller must return an {@link InteractionResult}
     * that satisfies the request contract.
     *
     * @param request the requested interaction context
     * @return a future completed with the caller's response
     */
    @NonNull
    CompletableFuture<InteractionResult> onInputRequired(@NonNull InteractionRequest request);

    /**
     * Fired whenever the game produces a feedback that must be handled by the caller.
     *
     * <p>The game flow waits for the returned future to complete before continuing.</p>
     *
     * @param feedback the feedback to handle
     * @return a future completed when the caller has finished handling the feedback
     */
    @NonNull
    CompletableFuture<Void> onFeedback(@NonNull InteractionFeedback feedback);
}