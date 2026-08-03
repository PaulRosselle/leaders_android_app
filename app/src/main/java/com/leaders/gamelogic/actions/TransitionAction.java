package com.leaders.gamelogic.actions;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.enums.GameActionType;
import com.leaders.gamelogic.enums.TransitionTarget;
import com.leaders.gamelogic.enums.TransitionType;

public final class TransitionAction implements IGameAction {
    @Override
    public GameActionType getActionType() {
        return GameActionType.Transition;
    }

    @NonNull
    private final TransitionType transitionType;
    @NonNull
    private final TransitionTarget transitionTarget;

    public TransitionAction(@NonNull TransitionType transitionType, @NonNull TransitionTarget transitionTarget) {
        this.transitionType = transitionType;
        this.transitionTarget = transitionTarget;
    }

    @NonNull
    public TransitionType getTransitionType() {
        return transitionType;
    }

    @NonNull
    public TransitionTarget getTransitionTarget() {
        return transitionTarget;
    }
}
