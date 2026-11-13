package com.leaders.app.entities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.app.utilities.CharacterCardUtils;
import com.leaders.gamelogic.enums.CharacterCard;

import java.util.Objects;

/**
 * Represents the user's selection of an alternative character.
 * Stores the base character and the selected alternative, if any.
 * A null alternative indicates that the base character is selected.
 */
public class AltCharacterSelection {
    @NonNull
    private final CharacterCard character;
    @Nullable
    private CharacterCard selectedAlternative;

    public AltCharacterSelection(@NonNull CharacterCard character,
                                 @Nullable CharacterCard selectedAlternative) {
        if (!CharacterCardUtils.hasAlternativeCharacter(character)) {
            throw new IllegalArgumentException(character + "has no alternative character");
        }

        this.character = character;
        setSelectedAlternative(selectedAlternative);
    }

    public void setSelectedAlternative(@Nullable CharacterCard selectedAlternative) {
        if (selectedAlternative != null &&
                CharacterCardUtils.getBaseCharacter(selectedAlternative) != character) {
            throw new IllegalArgumentException(selectedAlternative + " is not a valid alternative for " + character);
        }

        this.selectedAlternative = selectedAlternative;
    }

    @NonNull
    public CharacterCard getCharacter() {
        return character;
    }

    @Nullable
    public CharacterCard getSelectedAlternative() {
        return selectedAlternative;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AltCharacterSelection)) return false;
        AltCharacterSelection that = (AltCharacterSelection) o;
        return character == that.character;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(character);
    }

    @NonNull
    @Override
    public String toString() {
        return "AltCharacterSelection{" +
                "character=" + character +
                ", selectedAlternative=" + selectedAlternative +
                '}';
    }
}
