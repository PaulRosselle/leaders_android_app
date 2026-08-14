package com.leaders.gamelogic.interactions;

import androidx.annotation.NonNull;

import java.util.Collections;
import java.util.List;

public abstract class GameActionBuilder {
    @NonNull
    private final List<InteractionResult> interactionResults;

    @NonNull
    private final List<InteractionFeedback> interactionFeedbacks;

    public GameActionBuilder(@NonNull List<InteractionResult> interactionResults,
                             @NonNull List<InteractionFeedback> interactionFeedbacks) {
        this.interactionResults = interactionResults;
        this.interactionFeedbacks = interactionFeedbacks;
    }

    @NonNull
    public List<InteractionResult> getResults() {
        return Collections.unmodifiableList(interactionResults);
    }

    @NonNull
    public List<InteractionFeedback> getFeedbacks() {
        return Collections.unmodifiableList(interactionFeedbacks);
    }

    public void addResult(@NonNull InteractionResult result) {
        interactionResults.add(result);
    }

    public void addFeedback(@NonNull InteractionFeedback feedback) {
        interactionFeedbacks.add(feedback);
    }

    /**
     * Returns whether the action build has been canceled.
     *
     * @return {@code true} if the action build has been canceled, {@code false} otherwise
     */
    public boolean isBuildCancelled() {
        for (InteractionResult result : interactionResults) {
            if (result.getResultType() == InteractionResultType.CancelAction) {
                return true;
            }
        }
        return false;
    }
}
