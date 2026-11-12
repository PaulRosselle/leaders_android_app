package com.leaders.gamelogic.entities;

import androidx.annotation.NonNull;

import java.util.UUID;

public final class TransformedState {
    @NonNull
    private final Character characterBefore;
    @NonNull
    private final Character characterAfter;

    public TransformedState(@NonNull Character characterBefore, @NonNull Character characterAfter) {
        if (!characterBefore.getId().equals(characterAfter.getId())) {
            throw new IllegalArgumentException("Cannot create a transformed state for two different characters");
        }

        this.characterBefore = characterBefore;
        this.characterAfter = characterAfter;
    }

    @NonNull
    public UUID getCharacterId() {
        return characterAfter.getId();
    }

    @NonNull
    public Character getCharacterBefore() {
        return characterBefore;
    }

    @NonNull
    public Character getCharacterAfter() {
        return characterAfter;
    }
}
