
package com.leaders.puzzlelogic.entities.blocker;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.actions.CharacterAction;
import com.leaders.gamelogic.actions.CharacterActionMotion;
import com.leaders.gamelogic.actions.CharacterActionTarget;
import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.entities.Game;
import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.gamelogic.entities.Position;
import com.leaders.gamelogic.enums.CharacterType;
import com.leaders.gamelogic.factories.GameFactory;
import com.leaders.gamelogic.historyentries.IPhase;
import com.leaders.gamelogic.historyentries.segments.ActionsPhase;
import com.leaders.gamelogic.interactions.InteractionRequest;
import com.leaders.gamelogic.interactions.InteractionTarget;
import com.leaders.gamelogic.queries.BoardQuery;
import com.leaders.gamelogic.queries.CharacterAbilityQuery;
import com.leaders.gamelogic.queries.GameHistoryQuery;
import com.leaders.puzzlelogic.utilities.BotUtils;
import com.leaders.puzzlelogic.utilities.GameSearchUtils;
import com.leaders.puzzlelogic.utilities.blocker.NemesisBlockerUtils;
import com.leaders.puzzlelogic.utilities.blocker.SearchCache;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class NemesisBlocker {
    private final Game game;
    private final GameHistory gameHistory;

    private final Character nemesis;
    private final List<CharacterAction> nemesisActions;
    private final List<InteractionTarget> nemesisTargets;
    private final List<Character> opponentCharacters;
    private final NemesisBlockerCallback callback;
    private final SearchCache searchCache;

    private final NemesisBlockerCoordinator coordinator;
    private boolean callbackDelivered;

    private ExecutorService executor;

    private boolean started;

    public NemesisBlocker(@NonNull GameHistory gameHistory,
                          @NonNull InteractionRequest request,
                          @NonNull NemesisBlockerCallback callback) {
        this.game = GameFactory.create(gameHistory);
        this.gameHistory = gameHistory;

        nemesis = request.getContext().getTargetCharacter();
        if (nemesis == null || nemesis.getCharacterType() != CharacterType.Nemesis) {
            throw new IllegalArgumentException("Nemesis blocked require a Nemesis request");
        }
        nemesisTargets = request.getLegalTargets();

        this.nemesisActions = GameSearchUtils.enumerateActions(game, gameHistory, nemesis);
        this.opponentCharacters = searchOpponentCharacters(game, gameHistory, nemesis);
        this.searchCache = new SearchCache();

        this.callback = callback;
        this.callbackDelivered = false;

        this.coordinator = new NemesisBlockerCoordinator(nemesisActions);
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

    /**
     * Searches for a blocking action and notifies the callback of the result.
     *
     * @throws IllegalStateException if the search is interrupted unexpectedly
     */
    private void startSimpleSearch() {
        if (nemesisActions.size() == 1) {
            callbackDelivered = true;
            callback.onBlockingActionFound(getDefaultTarget());
            return;
        }

        for (CharacterAction nemesisAction : nemesisActions) {
            boolean canBlock;
            try {
                canBlock = NemesisBlockerUtils.canBlock(
                        game, gameHistory, nemesisAction, searchCache, () -> false);
            } catch (InterruptedException e) {
                throw new IllegalStateException("Invalid context for interruption", e);
            }
            if (canBlock) {
                callbackDelivered = true;
                callback.onBlockingActionFound(getTargetFromAction(nemesisAction));
                return;
            }
        }

        callbackDelivered = true;
        callback.onSearchCompletedWithoutResult(getDefaultTarget());
    }

    private void startCoordinatedSearch() {
        boolean alreadyFinished;

        synchronized (this) {
            alreadyFinished = coordinator.isFinished();

            if (!alreadyFinished) {
                int availableProcessors = Runtime.getRuntime().availableProcessors();
                int workerCount = Math.max(1, Math.min(availableProcessors - 1, nemesisActions.size()));

                executor = Executors.newFixedThreadPool(workerCount);

                for (int i = 0; i < workerCount; i++) {
                    executor.execute(new NemesisBlockerWorker(
                            coordinator, game, gameHistory,
                            nemesisActions, searchCache, this::onCoordinatorChanged
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
                    callback.onBlockingActionFound(getTargetFromAction(winningAction));
                }
            } break;

            case COMPLETED_WITHOUT_RESULT: {
                callback.onSearchCompletedWithoutResult(getDefaultTarget());
            } break;

            case CANCELLED: {
                callback.onSearchCancelled();
            } break;

            case RUNNING:
                throw new IllegalStateException("Callback should not be delivered with a running coordinator");
        }
    }

    @NonNull
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

    private InteractionTarget getTargetFromAction(@NonNull CharacterAction nemesisAction) {
        return getNemesisTargetFromDestination(nemesisTargets, getNemesisDestination(nemesisAction));
    }

    @NonNull
    public static InteractionTarget getNemesisTargetFromDestination(@NonNull List<InteractionTarget> targets,
                                                                    @NonNull Position destination) {
        for (InteractionTarget nemesisTarget : targets) {
            if (destination.equals(nemesisTarget.getChosenPosition())) {
                return nemesisTarget;
            }
        }

        throw new IllegalStateException("No target found matching Nemesis destination: " + destination);
    }

    @NonNull
    public static Position getNemesisDestination(@NonNull CharacterAction nemesisAction) {
        if (nemesisAction.getSrcCharacter().getCharacterType() != CharacterType.Nemesis) {
            throw new IllegalArgumentException("Nemesis action required");
        }

        CharacterActionMotion lastMotion = Objects.requireNonNull(
                nemesisAction.getMotions().get(nemesisAction.getMotions().size() - 1),
                "Motion missing for Nemesis action"
        );
        CharacterActionTarget motionTarget = Objects.requireNonNull(
                lastMotion.getTargets().get(0),
                "Target missing for Nemesis action"
        );
        return Objects.requireNonNull(
                motionTarget.getDestPos(),
                "Destination missing for Nemesis action"
        );
    }

    @NonNull
    private InteractionTarget getDefaultTarget() {
        Position enemyLeaderPos = Objects.requireNonNull(
                BoardQuery.findLeaderCell(game.getBoard(), nemesis.getTeamColor().getOpposite()),
                "Nemesis blocker cannot find a default target without an enemy Leader"
        ).getPosition();
        return BotUtils.getClosestTarget(nemesisTargets, enemyLeaderPos);
    }
}
