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
    private final List<CharacterActionTarget> targets;

    public CharacterAction(@NonNull Character srcCharacter, @NonNull List<CharacterActionTarget> targets) {
        this.srcCharacter = srcCharacter;
        this.targets = targets;
    }

    @NonNull
    public Character getSrcCharacter() {
        return srcCharacter;
    }

    @NonNull
    public List<CharacterActionTarget> getTargets() {
        return targets;
    }
}
