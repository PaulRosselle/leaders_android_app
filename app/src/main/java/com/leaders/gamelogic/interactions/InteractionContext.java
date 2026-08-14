package com.leaders.gamelogic.interactions;

import com.leaders.gamelogic.entities.Character;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class InteractionContext {
    @Nullable
    private final Character character;

    public InteractionContext() {
        this.character = null;
    }

    public InteractionContext(@NonNull Character character) {
        this.character = character;
    }

    @Nullable
    public Character getCharacter() {
        return character;
    }
}