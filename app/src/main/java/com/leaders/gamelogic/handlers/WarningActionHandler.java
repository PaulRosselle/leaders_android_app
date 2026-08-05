package com.leaders.gamelogic.handlers;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.actions.WarningAction;
import com.leaders.gamelogic.entities.Game;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.gamelogic.enums.WarningType;

public final class WarningActionHandler extends GameActionHandler {
    @NonNull
    private final WarningAction warningAction;

    public WarningActionHandler(@NonNull Game game, @NonNull WarningAction warningAction) {
        super(game);
        this.warningAction = warningAction;
    }

    private void applyWarningChange(int countChange) {
        TeamColor warningTeamColor = warningAction.getTeamColor();
        WarningType warningType = warningAction.getWarningType();
        int currentWarningCount = game.getPlayerWarningCount(warningTeamColor, warningType);
        game.setPlayerWarningCount(warningTeamColor, warningType, currentWarningCount + countChange);
    }

    @Override
    public void doAction() {
        applyWarningChange(warningAction.getCountChange());
    }

    @Override
    public void undoAction() {
        applyWarningChange(-warningAction.getCountChange());
    }
}
