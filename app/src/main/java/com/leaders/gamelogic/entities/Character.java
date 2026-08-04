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

    private Character(@NonNull UUID id, @NonNull CharacterType characterType, @NonNull TeamColor teamColor) {
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


    /**
     * Creates a new character with a random UUID.
     *
     * @param characterType The type of character to create.
     * @param teamColor    The team color for the character.
     * @return A new Character instance.
     */
    public static Character create(@NonNull CharacterType characterType, @NonNull TeamColor teamColor) {
        return new Character(UUID.randomUUID(), characterType, teamColor);
    }

    /**
     * Creates a new character instance based on an existing one, but with potentially different type or color.
     * The ID is preserved from the original character.
     *
     * @param character     The original character to transform.
     * @param characterType The new character type.
     * @param teamColor     The new team color.
     * @return A new Character instance with the same ID as the input character.
     */
    public static Character transform(@NonNull Character character,
                                      @NonNull CharacterType characterType, @NonNull TeamColor teamColor) {
        return new Character(character.id, characterType, teamColor);
    }

}
