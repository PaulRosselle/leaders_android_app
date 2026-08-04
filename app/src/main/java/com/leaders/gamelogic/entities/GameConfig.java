package com.leaders.gamelogic.entities;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.actions.IGameAction;
import com.leaders.gamelogic.enums.CharacterCard;
import com.leaders.gamelogic.enums.GameMode;

public final class GameConfig {
    @NonNull
    private final Player[] players;
    @NonNull
    private final Player firstPlayer;
    @NonNull
    private final GameMode gameMode;
    @NonNull
    private final CharacterCard initialRecruitableCards;
    @NonNull
    private final IGameAction[] initialPlacements;

    public GameConfig(@NonNull Player[] players, @NonNull Player firstPlayer, @NonNull GameMode gameMode,
                      @NonNull CharacterCard initialRecruitableCards, @NonNull IGameAction[] initialPlacements) {
        this.players = players;
        this.firstPlayer = firstPlayer;
        this.gameMode = gameMode;
        this.initialRecruitableCards = initialRecruitableCards;
        this.initialPlacements = initialPlacements;
    }

    @NonNull
    public Player[] getPlayers() {
        return players;
    }

    @NonNull
    public Player getFirstPlayer() {
        return firstPlayer;
    }

    @NonNull
    public GameMode getGameMode() {
        return gameMode;
    }

    @NonNull
    public CharacterCard getInitialRecruitableCards() {
        return initialRecruitableCards;
    }

    @NonNull
    public IGameAction[] getInitialPlacements() {
        return initialPlacements;
    }
}
