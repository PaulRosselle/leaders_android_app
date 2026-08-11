package com.leaders.gamelogic.interactions;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.entities.Character;

import java.util.ArrayList;
import java.util.List;

public final class CharacterActionBuilder extends GameActionBuilder {

    @NonNull
    private final Character sourceCharacter;

    public CharacterActionBuilder(@NonNull Character sourceCharacter,
                                  @NonNull List<InteractionResult> interactionResults,
                                  @NonNull List<InteractionFeedback> interactionFeedbacks) {
        super(interactionResults, interactionFeedbacks);
        this.sourceCharacter = sourceCharacter;
    }

    public CharacterActionBuilder(@NonNull CharacterActionBuilder refBuilder) {
        this(refBuilder.sourceCharacter,
                new ArrayList<>(refBuilder.getResults()),
                new ArrayList<>(refBuilder.getFeedbacks()));
    }

    @NonNull
    public Character getSourceCharacter() {
        return sourceCharacter;
    }
}