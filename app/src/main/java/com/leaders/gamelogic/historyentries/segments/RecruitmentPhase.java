package com.leaders.gamelogic.historyentries.segments;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.gamelogic.actions.CharacterAction;
import com.leaders.gamelogic.actions.IGameAction;
import com.leaders.gamelogic.actions.RecruitmentAction;
import com.leaders.gamelogic.actions.TransitionAction;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.gamelogic.enums.TransitionTarget;

import java.util.ArrayList;
import java.util.List;

public final class RecruitmentPhase extends TurnPhase {
    @NonNull
    private final ArrayList<IGameAction> actions;

    public RecruitmentPhase(@Nullable TransitionAction startAction, @Nullable TransitionAction endAction, @NonNull TeamColor turnTeamColor) {
        super(startAction, endAction, turnTeamColor);
        actions = new ArrayList<>();
    }

    public RecruitmentPhase(@NonNull RecruitmentPhase refRecruitmentPhase) {
        this(refRecruitmentPhase.getStartAction(), refRecruitmentPhase.getEndAction(), refRecruitmentPhase.getTurnTeamColor());
        actions.addAll(refRecruitmentPhase.getActions());
    }

    @NonNull
    @Override
    public ArrayList<IGameAction> getActions() {
        return actions;
    }

    /**
     * Retrieves all recruitment actions from the current list of actions.
     * <p>
     * This method filters the actions and returns only those that are instances
     * of {@link RecruitmentAction}. If an action of another type is encountered,
     * an {@link IllegalStateException} is thrown because only character actions
     * are supported during an actions phase.
     *
     * @return a list containing all {@link RecruitmentAction} instances from the current actions
     * @throws IllegalStateException if an action that is not a {@link RecruitmentAction}
     *                              is found in the actions list
     */
    public List<RecruitmentAction> getRecruitmentActions() {
        List<RecruitmentAction> recruitmentActions = new ArrayList<>();
        for (IGameAction action : actions) {
            if (action instanceof RecruitmentAction) {
                recruitmentActions.add((RecruitmentAction) action);
            } else {
                throw new IllegalStateException("RecruitmentAction is the only action type supported with an RecruitmentPhase\"");
            }
        }
        return recruitmentActions;
    }

    @NonNull
    @Override
    public TransitionTarget getTransitionTarget() {
        return TransitionTarget.RecruitmentPhase;
    }
}
