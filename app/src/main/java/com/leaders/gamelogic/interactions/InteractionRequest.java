package com.leaders.gamelogic.interactions;

import androidx.annotation.NonNull;

import java.util.List;

/**
 * Context token emitted whenever an interactive step is required.
 * <p>
 * Describes the expected input type, the corresponding legal values,
 * and the exhaustive list of accepted response types.
 */
public final class InteractionRequest {
    @NonNull
    private final InteractionType requestType;

    @NonNull
    private final InteractionContext context;

    @NonNull
    private final List<InteractionTarget> legalTargets;

    @NonNull
    private final List<InteractionResultType> legalResults;

    /**
     * Creates an immutable interaction request.
     *
     * <p>Collections are defensively copied to ensure that the request state cannot be
     * modified externally after creation. This preserves the immutability contract of
     * this context token.</p>
     *
     * @param requestType the expected interaction type
     * @param legalTargets the exhaustive list of accepted interaction targets
     * @param legalResults the exhaustive list of accepted interaction result types
     */
    public InteractionRequest(@NonNull InteractionType requestType,
                              @NonNull InteractionContext context,
                              @NonNull List<InteractionTarget> legalTargets,
                              @NonNull List<InteractionResultType> legalResults) {
        this.requestType = requestType;
        this.context = context;
        this.legalTargets = List.copyOf(legalTargets);
        this.legalResults = List.copyOf(legalResults);
    }

    @NonNull
    public InteractionType getRequestType() {
        return requestType;
    }

    @NonNull
    public InteractionContext getContext() {
        return context;
    }

    @NonNull
    public List<InteractionTarget> getLegalTargets() {
        return legalTargets;
    }

    @NonNull
    public List<InteractionResultType> getLegalResults() {
        return legalResults;
    }
}
