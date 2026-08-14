package com.leaders.gamelogic.interactions;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.gamelogic.entities.PlayableCharacter;
import com.leaders.gamelogic.entities.Position;
import com.leaders.gamelogic.enums.CharacterCard;

import java.util.Objects;

public final class InteractionTarget {
    @NonNull
    private final TargetCategory category;

    @Nullable
    private final CharacterCard chosenCard;

    @Nullable
    private final PlayableCharacter chosenPlayableCharacter;

    @Nullable
    private final Position chosenPosition;

    private InteractionTarget(@NonNull TargetCategory category,
                              @Nullable CharacterCard chosenCard,
                              @Nullable PlayableCharacter chosenPlayableCharacter,
                              @Nullable Position chosenPosition) {
        this.category = category;
        if (!(chosenCard != null ^ chosenPosition != null ^ chosenPlayableCharacter != null)) {
            throw new IllegalArgumentException("There can be only one target data per interaction");
        }
        this.chosenCard = chosenCard;
        this.chosenPlayableCharacter = chosenPlayableCharacter;
        this.chosenPosition = chosenPosition;
    }

    public InteractionTarget(@NonNull TargetCategory category, @NonNull CharacterCard chosenCard) {
        this(category, chosenCard, null, null);
    }

    public InteractionTarget(@NonNull TargetCategory category, @NonNull Position chosenPosition) {
        this(category, null, null, chosenPosition);
    }

    public InteractionTarget(@NonNull TargetCategory category, @NonNull PlayableCharacter chosenPlayableCharacter) {
        this(category, null, chosenPlayableCharacter, null);
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
    public PlayableCharacter getChosenCharacterPlayableState() {
        return chosenPlayableCharacter;
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
                Objects.equals(chosenPlayableCharacter, target.chosenPlayableCharacter) &&
                Objects.equals(chosenPosition, target.chosenPosition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(category, chosenCard, chosenPlayableCharacter, chosenPosition);
    }

    @NonNull
    @Override
    public String toString() {
        String cardStr = chosenCard != null ? ", card=" + chosenCard : "";
        String characterStr = chosenPlayableCharacter != null ? ", character=" + chosenPlayableCharacter : "";
        String positionStr = chosenPosition != null ? ", position=" + chosenPosition : "";
        return "InteractionTarget{category=" + category + cardStr + characterStr + positionStr + '}';
    }
}
