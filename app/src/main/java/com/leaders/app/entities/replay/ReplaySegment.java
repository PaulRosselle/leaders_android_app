package com.leaders.app.entities.replay;

import androidx.annotation.NonNull;

import com.leaders.app.utilities.GameActionUtils;
import com.leaders.gamelogic.actions.IGameAction;
import com.leaders.gamelogic.actions.WarningAction;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.gamelogic.historyentries.IHistoryEntry;
import com.leaders.gamelogic.historyentries.IPhase;
import com.leaders.gamelogic.historyentries.segments.Turn;
import com.leaders.gamelogic.historyentries.segments.TurnPhase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ReplaySegment {
    @NonNull
    private final TeamColor teamColor;

    @NonNull
    private final List<ReplayStep> steps;

    public ReplaySegment(@NonNull IHistoryEntry historyEntry) {
        this.teamColor = historyEntry.getTeamColor();

        List<IGameAction> playableActions = new ArrayList<>();
        List<WarningAction> warningActions = new ArrayList<>();

        if (historyEntry instanceof Turn) {
            for (TurnPhase phase : ((Turn) historyEntry).getSubPhasesInOrder()) {
                collectPhaseActions(phase, playableActions, warningActions);
            }
        } else if (historyEntry instanceof IPhase) {
            collectPhaseActions((IPhase) historyEntry, playableActions, warningActions);
        } else {
            throw new IllegalArgumentException("history entry not handled by ReplaySegment");
        }

        this.steps = buildSteps(playableActions, warningActions);
    }

    private void collectPhaseActions(@NonNull IPhase phase,
                                     @NonNull List<IGameAction> playableActions,
                                     @NonNull List<WarningAction> warningActions) {
        for (IGameAction action : phase.getActions()) {
            if (action instanceof WarningAction) {
                warningActions.add((WarningAction) action);

            } else if (GameActionUtils.isAnimatable(action)) {
                playableActions.add(action);
            }
        }
    }

    private List<ReplayStep> buildSteps(@NonNull List<IGameAction> playableActions,
                                        @NonNull List<WarningAction> warningActions) {
        // A segment without actions returns a single step with warnings
        if (playableActions.isEmpty()) {
            return Collections.singletonList(new ReplayStep(null, warningActions));
        }

        List<ReplayStep> actionSteps = new ArrayList<>();

        if (playableActions.size() > 1) {
            for (int i = 0; i < playableActions.size() - 1; i++) {
                actionSteps.add(new ReplayStep(playableActions.get(i), Collections.emptyList()));
            }
        }
        // The segment warnings are stored within the last action
        actionSteps.add(new ReplayStep(playableActions.get(playableActions.size() - 1), warningActions));

        return Collections.unmodifiableList(actionSteps);
    }


    @NonNull
    public TeamColor getTeamColor() {
        return teamColor;
    }

    @NonNull
    public List<ReplayStep> getSteps() {
        return steps;
    }
}
