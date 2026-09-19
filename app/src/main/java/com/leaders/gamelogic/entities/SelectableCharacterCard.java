package com.leaders.gamelogic.entities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.gamelogic.enums.CharacterCard;
import com.leaders.gamelogic.enums.CharacterCardSelectionStatus;
import com.leaders.gamelogic.enums.TeamColor;

public final class SelectableCharacterCard {
    @NonNull
    private final CharacterCard characterCard;
    @NonNull
    private final CharacterCardSelectionStatus selectionStatus;
    @Nullable
    private final TeamColor teamColor;


    public SelectableCharacterCard(@NonNull CharacterCard characterCard,
                                   @NonNull CharacterCardSelectionStatus selectionStatus,
                                   @Nullable TeamColor teamColor) {
        this.characterCard = characterCard;
        this.selectionStatus = selectionStatus;
        this.teamColor = teamColor;
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

    @Nullable
    public TeamColor getTeamColor() {
        return teamColor;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(
                characterCard,
                selectionStatus,
                teamColor
        );
    }

    @NonNull
    @Override
    public String toString() {
        return "SelectableCharacterCard{" +
                "characterCard=" + characterCard +
                ", selectionStatus=" + selectionStatus +
                ", teamColor=" + (teamColor != null ? teamColor.name() : "None") +
                '}';
    }
}
