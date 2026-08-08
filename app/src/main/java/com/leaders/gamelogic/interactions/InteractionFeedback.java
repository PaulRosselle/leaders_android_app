package com.leaders.gamelogic.interactions;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.actions.CharacterActionMotion;

public final class InteractionFeedback {
    @NonNull
    private final CharacterActionMotion characterActionMotion;

    public InteractionFeedback(@NonNull CharacterActionMotion characterActionMotion) {
        this.characterActionMotion = characterActionMotion;
    }

    @NonNull
    public CharacterActionMotion getCharacterActionMotion() {
        return characterActionMotion;
    }
}
