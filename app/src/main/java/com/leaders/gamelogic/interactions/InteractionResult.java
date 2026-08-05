package com.leaders.gamelogic.interactions;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.gamelogic.entities.Position;
import com.leaders.gamelogic.enums.CharacterCard;

/**
 * Response returned by the external caller in reply to an InteractionRequest.
 * <p>
 * Carries the response type and, depending on that type, an optional chosen value.
 */
public final class InteractionResult {
    @NonNull
    private final InteractionResultType resultType;

    @Nullable
    private final CharacterCard chosenCard;

    @Nullable
    private final Position chosenPosition;

    /**
     * Creates an interaction result.
     *
     * <p>The chosen values are optional because their relevance depends on the result type.
     * The validity of the combination between the result type and the chosen values is
     * checked by the interaction handling logic.</p>
     *
     * @param resultType the type of response provided by the caller
     * @param chosenCard the selected card, if applicable
     * @param chosenPosition the selected position, if applicable
     */
    public InteractionResult(@NonNull InteractionResultType resultType, @Nullable CharacterCard chosenCard, @Nullable Position chosenPosition) {
        // No defensive copies are necessary since every of the used types are immutable
        this.resultType = resultType;
        this.chosenCard = chosenCard;
        this.chosenPosition = chosenPosition;
    }

    @NonNull
    public InteractionResultType getResultType() {
        return resultType;
    }

    @Nullable
    public CharacterCard getChosenCard() {
        return chosenCard;
    }

    @Nullable
    public Position getChosenPosition() {
        return chosenPosition;
    }
}
