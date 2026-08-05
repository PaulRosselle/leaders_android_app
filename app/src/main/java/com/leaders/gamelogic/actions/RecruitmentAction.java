package com.leaders.gamelogic.actions;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.entities.Position;
import com.leaders.gamelogic.enums.GameActionType;

public final class RecruitmentAction implements IGameAction {
    @Override
    public GameActionType getActionType() {
        return GameActionType.Recruitment;
    }

    @NonNull
    private final Character character;
    @NonNull
    private final Position destPos;

    public RecruitmentAction(@NonNull Character character, @NonNull Position destPos) {
        this.character = character;
        this.destPos = destPos;
    }

    @NonNull
    public com.leaders.gamelogic.entities.Character getCharacter() {
        return character;
    }

    @NonNull
    public Position getDestPos() {
        return destPos;
    }
}
