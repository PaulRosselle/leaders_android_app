package com.leaders.gamelogic.interactions;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Response returned by the external caller in reply to an InteractionRequest.
 * <p>
 * Carries the response type and, depending on that type, an optional chosen value.
 */
public final class InteractionResult {
    @NonNull
    private final InteractionResultType resultType;

    @NonNull
    private final InteractionContext context;

    @Nullable
    private final InteractionTarget chosenTarget;

    /**
     * Creates an interaction result.
     *
     * <p>The chosen values are optional because their relevance depends on the result type.
     * The validity of the combination between the result type and the chosen values is
     * checked by the interaction handling logic.</p>
     *
     * @param resultType the type of response provided by the caller
     * @param chosenTarget the selected target, if applicable
     */
    public InteractionResult(@NonNull InteractionResultType resultType,
                             @NonNull InteractionContext context,
                             @Nullable InteractionTarget chosenTarget) {
        // No defensive copies are necessary since every of the used types are immutable
        this.resultType = resultType;
        this.context = context;
        this.chosenTarget = chosenTarget;
    }

    @NonNull
    public InteractionResultType getResultType() {
        return resultType;
    }

    @NonNull
    public InteractionContext getContext() {
        return context;
    }

    @Nullable
    public InteractionTarget getChosenTarget() {
        return chosenTarget;
    }
}
