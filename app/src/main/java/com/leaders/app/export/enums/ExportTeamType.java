package com.leaders.app.export.enums;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.enums.TeamColor;

public enum ExportTeamType {
    BLACK,
    WHITE;
    
    public TeamColor getAsTeamColor() {
        switch (this) {
            case BLACK: return TeamColor.Black;
            case WHITE: return TeamColor.White;
            default: throw new IllegalStateException("No team color found for team type: " + this);
        }
    }

    public static ExportTeamType getFromTeamColor(@NonNull TeamColor teamColor) {
        switch (teamColor) {
            case Black: return BLACK;
            case White: return WHITE;
            default: throw new IllegalStateException("No team type found for team color: " + teamColor);
        }
    }
}
