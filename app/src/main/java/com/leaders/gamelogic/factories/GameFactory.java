package com.leaders.gamelogic.factories;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.actions.IGameAction;
import com.leaders.gamelogic.entities.Board;
import com.leaders.gamelogic.entities.Game;
import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.gamelogic.historyentries.IHistoryEntry;
import com.leaders.gamelogic.historyentries.segments.BanishmentPhase;
import com.leaders.gamelogic.historyentries.segments.Turn;
import com.leaders.gamelogic.historyentries.segments.TurnPhase;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

public final class GameFactory {

    private GameFactory() {
    }

    @NonNull
    public static Game create(@NonNull GameHistory gameHistory) {
        // First, create a default game instance
        Game game = new Game(new Board(),
                new ArrayList<>(), // recruitableCards
                new ArrayList<>(), // recruitedCharacters
                new ArrayList<>(), // banishedCards
                new EnumMap<>(TeamColor.class) // playerWarnings
        );

        // The config contains every action made before the game started.
        doActions(game, gameHistory.getConfig().getInitialPlacements());

        // Once the game has been fully initialized, play every action in the history.
        for (IHistoryEntry historyEntry : gameHistory.getEntries()) {
            if (historyEntry instanceof Turn) {
                for (TurnPhase phase : ((Turn) historyEntry).getSubPhasesInOrder()) {
                    doActions(game, phase.getActions());
                }
            } else if (historyEntry instanceof BanishmentPhase) {
                doActions(game, ((BanishmentPhase) historyEntry).getActions());
            }
        }

        return game;
    }

    private static void doActions(@NonNull Game game, @NonNull List<IGameAction> actions) {
        for (IGameAction gameAction : actions) {
            GameActionHandlerFactory.create(game, gameAction).doAction();
        }
    }
}