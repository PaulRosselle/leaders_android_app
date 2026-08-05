package com.leaders.gamelogic.handlers;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.actions.TransitionAction;
import com.leaders.gamelogic.entities.Game;

public final class TransitionActionHandler extends GameActionHandler {
    @NonNull
    public TransitionAction transitionAction;

    public TransitionActionHandler(@NonNull Game game, @NonNull TransitionAction transitionAction) {
        super(game);
        this.transitionAction = transitionAction;
    }

    @Override
    public void doAction() {
        // Since transitions have no impact on the game projection, we have no treatment to do here.
        // This class is kept to simplify the generic approach on IGameAction
    }

    @Override
    public void undoAction() {
        // Since transitions have no impact on the game projection, we have no treatment to do here.
        // This class is kept to simplify the generic approach on IGameAction
    }
}
