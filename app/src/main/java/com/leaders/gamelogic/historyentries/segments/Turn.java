package com.leaders.gamelogic.historyentries.segments;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.gamelogic.actions.TransitionAction;
import com.leaders.gamelogic.enums.GamePhaseType;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.gamelogic.enums.TransitionTarget;
import com.leaders.gamelogic.historyentries.IHistoryEntry;
import com.leaders.gamelogic.historyentries.Segment;

import java.util.ArrayList;

public class Turn extends Segment implements IHistoryEntry {
    @NonNull
    private final TeamColor teamColor;

    @NonNull
    private final TurnStartPhase turnStartPhase;
    @NonNull
    private final ActionsPhase actionsPhase;
    @NonNull
    private final RecruitmentPhase recruitmentPhase;
    @NonNull
    private final TurnEndPhase turnEndPhase;

    public Turn(@Nullable TransitionAction startAction, @Nullable TransitionAction endAction, @NonNull TeamColor teamColor, @NonNull TurnStartPhase turnStartPhase, @NonNull ActionsPhase actionsPhase, @NonNull RecruitmentPhase recruitmentPhase, @NonNull TurnEndPhase turnEndPhase) {
        super(startAction, endAction);
        this.teamColor = teamColor;
        this.turnStartPhase = turnStartPhase;
        this.actionsPhase = actionsPhase;
        this.recruitmentPhase = recruitmentPhase;
        this.turnEndPhase = turnEndPhase;
    }

    @NonNull
    @Override
    public TeamColor getTeamColor() {
        return teamColor;
    }

    @NonNull
    @Override
    public TransitionTarget getTransitionTarget() {
        return TransitionTarget.Turn;
    }

    /**
     * Returns the sub-phases of this turn in their execution order.
     *
     * @return the ordered list of sub-phases composing this turn
     */
    @NonNull
    public TurnPhase[] getSubPhasesInOrder() {
        return new TurnPhase[] { turnStartPhase, actionsPhase, recruitmentPhase, turnEndPhase };
    }

    /**
     * Returns the sub-phase corresponding to the given game phase type.
     *
     * @param gamePhaseType the game phase type to resolve
     * @return the sub-phase matching the given game phase type
     * @throws IllegalStateException if no sub-phase matches the given game phase type
     */
    @NonNull
    public TurnPhase getSubPhase(@NonNull GamePhaseType gamePhaseType) {
        TransitionTarget transitionTarget = gamePhaseType.getTransitionTarget();
        for (TurnPhase turnPhase : getSubPhasesInOrder()) {
            if (turnPhase.getTransitionTarget() == transitionTarget) {
                return turnPhase;
            }
        }
        throw new IllegalStateException("No sub phase found matching game phase type " + gamePhaseType);
    }
}
