package com.leaders.gamelogic.historyentries.segments;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.gamelogic.actions.IGameAction;
import com.leaders.gamelogic.actions.TransitionAction;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.gamelogic.enums.TransitionTarget;
import com.leaders.gamelogic.historyentries.IPhase;
import com.leaders.gamelogic.historyentries.Segment;

import java.util.ArrayList;

public final class RecruitmentPhase extends TurnPhase {
    @NonNull
    private final ArrayList<IGameAction> actions;

    public RecruitmentPhase(@Nullable TransitionAction startAction, @Nullable TransitionAction endAction, @NonNull TeamColor turnTeamColor) {
        super(startAction, endAction, turnTeamColor);
        actions = new ArrayList<>();
    }

    @NonNull
    @Override
    public ArrayList<IGameAction> getActions() {
        return actions;
    }

    @NonNull
    @Override
    public TransitionTarget getTransitionTarget() {
        return TransitionTarget.RecruitmentPhase;
    }
}
