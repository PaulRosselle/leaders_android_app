package com.leaders.app.entities;

import androidx.annotation.NonNull;

import com.leaders.app.enums.LeaderType;
import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.entities.Player;
import com.leaders.gamelogic.enums.TeamColor;

public final class PlayerSetup {
    @NonNull
    private final String name;
    @NonNull
    private final TeamColor teamColor;
    @NonNull
    private final LeaderType leaderType;

    public PlayerSetup(@NonNull String name, @NonNull TeamColor teamColor, @NonNull LeaderType leaderType) {
        this.name = name;
        this.teamColor = teamColor;
        this.leaderType = leaderType;
    }

    public Player createPlayer() {
        return new Player(teamColor, name);
    }

    public Character createLeader() {
        return Character.create(leaderType.getCharacterType(), teamColor);
    }
}
