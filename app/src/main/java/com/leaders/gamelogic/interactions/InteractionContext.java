package com.leaders.gamelogic.interactions;

import com.leaders.gamelogic.entities.Character;
import androidx.annotation.NonNull;

public final class InteractionContext {
    @NonNull
    private final Character character;

    public InteractionContext(@NonNull Character character) {
        this.character = character;
    }

    @NonNull
    public Character getCharacter() {
        return character;
    }
}