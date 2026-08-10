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
    private final List<RecruitmentActionTarget> targets;

    public RecruitmentAction(@NonNull List<RecruitmentActionTarget> targets) {
        this.targets = new ArrayList<>(targets);
    }

    @NonNull
    public List<RecruitmentActionTarget> getTargets() {
        return Collections.unmodifiableList(targets);
    }
}