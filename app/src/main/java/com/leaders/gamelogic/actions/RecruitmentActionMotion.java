package com.leaders.gamelogic.actions;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.enums.RecruitmentMotionType;

public final class RecruitmentActionMotion {
    @NonNull
    private final RecruitmentMotionType motionType;

    @NonNull
    private final RecruitmentActionTarget target;

    public RecruitmentActionMotion(@NonNull RecruitmentMotionType motionType,
                                   @NonNull RecruitmentActionTarget target) {
        this.motionType = motionType;
        this.target = target;
    }

    @NonNull
    public RecruitmentActionTarget getTarget() {
        return target;
    }

    @NonNull
    public RecruitmentMotionType getMotionType() {
        return motionType;
    }
}