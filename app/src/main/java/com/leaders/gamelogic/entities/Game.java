package com.leaders.gamelogic.entities;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.enums.CharacterCard;
import com.leaders.gamelogic.enums.WarningType;

import java.util.List;
import java.util.Map;

public final class Game {
    @NonNull
    private final Board board;
    @NonNull
    private final List<CharacterCard> recruitableCards;
    @NonNull
    private final List<Character> recruitedCharacters;
    @NonNull
    private final List<CharacterCard> banishedCards;
    @NonNull
    private final Map<Player, List<WarningType>> playerWarnings;

    public Game(@NonNull Board board, @NonNull List<CharacterCard> recruitableCards,
                @NonNull List<Character> recruitedCharacters,
                @NonNull List<CharacterCard> banishedCards,
                @NonNull Map<Player, List<WarningType>> playerWarnings) {
        this.board = board;
        this.recruitableCards = recruitableCards;
        this.recruitedCharacters = recruitedCharacters;
        this.banishedCards = banishedCards;
        this.playerWarnings = playerWarnings;
    }

    @NonNull
    public Board getBoard() {
        return board;
    }

    @NonNull
    public List<CharacterCard> getRecruitableCards() {
        return recruitableCards;
    }

    @NonNull
    public List<Character> getRecruitedCharacters() {
        return recruitedCharacters;
    }

    @NonNull
    public List<CharacterCard> getBanishedCards() {
        return banishedCards;
    }

    @NonNull
    public Map<Player, List<WarningType>> getPlayerWarnings() {
        return playerWarnings;
    }
}
