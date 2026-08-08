package com.leaders.gamelogic.actions;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.enums.CharacterMotionType;

import java.util.List;

public final class CharacterActionMotion {
    @NonNull
    private final CharacterMotionType motionType;

    @NonNull
    private final List<CharacterActionTarget> targets;

    public CharacterActionMotion(@NonNull CharacterMotionType motionType,
                                 @NonNull List<CharacterActionTarget> targets) {
        this.motionType = motionType;
        this.targets = List.copyOf(targets);
    }

    @NonNull
    public CharacterMotionType getMotionType() {
        return motionType;
    }

    @NonNull
    public List<CharacterActionTarget> getTargets() {
        return targets;
    }
}
