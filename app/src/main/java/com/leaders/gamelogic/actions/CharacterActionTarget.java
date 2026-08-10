package com.leaders.gamelogic.actions;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.entities.Position;

public final class CharacterActionTarget {
    @NonNull
    private final Character character;
    @Nullable
    private final Position originPos;
    @Nullable
    private final Position destPos;

    public CharacterActionTarget(@NonNull Character character, @Nullable Position originPos, @Nullable Position destPos) {
        this.character = character;
        this.originPos = originPos;
        this.destPos = destPos;
    }

    @NonNull
    public Character getCharacter() {
        return character;
    }

    @Nullable
    public Position getOriginPos() {
        return originPos;
    }
    
    @Nullable
    public Position getDestPos() {
        return destPos;
    }
}
