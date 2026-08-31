package com.leaders.app.enums;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.enums.CharacterCard;
import com.leaders.gamelogic.enums.CharacterType;

import java.util.List;

public enum LeaderType {
    King,
    Queen;

    public static LeaderType getFromCharacterType(@NonNull CharacterType characterType) {
        switch (characterType) {
            case LeaderKing: return King;
            case LeaderQueen: return Queen;
            default: throw new IllegalArgumentException("No leader type found matching: " + characterType);
        }
    }

    public static LeaderType getFromCharacter(@Nullable Character character) {
        if (character == null) {
            throw new IllegalArgumentException("Cannot find a leader type matching an empty character");
        }

        return getFromCharacterType(character.getCharacterType());
    }

    public LeaderType getNext() {
        LeaderType[] values = LeaderType.values();

        int nextIdx = ordinal() + 1;
        if (nextIdx < values.length) {
            return values[nextIdx];
        }

        return values[0];
    }

    public CharacterCard getCharacterCard() {
        switch (this) {
            case King: return CharacterCard.LeaderKing;
            case Queen: return CharacterCard.LeaderQueen;
            default: throw new IllegalArgumentException("No character card found matching: " + this);
        }
    }

    public CharacterType getCharacterType() {
        List<CharacterType> leaderTypes = CharacterType.getCharacterTypesMatchingCard(getCharacterCard());
        if (leaderTypes.size() != 1) {
            throw new IllegalStateException("A leader card should be mapped to a single character type");
        }

        return leaderTypes.get(0);
    }
}
