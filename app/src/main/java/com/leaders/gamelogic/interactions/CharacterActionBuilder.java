package com.leaders.gamelogic.interactions;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.entities.Character;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Builder used to collect interaction results required to create a CharacterAction.
 * <p>
 * Stores the source character involved in the action and the results provided by
 * the external caller during the interaction phase.
 */
public final class CharacterActionBuilder {

    @NonNull
    private final List<InteractionResult> interactionResults;

    @NonNull
    private final Character sourceCharacter;

    public CharacterActionBuilder(@NonNull Character sourceCharacter) {
        this.interactionResults = new ArrayList<>();
        this.sourceCharacter = sourceCharacter;
    }

    /**
     * Returns the interaction results collected so far.
     *
     * <p>The returned list is read-only and cannot modify the builder internal state.</p>
     *
     * @return the collected interaction results
     */
    @NonNull
    public List<InteractionResult> getInteractionResults() {
        return Collections.unmodifiableList(interactionResults);
    }

    /**
     * Returns the character from which this action is built.
     *
     * @return the source character
     */
    @NonNull
    public Character getSourceCharacter() {
        return sourceCharacter;
    }

    /**
     * Adds an interaction result to the action being built.
     *
     * @param result the result provided by the external caller
     */
    public void addResult(@NonNull InteractionResult result) {
        interactionResults.add(result);
    }
}