package com.leaders.gamelogic.historyentries.segments;

import androidx.annotation.Nullable;

import com.leaders.gamelogic.actions.TransitionAction;
import com.leaders.gamelogic.historyentries.IPhase;
import com.leaders.gamelogic.historyentries.Segment;

public abstract class TurnPhase extends Segment implements IPhase {
    public TurnPhase(@Nullable TransitionAction startAction, @Nullable TransitionAction endAction) {
        super(startAction, endAction);
    }
}
