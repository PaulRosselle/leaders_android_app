package com.leaders.gamelogic.handlers;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.entities.Game;

public abstract class GameActionHandler {
    @NonNull
    protected final Game game;

    public GameActionHandler(@NonNull Game game) {
        this.game = game;
    }

    public abstract void doAction();
    public abstract void undoAction();
}
