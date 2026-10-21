package com.leaders.gamelogic.interactions;

import com.leaders.gamelogic.entities.Character;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class InteractionContext {
    @Nullable
    private final Character sourceCharacter;
    @Nullable
    private final Character targetCharacter;

    public InteractionContext() {
        this.sourceCharacter = null;
        this.targetCharacter = null;
    }

    public InteractionContext(@NonNull Character sourceAndTargetCharacter) {
        this.sourceCharacter = sourceAndTargetCharacter;
        this.targetCharacter = sourceAndTargetCharacter;
    }

    public InteractionContext(@NonNull Character sourceCharacter,
                              @NonNull Character targetCharacter) {
        this.sourceCharacter = sourceCharacter;
        this.targetCharacter = targetCharacter;
    }

    @Nullable
    public Character getSourceCharacter() {
        return sourceCharacter;
    }

    @Nullable
    public Character getTargetCharacter() {
        return targetCharacter;
    }
}