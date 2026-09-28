package com.leaders.app.entities.replay;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.gamelogic.actions.IGameAction;
import com.leaders.gamelogic.actions.WarningAction;

import java.util.List;

public final class ReplayStep {
    @Nullable
    private final IGameAction action;
    @NonNull
    private final List<WarningAction> warningActions;

    public ReplayStep(@Nullable IGameAction action, @NonNull List<WarningAction> warningActions) {
        this.action = action;
        this.warningActions = List.copyOf(warningActions);
    }

    @Nullable
    public IGameAction getAction() {
        return action;
    }

    @NonNull
    public List<WarningAction> getWarningActions() {
        return warningActions;
    }
}
