package com.leaders.gamelogic.entities;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.enums.CharacterType;
import com.leaders.gamelogic.enums.TeamColor;

import java.util.UUID;

public final class Character {
    @NonNull
    private final UUID id;
    @NonNull
    private final CharacterType characterType;
    @NonNull
    private final TeamColor teamColor;

    public Character(@NonNull UUID id, @NonNull CharacterType characterType, @NonNull TeamColor teamColor) {
        this.id = id;
        this.characterType = characterType;
        this.teamColor = teamColor;
    }

    @NonNull
    public UUID getId() {
        return id;
    }

    @NonNull
    public CharacterType getCharacterType() {
        return characterType;
    }

    @NonNull
    public TeamColor getTeamColor() {
        return teamColor;
    }
}
