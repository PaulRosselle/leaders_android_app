
package com.leaders.puzzlelogic.entities.blocker;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.actions.CharacterAction;
import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.entities.Game;
import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.puzzlelogic.utilities.blocker.NemesisBlockerUtils;

import java.util.List;
import java.util.Set;

final class NemesisBlockerWorker implements Runnable {
    private final NemesisBlockerCoordinator coordinator;
    private final Game game;
    private final GameHistory gameHistory;
    private final List<CharacterAction> nemesisActions;
    private final Set<String> losingStates; // partagé, concurrent
    private final Runnable endCallback;

    NemesisBlockerWorker(@NonNull NemesisBlockerCoordinator coordinator,
                         @NonNull Game game,
                         @NonNull GameHistory gameHistory,
                         @NonNull List<CharacterAction> nemesisActions,
                         @NonNull Set<String> losingStates,
                         @NonNull Runnable endCallback) {
        this.coordinator = coordinator;
        this.game = new Game(game);                      // copies privées par worker
        this.gameHistory = new GameHistory(gameHistory);
        this.nemesisActions = nemesisActions;
        this.losingStates = losingStates;
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
                            game, gameHistory, nemesisActions.get(index), losingStates,
                            () -> coordinator.isSuperseded(index)
                                    || Thread.currentThread().isInterrupted());

                    coordinator.reportResult(index, blocks);
                } catch (InterruptedException e) {
                    if (coordinator.isSuperseded(index) && !Thread.currentThread().isInterrupted()) {
                        continue; // candidat devenu inutile, pas une interruption du worker
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