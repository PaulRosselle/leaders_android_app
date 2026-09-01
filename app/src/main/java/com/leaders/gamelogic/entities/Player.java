package com.leaders.gamelogic.entities;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.enums.TeamColor;

public final class Player {
    @NonNull
    private final TeamColor teamColor;
    @NonNull
    private final String name;

    public Player(@NonNull TeamColor teamColor, @NonNull String name) {
        this.teamColor = teamColor;
        this.name = name;
    }

    public Player(@NonNull Player refPlayer) {
        this(refPlayer.getTeamColor(), refPlayer.getName());
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
