package com.leaders.gamelogic.entities;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.enums.CharacterCard;
import com.leaders.gamelogic.enums.CharacterCardSelectionStatus;

public final class SelectableCharacterCard {
    @NonNull
    private final CharacterCard characterCard;
    @NonNull
    private final CharacterCardSelectionStatus selectionStatus;


    public SelectableCharacterCard(@NonNull CharacterCard characterCard,
                                   @NonNull CharacterCardSelectionStatus selectionStatus) {
        this.characterCard = characterCard;
        this.selectionStatus = selectionStatus;
    }

    @NonNull
    public CharacterCard getCharacterCard() {
        return characterCard;
    }

    @NonNull
    public CharacterCardSelectionStatus getSelectionStatus() {
        return selectionStatus;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SelectableCharacterCard that = (SelectableCharacterCard) o;
        return characterCard == that.characterCard
                && selectionStatus == that.selectionStatus;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(
                characterCard,
                selectionStatus
        );
    }

    @NonNull
    @Override
    public String toString() {
        return "SelectableCharacterCard{" +
                "characterCard=" + characterCard +
                ", selectionStatus=" + selectionStatus +
                '}';
    }
}
