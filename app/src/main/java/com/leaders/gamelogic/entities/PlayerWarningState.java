package com.leaders.gamelogic.entities;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.gamelogic.enums.WarningType;

public class PlayerWarningState {
    @NonNull
    private final TeamColor playerTeamColor;
    @NonNull
    private final WarningType warningType;
    @NonNull
    private final int warningCount;

    public PlayerWarningState(@NonNull TeamColor playerTeamColor,
                              @NonNull WarningType warningType,
                              int warningCount) {
        this.playerTeamColor = playerTeamColor;
        this.warningType = warningType;
        this.warningCount = warningCount;
    }

    @NonNull
    public TeamColor getPlayerTeamColor() {
        return playerTeamColor;
    }

    @NonNull
    public WarningType getWarningType() {
        return warningType;
    }

    public int getWarningCount() {
        return warningCount;
    }
}
