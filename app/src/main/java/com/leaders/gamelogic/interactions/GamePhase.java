package com.leaders.gamelogic.interactions;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.entities.Player;
import com.leaders.gamelogic.enums.GamePhaseType;

public final class GamePhase {
    @NonNull
    private final GamePhaseType gamePhaseType;
    @NonNull
    private final Player phasePlayer;

    public GamePhase(@NonNull GamePhaseType gamePhaseType, @NonNull Player phasePlayer) {
        this.gamePhaseType = gamePhaseType;
        this.phasePlayer = phasePlayer;
    }

    @NonNull
    public GamePhaseType getGamePhaseType() {
        return gamePhaseType;
    }

    @NonNull
    public Player getPhasePlayer() {
        return phasePlayer;
    }
}
