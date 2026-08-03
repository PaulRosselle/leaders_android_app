package com.leaders.gamelogic.historyentries.segments;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.gamelogic.actions.IGameAction;
import com.leaders.gamelogic.actions.TransitionAction;
import com.leaders.gamelogic.enums.TransitionTarget;
import com.leaders.gamelogic.historyentries.IPhase;
import com.leaders.gamelogic.historyentries.Segment;

import java.util.ArrayList;

public final class TurnEndPhase extends Segment implements IPhase {
    @NonNull
    private final ArrayList<IGameAction> actions;

    public TurnEndPhase(@Nullable TransitionAction startAction, @Nullable TransitionAction endAction) {
        super(startAction, endAction);
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
