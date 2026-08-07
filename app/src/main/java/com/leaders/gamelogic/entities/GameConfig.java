package com.leaders.gamelogic.entities;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.actions.IGameAction;
import com.leaders.gamelogic.enums.CharacterCard;
import com.leaders.gamelogic.enums.GameMode;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class GameConfig {

    @NonNull
    private final List<Player> players;

    @NonNull
    private final Player firstPlayer;

    @NonNull
    private final GameMode gameMode;

    @NonNull
    private final List<CharacterCard> initialRecruitableCards;

    @NonNull
    private final List<IGameAction> initialPlacements;


    public GameConfig(@NonNull List<Player> players, @NonNull Player firstPlayer,
                      @NonNull GameMode gameMode,
                      @NonNull List<CharacterCard> initialRecruitableCards,
                      @NonNull List<IGameAction> initialPlacements) {
        // We make unmodifiable copies of lists to ensure the GameConfig is immutable
        this.players = List.copyOf(players);
        this.firstPlayer = firstPlayer;
        this.gameMode = gameMode;
        this.initialRecruitableCards = List.copyOf(initialRecruitableCards);
        this.initialPlacements = List.copyOf(initialPlacements);
    }

    @NonNull
    public List<Player> getPlayers() {
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
    public List<CharacterCard> getInitialRecruitableCards() {
        return initialRecruitableCards;
    }

    @NonNull
    public List<IGameAction> getInitialPlacements() {
        return initialPlacements;
    }
}
