package com.leaders.gamelogic.entities;

import com.leaders.gamelogic.enums.CharacterType;
import com.leaders.gamelogic.enums.TeamColor;

import java.util.UUID;

public class Character {
    private final UUID id;
    private final CharacterType characterType;
    private final TeamColor teamColor;

    public Character(UUID id, CharacterType characterType, TeamColor teamColor) {
        this.id = id;
        this.characterType = characterType;
        this.teamColor = teamColor;
    }

    public UUID getId() {
        return id;
    }

    public CharacterType getCharacterType() {
        return characterType;
    }

    public TeamColor getTeamColor() {
        return teamColor;
    }
}
