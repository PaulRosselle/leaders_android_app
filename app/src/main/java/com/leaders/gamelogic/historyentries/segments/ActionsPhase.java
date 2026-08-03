package com.leaders.gamelogic.historyentries.segments;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.gamelogic.actions.IGameAction;
import com.leaders.gamelogic.actions.TransitionAction;
import com.leaders.gamelogic.enums.TransitionTarget;
import com.leaders.gamelogic.historyentries.IPhase;
import com.leaders.gamelogic.historyentries.Segment;

import java.util.ArrayList;

public final class ActionsPhase extends Segment implements IPhase {
    private final ArrayList<IGameAction> actions;

    public ActionsPhase(@Nullable TransitionAction startAction, @Nullable TransitionAction endAction) {
        super(startAction, endAction);
        actions = new ArrayList<>();
    }

    @Override
    public ArrayList<IGameAction> getActions() {
        return actions;
    }

    @NonNull
    @Override
    public TransitionTarget getTransitionTarget() {
        return TransitionTarget.ActionsPhase;
    }
}
