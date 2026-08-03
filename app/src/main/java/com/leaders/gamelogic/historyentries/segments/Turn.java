package com.leaders.gamelogic.historyentries.segments;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.gamelogic.actions.TransitionAction;
import com.leaders.gamelogic.enums.GamePhaseType;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.gamelogic.enums.TransitionTarget;
import com.leaders.gamelogic.historyentries.IHistoryEntry;
import com.leaders.gamelogic.historyentries.IPhase;
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

    public IPhase[] getSubPhasesInOrder() {
        return new IPhase[] { turnStartPhase, actionsPhase, recruitmentPhase, turnEndPhase };
    }

    public Segment[] getSubPhasesInOrderAsSegments() {
        ArrayList<Segment> segments = new ArrayList<>();
        for (IPhase phase : getSubPhasesInOrder()) {
            if (phase instanceof Segment) {
                segments.add((Segment) phase);
            }
        }
        return segments.toArray(new Segment[0]);
    }

    public Segment getSubPhaseAsSegment(GamePhaseType gamePhaseType) {
        TransitionTarget transitionTarget = gamePhaseType.getTransitionTarget();
        for (Segment segment : getSubPhasesInOrderAsSegments()) {
            if (segment.getTransitionTarget() == transitionTarget) {
                return segment;
            }
        }
        throw new IllegalStateException("No phase found matching game phase type " + gamePhaseType);
    }
}
