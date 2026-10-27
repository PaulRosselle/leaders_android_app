
package com.leaders.puzzlelogic.entities.blocker;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.actions.CharacterAction;
import com.leaders.gamelogic.entities.Game;
import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.puzzlelogic.utilities.blocker.NemesisBlockerUtils;
import com.leaders.puzzlelogic.utilities.blocker.SearchCache;

import java.util.List;

final class NemesisBlockerWorker implements Runnable {
    private final NemesisBlockerCoordinator coordinator;
    private final Game game;
    private final GameHistory gameHistory;
    private final List<CharacterAction> nemesisActions;
    private final SearchCache searchCache; // Shared and concurrent
    private final Runnable endCallback;

    NemesisBlockerWorker(@NonNull NemesisBlockerCoordinator coordinator,
                         @NonNull Game game,
                         @NonNull GameHistory gameHistory,
                         @NonNull List<CharacterAction> nemesisActions,
                         @NonNull SearchCache searchCache,
                         @NonNull Runnable endCallback) {
        this.coordinator = coordinator;

        // Each worker use its own copy of the game and history to test out actions
        this.game = new Game(game);
        this.gameHistory = new GameHistory(gameHistory);

        this.nemesisActions = nemesisActions;
        this.searchCache = searchCache;
        this.endCallback = endCallback;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                int index = coordinator.claimNext();
                if (index < 0) break;

                try {
                    boolean blocks = NemesisBlockerUtils.canBlock(
                            game, gameHistory, nemesisActions.get(index), searchCache,
                            () -> coordinator.isSuperseded(index)
                                    || Thread.currentThread().isInterrupted());

                    coordinator.reportResult(index, blocks);
                } catch (InterruptedException e) {
                    if (coordinator.isSuperseded(index) && !Thread.currentThread().isInterrupted()) {
                        continue; // Invalid candidate, not a worker interuption
                    }
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        } finally {
            if (Thread.currentThread().isInterrupted()) {
                coordinator.cancel();
            }
            endCallback.run();
        }
    }
}