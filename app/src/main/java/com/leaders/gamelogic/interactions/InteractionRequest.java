package com.leaders.gamelogic.interactions;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.gamelogic.entities.Position;
import com.leaders.gamelogic.enums.CharacterCard;

import java.util.List;
import java.util.Map;

/**
 * Context token emitted whenever an interactive step is required.
 * <p>
 * Describes the expected input type, the corresponding legal values,
 * and the exhaustive list of accepted response types.
 */
public final class InteractionRequest {
    @NonNull
    private final InteractionType type;

    @Nullable
    private final List<CharacterCard> legalCards;

    @Nullable
    private final Map<TargetCategory, List<Position>> legalPositions;

    @NonNull
    private final List<InteractionResultType> legalResults;

    public InteractionRequest(@NonNull InteractionType type, @Nullable List<CharacterCard> legalCards,
                              @Nullable Map<TargetCategory, List<Position>> legalPositions,
                              @NonNull List<InteractionResultType> legalResults) {
        this.type = type;
        this.legalCards = legalCards;
        this.legalPositions = legalPositions;
        this.legalResults = legalResults;
    }

    @NonNull
    public InteractionType getType() {
        return type;
    }

    @Nullable
    public List<CharacterCard> getLegalCards() {
        return legalCards;
    }

    @Nullable
    public Map<TargetCategory, List<Position>> getLegalPositions() {
        return legalPositions;
    }

    @NonNull
    public List<InteractionResultType> getLegalResults() {
        return legalResults;
    }
}
