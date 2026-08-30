package com.leaders.app.enums;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.enums.CharacterType;

public enum LeaderType {
    King,
    Queen;

    public static LeaderType getFromCharacterType(@NonNull CharacterType characterType) {
        switch (characterType) {
            case LeaderKing: return King;
            case LeaderQueen: return Queen;
            default: throw new IllegalArgumentException("No leader type found matching characte type: " + characterType);
        }
    }

    public static LeaderType getFromCharacter(@Nullable Character character) {
        if (character == null) {
            throw new IllegalArgumentException("Cannot find a leader type matching an empty character");
        }

        return getFromCharacterType(character.getCharacterType());
    }
}
