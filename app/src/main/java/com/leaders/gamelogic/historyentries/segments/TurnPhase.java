package com.leaders.gamelogic.historyentries.segments;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.gamelogic.actions.TransitionAction;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.gamelogic.historyentries.IPhase;
import com.leaders.gamelogic.historyentries.Segment;

public abstract class TurnPhase extends Segment implements IPhase {
    @NonNull
    private final TeamColor turnTeamColor;

    public TurnPhase(@Nullable TransitionAction startAction, @Nullable TransitionAction endAction, @NonNull TeamColor turnTeamColor) {
        super(startAction, endAction);
        this.turnTeamColor = turnTeamColor;
    }

    @NonNull
    public TeamColor getTurnTeamColor() {
        return turnTeamColor;
    }
}
