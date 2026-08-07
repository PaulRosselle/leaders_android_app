package com.leaders.gamelogic.historyentries.segments;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.gamelogic.actions.IGameAction;
import com.leaders.gamelogic.actions.TransitionAction;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.gamelogic.enums.TransitionTarget;
import com.leaders.gamelogic.historyentries.IHistoryEntry;
import com.leaders.gamelogic.historyentries.IPhase;
import com.leaders.gamelogic.historyentries.Segment;

import java.util.ArrayList;

public final class BanishmentPhase extends Segment implements IHistoryEntry, IPhase {
    @NonNull
    private final ArrayList<IGameAction> actions;
    @NonNull
    private final TeamColor teamColor;

    public BanishmentPhase(@Nullable TransitionAction startAction, @Nullable TransitionAction endAction, @NonNull TeamColor teamColor) {
        super(startAction, endAction);
        this.teamColor = teamColor;
        actions = new ArrayList<>();
    }

    public BanishmentPhase(@NonNull BanishmentPhase refBanishmentPhase) {
        this(refBanishmentPhase.getStartAction(), refBanishmentPhase.getEndAction(), refBanishmentPhase.getTeamColor());
        actions.addAll(refBanishmentPhase.getActions());
    }

    @NonNull
    @Override
    public ArrayList<IGameAction> getActions() {
        return actions;
    }

    @NonNull
    @Override
    public TransitionTarget getTransitionTarget() {
        return TransitionTarget.BanishmentPhase;
    }

    @NonNull
    @Override
    public TeamColor getTeamColor() {
        return teamColor;
    }
}
