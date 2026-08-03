package com.leaders.gamelogic.actions;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.enums.GameActionType;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.gamelogic.enums.WarningType;

public final class WarningAction implements IGameAction {
    @Override
    public GameActionType getActionType() {
        return GameActionType.Warning;
    }
    @NonNull
    private final WarningType warningType;
    @NonNull
    private final TeamColor teamColor;

    public WarningAction(@NonNull WarningType warningType, @NonNull TeamColor teamColor) {
        this.warningType = warningType;
        this.teamColor = teamColor;
    }

    @NonNull
    public WarningType getWarningType() {
        return warningType;
    }

    @NonNull
    public TeamColor getTeamColor() {
        return teamColor;
    }
}
