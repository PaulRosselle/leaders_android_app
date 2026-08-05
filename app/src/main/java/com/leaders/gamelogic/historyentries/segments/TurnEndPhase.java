package com.leaders.gamelogic.historyentries.segments;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.gamelogic.actions.IGameAction;
import com.leaders.gamelogic.actions.TransitionAction;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.gamelogic.enums.TransitionTarget;

import java.util.ArrayList;

public final class TurnEndPhase extends TurnPhase {
    @NonNull
    private final ArrayList<IGameAction> actions;

    public TurnEndPhase(@Nullable TransitionAction startAction, @Nullable TransitionAction endAction, @NonNull TeamColor turnTeamColor) {
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
        return TransitionTarget.TurnEndPhase;
    }
}
