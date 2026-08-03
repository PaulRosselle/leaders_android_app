package com.leaders.gamelogic.historyentries;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.actions.IGameAction;

import java.util.ArrayList;

public interface IPhase {
    @NonNull
    ArrayList<IGameAction> getActions();
}
