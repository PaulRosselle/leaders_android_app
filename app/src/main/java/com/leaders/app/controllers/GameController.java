package com.leaders.app.controllers;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.GameHandler;
import com.leaders.gamelogic.entities.Game;
import com.leaders.gamelogic.entities.GameContext;
import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.gamelogic.entities.GamePhase;
import com.leaders.gamelogic.entities.Player;
import com.leaders.gamelogic.interactions.IGameFlowListener;
import com.leaders.gamelogic.interactions.InteractionFeedback;
import com.leaders.gamelogic.interactions.InteractionRequest;
import com.leaders.gamelogic.interactions.InteractionResult;
import com.leaders.gamelogic.interactions.InteractionResultType;
import com.leaders.gamelogic.interactions.InteractionTarget;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicReference;

public final class GameController implements IGameFlowListener {

    public interface Listener {
        void onGameStarted(@NonNull GameContext context);
        void onGameEnded(@NonNull Player winner, @NonNull GameContext context);
        void onActionUndone(@NonNull GameContext context);
        void onInteractionRequired(@NonNull InteractionRequest request, @NonNull GameContext context);
        void onInteractionCleared(@NonNull GameContext context);
        void onPhaseChanged(@NonNull GamePhase phase, @NonNull GameContext context);
        void onFeedback(@NonNull InteractionFeedback feedback, @NonNull InteractionCompletion completion);
    }

    private static final class Pending {
        @NonNull
        final InteractionRequest request;
        @NonNull
        final CompletableFuture<InteractionResult> future;
        @NonNull
        final GameContext context;

        Pending(@NonNull InteractionRequest request,
                @NonNull CompletableFuture<InteractionResult> future,
                @NonNull GameContext context) {
            this.request = request;
            this.future = future;
            this.context = context;
        }
    }

    private final AtomicReference<Pending> pending = new AtomicReference<>();
    private volatile GameContext latestContext;

    public interface InteractionCompletion {
        void complete();
    }

    private final Listener listener;

    private final ExecutorService gameHandlerExecutor = Executors.newSingleThreadExecutor();
    private volatile GameHandler gameHandler;
    private volatile CompletableFuture<Void> gameTask;

    public GameController(@NonNull Listener listener) {
        this.listener = listener;
    }

    public void startGame(@NonNull GameHistory gameHistory) {
        gameHandlerExecutor.execute(() -> {
            gameHandler = new GameHandler(gameHistory, this);

            gameTask = gameHandler.runAsync();

            gameTask.whenComplete(this::doOnGameEnd);
        });
    }

    private void doOnGameEnd(Void result, Throwable throwable) {
        if (throwable != null) {
            Thread thread = Thread.currentThread();
            Thread.UncaughtExceptionHandler exceptionHandler = thread.getUncaughtExceptionHandler();

            if (exceptionHandler != null) {
                exceptionHandler.uncaughtException(thread, throwable);
            }
        }
    }

    private void endGame() {
        if (gameHandler != null) {
            gameHandler.stop();
        }
        Pending currentPending = pending.getAndSet(null);
        if (currentPending != null) {
            // Last game loop ends with a CancellationException (catched by the gameHandler)
            currentPending.future.completeExceptionally(new CancellationException());
        }
    }

    public void restartGame(@NonNull GameHistory gameHistory) {
        endGame();
        startGame(gameHistory);
    }

    private boolean hasLegalResult(@NonNull InteractionResultType type) {
        Pending currentPeding = pending.get();
        return currentPeding != null && currentPeding.request.getLegalResults().contains(type);
    }

    public boolean canUndoLastAction() {
        return hasLegalResult(InteractionResultType.UndoLastAction);
    }

    public boolean canEndPhase() {
        return hasLegalResult(InteractionResultType.EndPhase);
    }

    public void selectTarget(@NonNull InteractionTarget target) {
        Pending currentPending = pending.get();
        if (currentPending == null) {
            throw new IllegalStateException("Targets should not exist outside of a valid request context");
        }
        if (!currentPending.request.getLegalTargets().contains(target)) {
            throw new IllegalArgumentException("Invalid target: " + target);
        }
        completeInteraction(currentPending, getTargetResult(currentPending.request, target));
    }

    private void completeInteractionWithResult(@NonNull InteractionResultType resultType) {
        Pending currentPending = pending.get();
        if (currentPending == null ||
                !currentPending.request.getLegalResults().contains(resultType)) {
            return;
        }
        completeInteraction(currentPending, getResult(currentPending.request, resultType));
    }

    public void cancelAction() {
        completeInteractionWithResult(InteractionResultType.CancelAction);
    }

    public void undoLastAction() {
        completeInteractionWithResult(InteractionResultType.UndoLastAction);
    }

    public void endPhase() {
        completeInteractionWithResult(InteractionResultType.EndPhase);
    }

    private void completeInteraction(@NonNull Pending pending, @NonNull InteractionResult result) {
        // compareAndSet prevent two different threads to complete the same interaction
        if (!this.pending.compareAndSet(pending, null)) {
            return;
        }

        runOnGameThread(() -> {
            // Since state is still linked with the last request, we can use pending.contexte here.
            // Clear is called before the next notification sent to the listener
            listener.onInteractionCleared(pending.context);
            pending.future.complete(result);
        });
    }

    private void runOnGameThread(@NonNull Runnable task) {
        try {
            gameHandlerExecutor.execute(task);
        } catch (RejectedExecutionException ignored) {
            // stopped controller
        }
    }

    public boolean isRequestPending(@NonNull InteractionRequest request) {
        Pending currentPending = pending.get();
        return currentPending != null && currentPending.request == request;
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
    public CompletableFuture<InteractionResult> onInputRequired(@NonNull InteractionRequest request) {
        CompletableFuture<InteractionResult> future = new CompletableFuture<>();
        GameContext context = snapshot();
        pending.set(new Pending(request, future, context));
        listener.onInteractionRequired(request, context);
        return future;
    }

    @NonNull
    @Override
    public CompletableFuture<Void> onGameStarted(@NonNull Game game) {
        listener.onGameStarted(snapshot());
        return CompletableFuture.completedFuture(null);
    }

    @NonNull
    @Override
    public CompletableFuture<Void> onGameEnded(@NonNull Player winner) {
        listener.onGameEnded(winner, snapshot());
        return CompletableFuture.completedFuture(null);
    }

    @NonNull
    @Override
    public CompletableFuture<Void> onPhaseChanged(@NonNull GamePhase phase) {
        listener.onPhaseChanged(phase, snapshot());
        return CompletableFuture.completedFuture(null);
    }

    @NonNull
    @Override
    public CompletableFuture<Void> onActionUndone(@NonNull Game game) {
        listener.onActionUndone(snapshot());
        return CompletableFuture.completedFuture(null);
    }

    @NonNull
    @Override
    public CompletableFuture<Void> onFeedback(@NonNull InteractionFeedback feedback) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        listener.onFeedback(feedback, () -> runOnGameThread(() -> future.complete(null)));
        return future;
    }

    // Only called from the gameHandler thread
    private GameContext snapshot() {
        GameContext context = gameHandler.getCurrentContext();
        latestContext = context;
        return context;
    }


    public void shutdown() {
        endGame();
        gameHandlerExecutor.shutdownNow();
    }

    // Readonly gameContext for UI: always an immutable snapshot
    public GameContext getCurrentContext() {
        GameContext context = latestContext;
        if (context == null) {
            throw new IllegalStateException("No game in progress");
        }
        return context;
    }

    public GameHistory getHistory() {
        if (gameHandler == null) {
            throw new IllegalStateException("No game in progress");
        }

        return new GameHistory(gameHandler.getCurrentHistory());
    }

    /**
     * Checks whether a game task is currently running.
     *
     * @return {@code true} if the game task exists and has not completed, otherwise {@code false}
     */
    public boolean isGameRunning() {
        CompletableFuture<Void> task = gameTask;
        return task != null && !task.isDone();
    }
}