package com.leaders.gamelogic.actions;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.enums.GameActionType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class RecruitmentAction implements IGameAction {
    @Override
    public GameActionType getActionType() {
        return GameActionType.Recruitment;
    }

    @NonNull
    private final List<RecruitmentActionMotion> motions;

    public RecruitmentAction(@NonNull List<RecruitmentActionMotion> motions) {
        this.motions = new ArrayList<>(motions);
    }

    @NonNull
    public List<RecruitmentActionMotion> getMotions() {
        return Collections.unmodifiableList(motions);
    }
}