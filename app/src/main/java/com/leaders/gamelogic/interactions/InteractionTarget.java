package com.leaders.gamelogic.interactions;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.entities.Position;
import com.leaders.gamelogic.enums.CharacterCard;

import java.util.Objects;

public final class InteractionTarget {
    @NonNull
    private final TargetCategory category;

    @Nullable
    private final CharacterCard chosenCard;

    @Nullable
    private final Character chosenCharacter;

    @Nullable
    private final Position chosenPosition;

    private InteractionTarget(@NonNull TargetCategory category,
                              @Nullable CharacterCard chosenCard,
                              @Nullable Character chosenCharacter,
                              @Nullable Position chosenPosition) {
        this.category = category;
        if (!(chosenCard != null ^ chosenPosition != null ^ chosenCharacter != null)) {
            throw new IllegalArgumentException("There can be only one target data per interaction");
        }
        this.chosenCard = chosenCard;
        this.chosenCharacter = chosenCharacter;
        this.chosenPosition = chosenPosition;
    }

    public InteractionTarget(@NonNull TargetCategory category, @NonNull CharacterCard chosenCard) {
        this(category, chosenCard, null, null);
    }

    public InteractionTarget(@NonNull TargetCategory category, @NonNull Position chosenPosition) {
        this(category, null, null, chosenPosition);
    }

    public InteractionTarget(@NonNull TargetCategory category, @NonNull Character chosenCharacter) {
        this(category, null, chosenCharacter, null);
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
    public Character getChosenCharacter() {
        return chosenCharacter;
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
                Objects.equals(chosenCharacter, target.chosenCharacter) &&
                Objects.equals(chosenPosition, target.chosenPosition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(category, chosenCard, chosenCharacter, chosenPosition);
    }

    @NonNull
    @Override
    public String toString() {
        String cardStr = chosenCard != null ? ", card=" + chosenCard : "";
        String characterStr = chosenCharacter != null ? ", character=" + chosenCharacter : "";
        String positionStr = chosenPosition != null ? ", position=" + chosenPosition : "";
        return "InteractionTarget{category=" + category + cardStr + characterStr + positionStr + '}';
    }
}
