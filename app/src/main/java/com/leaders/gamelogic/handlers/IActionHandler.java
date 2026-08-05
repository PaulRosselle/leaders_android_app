package com.leaders.gamelogic.handlers;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.entities.Game;

public interface IActionHandler {
    @NonNull
    Game getGame();
    void doAction();
    void undoAction();
}
