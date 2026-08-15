package com.leaders.gamelogic.interactions;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.gamelogic.entities.PlayableCharacter;
import com.leaders.gamelogic.entities.Position;
import com.leaders.gamelogic.entities.SelectableCharacterCard;
import com.leaders.gamelogic.enums.CharacterCard;

import java.nio.channels.SelectableChannel;
import java.util.Objects;

public final class InteractionTarget {
    @NonNull
    private final TargetCategory category;

    @Nullable
    private final SelectableCharacterCard chosenSelectableCharacterCard;

    @Nullable
    private final PlayableCharacter chosenPlayableCharacter;

    @Nullable
    private final Position chosenPosition;

    private InteractionTarget(@NonNull TargetCategory category,
                              @Nullable SelectableCharacterCard chosenSelectableCharacterCard,
                              @Nullable PlayableCharacter chosenPlayableCharacter,
                              @Nullable Position chosenPosition) {
        this.category = category;
        if (!(chosenSelectableCharacterCard != null ^ chosenPosition != null ^ chosenPlayableCharacter != null)) {
            throw new IllegalArgumentException("There can be only one target data per interaction");
        }
        this.chosenSelectableCharacterCard = chosenSelectableCharacterCard;
        this.chosenPlayableCharacter = chosenPlayableCharacter;
        this.chosenPosition = chosenPosition;
    }

    public InteractionTarget(@NonNull TargetCategory category, @NonNull SelectableCharacterCard chosenSelectableCharacterCard) {
        this(category, chosenSelectableCharacterCard, null, null);
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
    public SelectableCharacterCard getChosenSelectableCharacterCard() {
        return chosenSelectableCharacterCard;
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
                Objects.equals(chosenSelectableCharacterCard, target.chosenSelectableCharacterCard) &&
                Objects.equals(chosenPlayableCharacter, target.chosenPlayableCharacter) &&
                Objects.equals(chosenPosition, target.chosenPosition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(category, chosenSelectableCharacterCard, chosenPlayableCharacter, chosenPosition);
    }

    @NonNull
    @Override
    public String toString() {
        String cardStr = chosenSelectableCharacterCard != null ? ", card=" + chosenSelectableCharacterCard : "";
        String characterStr = chosenPlayableCharacter != null ? ", character=" + chosenPlayableCharacter : "";
        String positionStr = chosenPosition != null ? ", position=" + chosenPosition : "";
        return "InteractionTarget{category=" + category + cardStr + characterStr + positionStr + '}';
    }
}
