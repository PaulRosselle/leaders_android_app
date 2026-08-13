package com.leaders.gamelogic;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.entities.Game;
import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.gamelogic.entities.Player;
import com.leaders.gamelogic.enums.GameMode;
import com.leaders.gamelogic.factories.GameFactory;
import com.leaders.gamelogic.interactions.IGameFlowListener;

import java.util.List;

public final class GameHandler {

    @NonNull
    private final Game currentGame;

    @NonNull
    private final GameHistory currentHistory;

    @NonNull
    private final IGameFlowListener gameFlowListener;

    public GameHandler(@NonNull GameHistory currentHistory,
                       @NonNull IGameFlowListener gameFlowListener) {
        this.currentHistory = currentHistory;
        this.currentGame = GameFactory.create(currentHistory);
        this.gameFlowListener = gameFlowListener;
    }

    @NonNull
    public Game getCurrentGame() {
        return currentGame;
    }

    @NonNull
    public GameHistory getCurrentHistory() {
        return currentHistory;
    }

    @NonNull
    public GameMode getGameMode() {
        return currentHistory.getConfig().getGameMode();
    }

    @NonNull
    public List<Player> getPlayers() {
        return currentHistory.getConfig().getPlayers();
    }
}