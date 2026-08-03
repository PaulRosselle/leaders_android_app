package com.leaders.gamelogic.historyentries;

import com.leaders.gamelogic.actions.IGameAction;

import java.util.ArrayList;

public interface IPhase {
    ArrayList<IGameAction> getActions();
}
