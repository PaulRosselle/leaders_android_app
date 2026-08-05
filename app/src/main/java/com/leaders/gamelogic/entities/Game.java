package com.leaders.gamelogic.entities;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.enums.CharacterCard;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.gamelogic.enums.WarningType;

import java.util.EnumMap;
import java.util.List;

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
    private final EnumMap<TeamColor, EnumMap<WarningType, Integer>> playerWarnings;

    public Game(@NonNull Board board, @NonNull List<CharacterCard> recruitableCards,
                @NonNull List<Character> recruitedCharacters,
                @NonNull List<CharacterCard> banishedCards,
                @NonNull EnumMap<TeamColor, EnumMap<WarningType, Integer>> playerWarnings) {
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

    /**
     * Returns the current number of warnings of the specified type for a player team.
     *
     * <p>The absence of an entry in the underlying map represents a warning count of zero.
     * This avoids storing unnecessary zero values and keeps the projection compact.</p>
     *
     * @param playerTeamColor the team whose warnings must be retrieved
     * @param warningType the type of warning to retrieve
     * @return the current warning count, or {@code 0} if no warning of this type is active
     */
    public int getPlayerWarningCount(@NonNull TeamColor playerTeamColor, @NonNull WarningType warningType) {
        EnumMap<WarningType, Integer> teamWarnings = playerWarnings.get(playerTeamColor);
        // A missing team entry means that this team currently has no active warnings.
        if (teamWarnings == null) {
            return 0;
        }
        // Java collections cannot store primitive int values. A null Integer is treated as
        // an absent warning count and therefore interpreted as zero.
        Integer count = teamWarnings.get(warningType);
        return count == null ? 0 : count;
    }

    /**
     * Updates the current number of warnings of the specified type for a player team.
     *
     * <p>A warning count of zero is represented by the absence of an entry in the underlying
     * map. Therefore, setting a warning count to zero removes the corresponding warning
     * instead of storing a zero value.</p>
     *
     * <p>This method maintains the following invariant:
     * <ul>
     *     <li>no warning type is stored with a count of zero;</li>
     *     <li>no team is stored if it has no active warnings.</li>
     * </ul>
     * </p>
     *
     * @param playerTeamColor the team whose warnings must be updated
     * @param warningType the type of warning to update
     * @param warningCount the new warning count, must be greater than or equal to zero
     * @throws IllegalArgumentException if {@code warningCount} is negative
     */
    public void setPlayerWarningCount(@NonNull TeamColor playerTeamColor, @NonNull WarningType warningType, int warningCount) {
        if (warningCount < 0) {
            throw new IllegalArgumentException("Warning count cannot be negative");
        }

        // A zero warning count is represented by the absence of an entry in the map.
        // Removing empty nested maps also avoids keeping teams without active warnings.
        if (warningCount == 0) {
            EnumMap<WarningType, Integer> teamWarnings = playerWarnings.get(playerTeamColor);
            if (teamWarnings != null) {
                teamWarnings.remove(warningType);
                if (teamWarnings.isEmpty()) {
                    playerWarnings.remove(playerTeamColor);
                }
            }
            return;
        }
        // Creates the team's warning map only when the first warning of this team is added.
        // computeIfAbsent avoids creating an unused EnumMap when the team already exists.
        playerWarnings.computeIfAbsent(playerTeamColor, key -> new EnumMap<>(WarningType.class))
                .put(warningType, warningCount);
    }
}
