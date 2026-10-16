
package com.leaders.puzzlelogic.entities.blocker;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.actions.CharacterAction;
import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.entities.Game;
import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.puzzlelogic.utilities.blocker.NemesisBlockerUtils;

import java.util.List;

final class NemesisBlockerWorker implements Runnable {

    private final NemesisBlockerCoordinator coordinator;
    private final Game game;
    private final GameHistory gameHistory;
    private final List<CharacterAction> nemesisActions;
    private final List<Character> opponentCharacters;

    private final Runnable endCallback;

    NemesisBlockerWorker(@NonNull NemesisBlockerCoordinator coordinator,
                         @NonNull Game game,
                         @NonNull GameHistory gameHistory,
                         @NonNull List<CharacterAction> nemesisActions,
                         @NonNull List<Character> opponentCharacters,
                         @NonNull Runnable endCallback) {
        this.coordinator = coordinator;

        // Each worker use its own copy of the game and history to test out actions
        this.game = new Game(game);
        this.gameHistory = new GameHistory(gameHistory);

        // The goal of a worker is to find out an action within "nemesisActions" allowing
        // no victory for the opponent no matter the characters within "opponentCharacters"
        this.nemesisActions = nemesisActions;
        this.opponentCharacters = opponentCharacters;

        this.endCallback = endCallback;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted() && !coordinator.isFinished()) {
                ActionCandidate candidate = coordinator.getCurrentCandidate();

                if (candidate == null) {
                    break;
                }

                if (!candidate.isActive()) {
                    coordinator.reportResult(candidate, candidate.getStatus());
                    continue;
                }

                long permutationIndex = candidate.reservePermutation();

                if (permutationIndex < 0) {
                    try {
                        candidate.awaitProgress();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                    continue;
                }

                try {
                    if (!candidate.isActive()) {
                        continue;
                    }

                    boolean canBlock = NemesisBlockerUtils.canBlock(
                            game,
                            gameHistory,
                            nemesisActions.get(candidate.actionIndex),
                            opponentCharacters,
                            permutationIndex,
                            () -> !candidate.isActive() ||
                                    coordinator.isFinished() ||
                                    Thread.currentThread().isInterrupted()
                    );

                    ActionCandidate.Status result = candidate.reportResult(canBlock);

                    coordinator.reportResult(candidate, result);

                } catch (InterruptedException e) {
                    if (!candidate.isActive()
                            && !Thread.currentThread().isInterrupted()) {
                        // Invalid candidate (not necessarely a worker interuption)
                        coordinator.reportResult(candidate, candidate.getStatus());
                    } else {
                        // Worker interuption
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        } finally {
            // An interupted worker must not leave the search waiting for a permutation.
            if (Thread.currentThread().isInterrupted()) {
                coordinator.cancel();
            }

            endCallback.run();
        }
    }
}