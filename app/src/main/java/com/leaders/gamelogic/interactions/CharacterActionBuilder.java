package com.leaders.gamelogic.interactions;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.entities.Character;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CharacterActionBuilder {

    @NonNull
    private final Character sourceCharacter;

    @NonNull
    private final List<InteractionResult> interactionResults;

    @NonNull
    private final List<InteractionFeedback> interactionFeedbacks;

    public CharacterActionBuilder(@NonNull Character sourceCharacter,
                                  @NonNull List<InteractionResult> interactionResults,
                                  @NonNull List<InteractionFeedback> interactionFeedbacks) {
        this.sourceCharacter = sourceCharacter;
        this.interactionResults = interactionResults;
        this.interactionFeedbacks = interactionFeedbacks;
    }

    public CharacterActionBuilder(@NonNull CharacterActionBuilder refBuilder) {
        this(refBuilder.sourceCharacter,
                new ArrayList<>(refBuilder.interactionResults),
                new ArrayList<>(refBuilder.interactionFeedbacks));
    }

    @NonNull
    public Character getSourceCharacter() {
        return sourceCharacter;
    }

    @NonNull
    public List<InteractionResult> getInteractionResults() {
        return Collections.unmodifiableList(interactionResults);
    }

    @NonNull
    public List<InteractionFeedback> getInteractionFeedbacks() {
        return Collections.unmodifiableList(interactionFeedbacks);
    }

    public void addResult(@NonNull InteractionResult result) {
        interactionResults.add(result);
    }

    public void addFeedback(@NonNull InteractionFeedback feedback) {
        interactionFeedbacks.add(feedback);
    }
}