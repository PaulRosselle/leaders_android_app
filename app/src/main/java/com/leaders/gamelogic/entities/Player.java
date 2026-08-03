package com.leaders.gamelogic.entities;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.enums.TeamColor;

public class Player {
    @NonNull
    private final TeamColor teamColor;
    @NonNull
    private final String name;

    public Player(@NonNull TeamColor teamColor, @NonNull String name) {
        this.teamColor = teamColor;
        this.name = name;
    }

    @NonNull
    public TeamColor getTeamColor() {
        return teamColor;
    }

    @NonNull
    public String getName() {
        return name;
    }
}
