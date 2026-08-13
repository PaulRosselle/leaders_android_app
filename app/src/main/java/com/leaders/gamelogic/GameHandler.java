package com.leaders.gamelogic;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.entities.Game;
import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.gamelogic.entities.GamePhase;
import com.leaders.gamelogic.entities.Player;
import com.leaders.gamelogic.enums.GameMode;
import com.leaders.gamelogic.enums.GamePhaseType;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.gamelogic.factories.GameFactory;
import com.leaders.gamelogic.historyentries.segments.ActionsPhase;
import com.leaders.gamelogic.historyentries.segments.BanishmentPhase;
import com.leaders.gamelogic.historyentries.segments.RecruitmentPhase;
import com.leaders.gamelogic.historyentries.segments.Turn;
import com.leaders.gamelogic.historyentries.segments.TurnEndPhase;
import com.leaders.gamelogic.historyentries.segments.TurnStartPhase;
import com.leaders.gamelogic.interactions.IGameFlowListener;
import com.leaders.gamelogic.queries.GameHistoryQuery;
import com.leaders.gamelogic.queries.PhaseTransitionQuery;

import java.util.List;
import java.util.concurrent.CompletableFuture;

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

    /**
     * Starts the next phase and notifies the game flow listener.
     *
     * @return a future completed when the phase change has been acknowledged
     * @throws IllegalStateException if no current turn exists for a turn-bound phase
     */
    private CompletableFuture<Void> startNextPhaseAsync() {
        GamePhase nextPhase = PhaseTransitionQuery.getNextPhase(currentGame, currentHistory);
        TeamColor nextPhaseTeamColor = nextPhase.getPhasePlayer().getTeamColor();
        if (nextPhase.getPhaseType() == GamePhaseType.TurnStart) {
            Turn nextTurn = new Turn(nextPhaseTeamColor);
            currentHistory.getEntries().add(nextTurn);
            nextTurn.start();
            nextTurn.getSubPhase(GamePhaseType.TurnStart).start();
        } else if (nextPhase.getPhaseType() == GamePhaseType.Banishment) {
            BanishmentPhase banishmentPhase = new BanishmentPhase(nextPhaseTeamColor);
            currentHistory.getEntries().add(banishmentPhase);
            banishmentPhase.start();
        } else {
            Turn currentTurn = GameHistoryQuery.findCurrentTurn(currentHistory);
            if (currentTurn == null) {
                throw new IllegalStateException("All transitions besides \"TurnStart\" and \"Banishment\" should imply a turn in progress");
            }
            currentTurn.getSubPhase(nextPhase.getPhaseType()).start();
        }

        return gameFlowListener.onPhaseChanged(nextPhase);
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