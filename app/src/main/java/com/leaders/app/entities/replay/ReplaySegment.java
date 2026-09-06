package com.leaders.app.entities.replay;

import androidx.annotation.NonNull;

import com.leaders.app.utilities.GameActionUtils;
import com.leaders.gamelogic.actions.IGameAction;
import com.leaders.gamelogic.actions.WarningAction;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.gamelogic.historyentries.segments.BanishmentPhase;
import com.leaders.gamelogic.historyentries.segments.Turn;
import com.leaders.gamelogic.historyentries.segments.TurnPhase;

import java.util.ArrayList;
import java.util.List;

public class ReplaySegment {
    @NonNull
    private final TeamColor teamColor;

    @NonNull
    private final List<IGameAction> playableActions;

    @NonNull
    private final List<WarningAction> warningActions;

    public ReplaySegment(@NonNull Turn turn) {
        this.teamColor = turn.getTeamColor();

        this.playableActions = new ArrayList<>();
        this.warningActions = new ArrayList<>();

        for (TurnPhase phase : turn.getSubPhasesInOrder()) {
            for (IGameAction action : phase.getActions()) {
                if (action instanceof WarningAction) {
                    warningActions.add((WarningAction) action);

                } else if (GameActionUtils.isAnimatable(action)) {
                    playableActions.add(action);
                }
            }
        }
    }

    public ReplaySegment(@NonNull BanishmentPhase banishmentPhase) {
        this.teamColor = banishmentPhase.getTeamColor();

        this.playableActions = new ArrayList<>();
        this.warningActions = new ArrayList<>();

        for (IGameAction action : banishmentPhase.getActions()) {
            if (action instanceof WarningAction) {
                warningActions.add((WarningAction) action);

            } else if (GameActionUtils.isAnimatable(action)) {
                playableActions.add(action);
            }
        }
    }

    @NonNull
    public TeamColor getTeamColor() {
        return teamColor;
    }

    @NonNull
    public List<IGameAction> getPlayableActions() {
        return playableActions;
    }

    @NonNull
    public List<WarningAction> getWarningActions() {
        return warningActions;
    }
}
