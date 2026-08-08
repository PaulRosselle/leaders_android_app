package com.leaders.gamelogic.actions;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.enums.GameActionType;

import java.util.List;

public final class CharacterAction implements IGameAction {
    @Override
    public GameActionType getActionType() {
        return GameActionType.CharacterAction;
    }

    @NonNull
    private final Character srcCharacter;

    @NonNull
    private final List<CharacterActionMotion> motions;

    public CharacterAction(@NonNull Character srcCharacter, @NonNull List<CharacterActionMotion> motions) {
        this.srcCharacter = srcCharacter;
        this.motions = motions;
    }

    @NonNull
    public Character getSrcCharacter() {
        return srcCharacter;
    }

    @NonNull
    public List<CharacterActionMotion> getMotions() {
        return motions;
    }
}
