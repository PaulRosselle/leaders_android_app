package com.leaders.gamelogic.interactions;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.gamelogic.entities.Position;
import com.leaders.gamelogic.enums.CharacterCard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
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

    /**
     * Creates an immutable interaction request.
     *
     * <p>Collections are defensively copied to ensure that the request state cannot be
     * modified externally after creation. This preserves the immutability contract of
     * this context token.</p>
     *
     * @param type the expected interaction type
     * @param legalCards the cards accepted as input, or {@code null} if not applicable
     * @param legalPositions the positions accepted as input grouped by target category,
     *                      or {@code null} if not applicable
     * @param legalResults the exhaustive list of accepted interaction result types
     */
    public InteractionRequest(@NonNull InteractionType type, @Nullable List<CharacterCard> legalCards,
                              @Nullable Map<TargetCategory, List<Position>> legalPositions,
                              @NonNull List<InteractionResultType> legalResults) {
        this.type = type;

        this.legalCards = legalCards != null ? Collections.unmodifiableList(new ArrayList<>(legalCards)) : null;

        if (legalPositions == null) {
            this.legalPositions = null;
        } else {
            Map<TargetCategory, List<Position>> positionsCopy = new EnumMap<>(TargetCategory.class);
            for (Map.Entry<TargetCategory, List<Position>> entry : legalPositions.entrySet()) {
                positionsCopy.put(entry.getKey(), Collections.unmodifiableList(new ArrayList<>(entry.getValue())));
            }
            this.legalPositions = Collections.unmodifiableMap(positionsCopy);
        }

        this.legalResults = Collections.unmodifiableList(new ArrayList<>(legalResults));
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
