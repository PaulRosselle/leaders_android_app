
package com.leaders.puzzlelogic.entities.blocker;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.actions.CharacterAction;
import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.entities.Game;
import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.gamelogic.factories.GameFactory;
import com.leaders.gamelogic.historyentries.IPhase;
import com.leaders.gamelogic.historyentries.segments.ActionsPhase;
import com.leaders.gamelogic.queries.CharacterAbilityQuery;
import com.leaders.gamelogic.queries.GameHistoryQuery;
import com.leaders.puzzlelogic.utilities.CharactersPermutationUtils;
import com.leaders.puzzlelogic.utilities.GameSearchUtils;
import com.leaders.puzzlelogic.utilities.blocker.NemesisBlockerUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class NemesisBlocker {
    private final Game game;
    private final GameHistory gameHistory;
    private final List<CharacterAction> nemesisActions;
    private final List<Character> opponentCharacters;
    private final long permutationCount;
    private final NemesisBlockerCallback callback;

    private final NemesisBlockerCoordinator coordinator;
    private boolean callbackDelivered;

    private ExecutorService executor;

    private boolean started;

    public NemesisBlocker(@NonNull GameHistory gameHistory,
                          @NonNull Character nemesis,
                          @NonNull NemesisBlockerCallback callback) {
        this.game = GameFactory.create(gameHistory);
        this.gameHistory = gameHistory;

        this.nemesisActions = GameSearchUtils.enumerateActions(game, gameHistory, nemesis);
        this.opponentCharacters = searchOpponentCharacters(game, gameHistory, nemesis);

        this.callback = callback;
        this.callbackDelivered = false;

        permutationCount = CharactersPermutationUtils.getPermutationCount(opponentCharacters);

        List<ActionCandidate> candidates = new ArrayList<>();
        for (int i = 0; i < nemesisActions.size(); i++) {
            candidates.add(new ActionCandidate(nemesisActions.get(i), i, permutationCount));
        }
        this.coordinator = new NemesisBlockerCoordinator(candidates);
    }

    public boolean canBeInterrupted() {
        return nemesisActions.size() > 1 && opponentCharacters.size() > 2;
    }

    public void start() {
        synchronized (this) {
            if (started) {
                throw new IllegalStateException("NemesisBlocker has already been started");
            }
            started = true;

            if (callbackDelivered) {
                return;
            }
        }

        if (canBeInterrupted()) {
            startCoordinatedSearch();
        } else {
            startSimpleSearch();
        }
    }

    private void startSimpleSearch() {
        // Since the Nemesis is forced to move, a search is not needed with a single action
        if (nemesisActions.size() == 1) {
            callback.onBlockingActionFound(nemesisActions.get(0));
            return;
        }

        // Simple search iterates through Nemesis action to find one that can block the opponent
        // victory for every permutation. It is run in a single thread and cannot be interrupted
        for (CharacterAction nemesisAction : nemesisActions) {
            boolean canBlock = true;
            try {
                // Without permutation, we only check the current state of the game
                if (permutationCount <= 1) {
                    canBlock = NemesisBlockerUtils.canBlock(
                            game, gameHistory, nemesisAction, opponentCharacters, () -> false
                    );
                } else {
                    for (int permutationIndex = 0; permutationIndex < permutationCount; permutationIndex++) {
                            if (!NemesisBlockerUtils.canBlock(
                                    game, gameHistory, nemesisAction,
                                    opponentCharacters, permutationIndex, () -> false)) {
                                canBlock = false;
                                break;
                            }
                    }
                }
            } catch (InterruptedException e) {
                throw new IllegalStateException("Invalid context for interuption" + e);
            }
            if (canBlock) {
                callbackDelivered = true;
                callback.onBlockingActionFound(nemesisAction);
                return;
            }
        }

        // By default, the first nemesis action is returned
        callbackDelivered = true;
        callback.onSearchCompletedWithoutResult(nemesisActions.isEmpty() ? null : nemesisActions.get(0));
    }

    private void startCoordinatedSearch() {
        boolean alreadyFinished;

        synchronized (this) {
            alreadyFinished = coordinator.isFinished();

            if (!alreadyFinished) {
                int availableProcessors = Runtime.getRuntime().availableProcessors();
                int workerCount = Math.max(1, availableProcessors - 1);

                executor = Executors.newFixedThreadPool(workerCount);

                for (int i = 0; i < workerCount; i++) {
                    executor.execute(new NemesisBlockerWorker(
                            coordinator,
                            game,
                            gameHistory,
                            nemesisActions,
                            opponentCharacters,
                            this::onCoordinatorChanged
                    ));
                }
            }
        }

        // An already resolved blocker cannot be reused.
        if (alreadyFinished) {
            onCoordinatorChanged();
        }
    }

    public void cancel() {
        synchronized (this) {
            coordinator.cancel();

            if (executor != null) {
                executor.shutdownNow();
            }
        }

        onCoordinatorChanged();
    }

    // Called after each relevant state change of the coordinator.
    void onCoordinatorChanged() {
        NemesisBlockerCoordinator.SearchStatus finalStatus;
        CharacterAction winningAction;

        synchronized (this) {
            if (callbackDelivered) {
                return;
            }

            finalStatus = coordinator.getStatus();

            if (finalStatus == NemesisBlockerCoordinator.SearchStatus.RUNNING) {
                return;
            }

            // The callback must be called only once.
            callbackDelivered = true;
            winningAction = coordinator.getWinningAction();
        }

        // We can deliver the callback once outside the synchronized block.
        switch (finalStatus) {
            case FOUND: {
                if (winningAction != null) {
                    callback.onBlockingActionFound(winningAction);
                }
            } break;

            case COMPLETED_WITHOUT_RESULT: {
                // By default, the first nemesis action is returned
                callback.onSearchCompletedWithoutResult(
                        nemesisActions.isEmpty() ? null : nemesisActions.get(0)
                );
            } break;

            case CANCELLED: {
                callback.onSearchCancelled();
            } break;

            case RUNNING:
                throw new IllegalStateException("Callback should not be delivered with a running coordinator");
        }
    }

    private List<Character> searchOpponentCharacters(@NonNull Game game,
                                                     @NonNull GameHistory gameHistory,
                                                     @NonNull Character nemesis) {
        IPhase currentPhase = GameHistoryQuery.findCurrentPhase(gameHistory);
        if (!(currentPhase instanceof ActionsPhase)) {
            throw new IllegalStateException("The Nemesis blocker require an ActionsPhase");
        }

        ActionsPhase currentActionsPhase = (ActionsPhase) currentPhase;
        if (currentActionsPhase.getTurnTeamColor() == nemesis.getTeamColor()) {
            throw new IllegalStateException("The Nemesis blocker require an opponent Nemesis");
        }

        List<Character> characters = new ArrayList<>();

        for (Character character : game.getRecruitedCharacters()) {
            if (CharacterAbilityQuery.canAct(currentActionsPhase, character)) {
                characters.add(character);
            }
        }

        return characters;
    }
}
