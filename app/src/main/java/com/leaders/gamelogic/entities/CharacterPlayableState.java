package com.leaders.gamelogic.entities;

import androidx.annotation.NonNull;

public final class CharacterPlayableState {
    @NonNull
    private final Character character;
    @NonNull
    private final Position position;
    private final boolean mandatory;
    private final boolean canUseActiveAbility;

    public CharacterPlayableState(@NonNull Character character, @NonNull Position position,
                                  boolean mandatory, boolean canUseActiveAbility) {
        this.character = character;
        this.position = position;
        this.mandatory = mandatory;
        this.canUseActiveAbility = canUseActiveAbility;
    }

    @NonNull
    public Character getCharacter() {
        return character;
    }

    @NonNull
    public Position getPosition() {
        return position;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public boolean isCanUseActiveAbility() {
        return canUseActiveAbility;
    }
}
