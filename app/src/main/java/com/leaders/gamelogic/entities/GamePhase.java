package com.leaders.gamelogic.entities;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.enums.GamePhaseType;

public class GamePhase {
    @NonNull
    private final GamePhaseType phaseType;

    @NonNull
    private final Player phasePlayer;

    public GamePhase(@NonNull GamePhaseType phaseType, @NonNull Player phasePlayer) {
        this.phaseType = phaseType;
        this.phasePlayer = phasePlayer;
    }

    @NonNull
    public GamePhaseType getPhaseType() {
        return phaseType;
    }

    @NonNull
    public Player getPhasePlayer() {
        return phasePlayer;
    }
}
