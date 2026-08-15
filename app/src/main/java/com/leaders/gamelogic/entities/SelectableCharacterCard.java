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
}
