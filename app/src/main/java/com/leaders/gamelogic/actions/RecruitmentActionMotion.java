package com.leaders.gamelogic.actions;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.entities.Position;
import com.leaders.gamelogic.enums.RecruitmentMotionType;

import java.util.Objects;

public final class RecruitmentActionMotion {

    @NonNull
    private final RecruitmentMotionType motionType;

    @NonNull
    private final Character character;

    @NonNull
    private final Position position;

    public RecruitmentActionMotion(@NonNull RecruitmentMotionType motionType,
                                   @NonNull Character character,
                                   @NonNull Position position) {
        this.motionType = motionType;
        this.character = character;
        this.position = position;
    }

    @NonNull
    public RecruitmentMotionType getMotionType() {
        return motionType;
    }

    @NonNull
    public Character getCharacter() {
        return character;
    }

    @NonNull
    public Position getPosition() {
        return position;
    }
}