package com.leaders.gamelogic.historyentries.segments;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.gamelogic.actions.IGameAction;
import com.leaders.gamelogic.actions.TransitionAction;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.gamelogic.enums.TransitionTarget;

import java.util.ArrayList;

public final class TurnStartPhase extends TurnPhase {
    @NonNull
    private final ArrayList<IGameAction> actions;

    public TurnStartPhase(@Nullable TransitionAction startAction, @Nullable TransitionAction endAction, @NonNull TeamColor turnTeamColor) {
        super(startAction, endAction, turnTeamColor);
        actions = new ArrayList<>();
    }

    public TurnStartPhase(@NonNull TeamColor turnTeamColor) {
        this(null, null, turnTeamColor);
    }

    public TurnStartPhase(@NonNull TurnStartPhase refTurnStartPhase) {
        this(refTurnStartPhase.getStartAction(), refTurnStartPhase.getEndAction(), refTurnStartPhase.getTurnTeamColor());
        actions.addAll(refTurnStartPhase.getActions());
    }

    @NonNull
    @Override
    public ArrayList<IGameAction> getActions() {
        return actions;
    }

    @NonNull
    @Override
    public TransitionTarget getTransitionTarget() {
        return TransitionTarget.TurnStartPhase;
    }
}
