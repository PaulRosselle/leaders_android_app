package com.leaders.gamelogic.actions;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.enums.GameActionType;

import java.util.ArrayList;

public final class CharacterAction implements IGameAction {
    @Override
    public GameActionType getActionType() {
        return GameActionType.CharacterAction;
    }

    private final boolean isActiveAbility;
    @NonNull
    private final Character srcCharacter;
    @NonNull
    private final ArrayList<CharacterActionTarget> targets;

    public CharacterAction(boolean isActiveAbility, @NonNull Character srcCharacter, @NonNull ArrayList<CharacterActionTarget> targets) {
        this.isActiveAbility = isActiveAbility;
        this.srcCharacter = srcCharacter;
        this.targets = targets;
    }

    public boolean isActiveAbility() {
        return isActiveAbility;
    }

    @NonNull
    public Character getSrcCharacter() {
        return srcCharacter;
    }

    @NonNull
    public ArrayList<CharacterActionTarget> getTargets() {
        return targets;
    }
}
