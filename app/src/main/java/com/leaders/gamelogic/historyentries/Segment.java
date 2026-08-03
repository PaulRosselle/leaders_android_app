package com.leaders.gamelogic.historyentries;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.gamelogic.actions.TransitionAction;
import com.leaders.gamelogic.enums.TransitionTarget;
import com.leaders.gamelogic.enums.TransitionType;

public abstract class Segment {
    @NonNull
    public abstract TransitionTarget getTransitionTarget();
    @Nullable
    private TransitionAction startAction;
    @Nullable
    private TransitionAction endAction;

    public Segment(@Nullable TransitionAction startAction, @Nullable TransitionAction endAction) {
        this.startAction = startAction;
        this.endAction = endAction;
    }

    public void start() {
        if (hasStarted() || hasEnded()) {
            throw new IllegalStateException("Segment " + getTransitionTarget() + " already" + (hasStarted() ? "started" : "ended"));
        }
        startAction = new TransitionAction(TransitionType.Start, getTransitionTarget());
    }

    public void end() {
        if (hasEnded()) {
            throw new IllegalStateException("Segment " + getTransitionTarget() + " already ended");
        }
        endAction = new TransitionAction(TransitionType.End, getTransitionTarget());
    }

    public boolean hasStarted() {
        return startAction != null;
    }

    public boolean hasEnded() {
        return endAction != null;
    }

    @Nullable
    public TransitionAction getStartAction() {
        return startAction;
    }

    @Nullable
    public TransitionAction getEndAction() {
        return endAction;
    }
}
