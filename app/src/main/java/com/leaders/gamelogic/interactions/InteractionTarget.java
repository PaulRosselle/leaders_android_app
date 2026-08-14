package com.leaders.gamelogic.interactions;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.entities.CharacterPlayableState;
import com.leaders.gamelogic.entities.Position;
import com.leaders.gamelogic.enums.CharacterCard;

import java.util.Objects;

public final class InteractionTarget {
    @NonNull
    private final TargetCategory category;

    @Nullable
    private final CharacterCard chosenCard;

    @Nullable
    private final CharacterPlayableState chosenCharacterPlayableState;

    @Nullable
    private final Position chosenPosition;

    private InteractionTarget(@NonNull TargetCategory category,
                              @Nullable CharacterCard chosenCard,
                              @Nullable CharacterPlayableState chosenCharacterPlayableState,
                              @Nullable Position chosenPosition) {
        this.category = category;
        if (!(chosenCard != null ^ chosenPosition != null ^ chosenCharacterPlayableState != null)) {
            throw new IllegalArgumentException("There can be only one target data per interaction");
        }
        this.chosenCard = chosenCard;
        this.chosenCharacterPlayableState = chosenCharacterPlayableState;
        this.chosenPosition = chosenPosition;
    }

    public InteractionTarget(@NonNull TargetCategory category, @NonNull CharacterCard chosenCard) {
        this(category, chosenCard, null, null);
    }

    public InteractionTarget(@NonNull TargetCategory category, @NonNull Position chosenPosition) {
        this(category, null, null, chosenPosition);
    }

    public InteractionTarget(@NonNull TargetCategory category, @NonNull CharacterPlayableState chosenCharacterPlayableState) {
        this(category, null, chosenCharacterPlayableState, null);
    }

    @NonNull
    public TargetCategory getCategory() {
        return category;
    }

    @Nullable
    public CharacterCard getChosenCard() {
        return chosenCard;
    }

    @Nullable
    public CharacterPlayableState getChosenCharacterPlayableState() {
        return chosenCharacterPlayableState;
    }

    @Nullable
    public Position getChosenPosition() {
        return chosenPosition;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof InteractionTarget)) {
            return false;
        }

        InteractionTarget target = (InteractionTarget) o;
        return category.equals(target.category) &&
                Objects.equals(chosenCard, target.chosenCard) &&
                Objects.equals(chosenCharacterPlayableState, target.chosenCharacterPlayableState) &&
                Objects.equals(chosenPosition, target.chosenPosition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(category, chosenCard, chosenCharacterPlayableState, chosenPosition);
    }

    @NonNull
    @Override
    public String toString() {
        String cardStr = chosenCard != null ? ", card=" + chosenCard : "";
        String characterStr = chosenCharacterPlayableState != null ? ", character=" + chosenCharacterPlayableState : "";
        String positionStr = chosenPosition != null ? ", position=" + chosenPosition : "";
        return "InteractionTarget{category=" + category + cardStr + characterStr + positionStr + '}';
    }
}
