package com.leaders.app.controllers;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.gamelogic.GameHandler;
import com.leaders.gamelogic.entities.Game;
import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.gamelogic.entities.GamePhase;
import com.leaders.gamelogic.entities.Player;
import com.leaders.gamelogic.interactions.IGameFlowListener;
import com.leaders.gamelogic.interactions.InteractionFeedback;
import com.leaders.gamelogic.interactions.InteractionRequest;
import com.leaders.gamelogic.interactions.InteractionResult;
import com.leaders.gamelogic.interactions.InteractionResultType;
import com.leaders.gamelogic.interactions.InteractionTarget;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class PuzzlePlayerController implements IGameFlowListener {

    public interface Listener {
        void onGameStarted(@NonNull Game game);

        void onGameEnded(@NonNull Player winner);

        void onActionUndone(@NonNull Game game);

        void onInteractionRequired(@NonNull InteractionRequest request);

        void onFeedback(@NonNull InteractionFeedback feedback);

        void onInteractionCleared();
    }

    private final Listener listener;

    private final ExecutorService gameHandlerExecutor = Executors.newSingleThreadExecutor();

    private GameHandler gameHandler;
    private CompletableFuture<Void> gameTask;

    private InteractionRequest pendingRequest;
    private CompletableFuture<InteractionResult> pendingRequestFuture;

    public PuzzlePlayerController(@NonNull Listener listener) {
        this.listener = listener;
    }

    public void startGame(@NonNull GameHistory gameHistory) {
        gameHandlerExecutor.execute(() -> {
            gameHandler = new GameHandler(gameHistory, this);

            gameTask = gameHandler.runAsync();

            gameTask.whenComplete((result, throwable) -> {
                if (throwable != null) {
                    Thread thread = Thread.currentThread();
                    Thread.UncaughtExceptionHandler exceptionHandler =
                            thread.getUncaughtExceptionHandler();

                    if (exceptionHandler != null) {
                        exceptionHandler.uncaughtException(thread, throwable);
                    }
                }
            });
        });
    }

    private void endGame() {
        if (hasPendingInteraction() && isCancellationAllowed()) {
            cancelAction();
        }

        if (gameHandler != null) {
            gameHandler.stop();
        }
    }

    public void restartGame(@NonNull GameHistory gameHistory) {
        endGame();
        startGame(gameHistory);
    }

    public boolean hasPendingInteraction() {
        return pendingRequest != null && pendingRequestFuture != null && !pendingRequestFuture.isDone();
    }

    public boolean isLegalTarget(@NonNull InteractionTarget target) {
        if (pendingRequest == null) {
            return false;
        }

        return pendingRequest.getLegalTargets().contains(target);
    }

    public boolean isCancellationAllowed() {
        return hasLegalResult(InteractionResultType.CancelAction);
    }

    public boolean canUndoLastAction() {
        return hasLegalResult(InteractionResultType.UndoLastAction);
    }

    public void selectTarget(@NonNull InteractionTarget target) {
        if (!hasPendingInteraction()) {
            throw new IllegalStateException(
                    "Targets should not exist outside of a valid request context"
            );
        }

        if (!isLegalTarget(target)) {
            throw new IllegalArgumentException("Invalid target: " + target);
        }

        completeInteraction(getTargetResult(pendingRequest, target));
    }

    public void cancelAction() {
        if (!isCancellationAllowed()) {
            return;
        }

        completeInteraction(getResult(pendingRequest, InteractionResultType.CancelAction));
    }

    public void undoLastAction() {
        if (!hasPendingInteraction()) {
            throw new IllegalStateException(
                    "Undo should not exist outside of a valid request context"
            );
        }

        if (!canUndoLastAction()) {
            return;
        }

        completeInteraction(getResult(pendingRequest, InteractionResultType.UndoLastAction));
    }

    private boolean hasLegalResult(@NonNull InteractionResultType resultType) {
        return pendingRequest != null
                && pendingRequest.getLegalResults().contains(resultType);
    }

    private void completeInteraction(@NonNull InteractionResult result) {
        if (pendingRequestFuture == null || pendingRequestFuture.isDone()) {
            return;
        }

        CompletableFuture<InteractionResult> future = pendingRequestFuture;

        pendingRequest = null;
        pendingRequestFuture = null;

        listener.onInteractionCleared();

        future.complete(result);
    }

    private InteractionResult getTargetResult(@NonNull InteractionRequest request,
                                              @NonNull InteractionTarget target) {
        return new InteractionResult(
                target.getCategory().getResultType(),
                request.getContext(),
                target
        );
    }

    private InteractionResult getResult(@NonNull InteractionRequest request,
                                        @NonNull InteractionResultType resultType) {
        return new InteractionResult(resultType, request.getContext(), null);
    }

    @NonNull
    @Override
    public CompletableFuture<Void> onGameStarted(@NonNull Game game) {
        listener.onGameStarted(game);
        return CompletableFuture.completedFuture(null);
    }

    @NonNull
    @Override
    public CompletableFuture<Void> onGameEnded(@NonNull Player winner) {
        listener.onGameEnded(winner);
        return CompletableFuture.completedFuture(null);
    }

    @NonNull
    @Override
    public CompletableFuture<Void> onPhaseChanged(@NonNull GamePhase phase) {
        throw new IllegalStateException("Phase change is not supported within the puzzle player");
    }

    @NonNull
    @Override
    public CompletableFuture<Void> onActionUndone(@NonNull Game game) {
        listener.onActionUndone(game);
        return CompletableFuture.completedFuture(null);
    }

    @NonNull
    @Override
    public CompletableFuture<InteractionResult> onInputRequired(@NonNull InteractionRequest request) {

        pendingRequest = request;
        pendingRequestFuture = new CompletableFuture<>();

        listener.onInteractionRequired(request);

        return pendingRequestFuture;
    }

    @NonNull
    @Override
    public CompletableFuture<Void> onFeedback(@NonNull InteractionFeedback feedback) {

        listener.onFeedback(feedback);
        return CompletableFuture.completedFuture(null);
    }

    public void shutdown() {
        endGame();
        gameHandlerExecutor.shutdownNow();
    }

    @NonNull
    public Game getCurrentGame() {
        if (gameHandler == null) {
            throw new IllegalStateException("Game has not been started");
        }

        return new Game(gameHandler.getCurrentGame());
    }
}