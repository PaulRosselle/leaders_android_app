package com.leaders.gamelogic.queries;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.actions.RecruitmentAction;
import com.leaders.gamelogic.actions.RecruitmentActionMotion;
import com.leaders.gamelogic.entities.Cell;
import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.entities.Game;
import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.gamelogic.entities.SelectableCharacterCard;
import com.leaders.gamelogic.enums.CharacterCard;
import com.leaders.gamelogic.enums.CharacterCardSelectionStatus;
import com.leaders.gamelogic.enums.CharacterType;
import com.leaders.gamelogic.enums.GameMode;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.gamelogic.historyentries.IPhase;
import com.leaders.gamelogic.historyentries.segments.RecruitmentPhase;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class RecruitmentQuery {

    private RecruitmentQuery() {
        throw new AssertionError("Cannot instantiate utility class");
    }

    public static boolean canRecruit(@NonNull Game game, @NonNull GameHistory gameHistory, @NonNull TeamColor teamColor) {
        return getRecruitmentLimit(game, gameHistory, teamColor) > getCurrentRecruitmentCount(gameHistory)
                && !BoardQuery.getRecruitmentCells(game.getBoard(), teamColor).isEmpty();
    }

    private static int getCurrentRecruitmentCount(@NonNull GameHistory gameHistory) {
        IPhase currentPhase = GameHistoryQuery.findCurrentPhase(gameHistory);
        if (!(currentPhase instanceof RecruitmentPhase)) {
           return 0;
        }
        return ((RecruitmentPhase) currentPhase).getRecruitmentActions().size();
    }

    /**
     * Returns the number of cards that can be recruited during the current recruitment phase.
     */
    private static int getRecruitmentLimit(@NonNull Game game, @NonNull GameHistory gameHistory, @NonNull TeamColor teamColor) {
        int recruitmentCount = getRecruitedCards(game, teamColor, false).size();

        // The 2nd player is allowed to recruit twice during their first recruitment phase.
        if (recruitmentCount == 0 && teamColor != gameHistory.getConfig().getFirstPlayer().getTeamColor()) {
            return 2;
        }

        // A full team is made of 5 character cards (a leader card + 4 recruited cards).
        final int fullTeamSize = 4;
        // 1 card per phase can be recruited until the team is full.
        return recruitmentCount < fullTeamSize ? 1 : 0;
    }

    @NonNull
    private static List<CharacterCard> getGameModeRecruitmentCards(@NonNull Game game,
                                                                   @NonNull GameHistory gameHistory) {
        // The recruitment is restricted to a pool of 3 randomly chosen cards in Discovery mode
        if (gameHistory.getConfig().getGameMode() == GameMode.Discovery) {
            return game.getRecruitableCards().stream().limit(3).collect(Collectors.toList());
        }
        // By default, all recruitable cards are returned
        return game.getRecruitableCards();
    }

    /**
     * Returns the list of cards recruitable during the current recruitment phase.
     */
    @NonNull
    private static List<CharacterCard> getValidRecruitmentCards(@NonNull Game game,
                                                                @NonNull GameHistory gameHistory) {
        // We only return a list if a recruitment phase is in progress.
        IPhase currentPhase = GameHistoryQuery.findCurrentPhase(gameHistory);
        if (!(currentPhase instanceof RecruitmentPhase)) {
            throw new IllegalStateException("Cannot get valid recruitment cards outside of a recruitment phase");
        }

        RecruitmentPhase recruitmentPhase = (RecruitmentPhase) currentPhase;
        TeamColor recruitmentTeamColor = recruitmentPhase.getTurnTeamColor();
        // No recruitment can be made without at least one available recruitment cell.
        List<Cell> recruitmentCells = BoardQuery.getRecruitmentCells(game.getBoard(), recruitmentTeamColor);

        if (recruitmentCells.isEmpty()) {
            return List.of();
        }

        // We get every card already recruited this phase.
        Set<CharacterCard> recruitedCharacterCards = new HashSet<>();
        for (RecruitmentAction recruitmentAction : recruitmentPhase.getRecruitmentActions()) {
            for (RecruitmentActionMotion motion : recruitmentAction.getMotions()) {
                recruitedCharacterCards.add(motion.getCharacter().getCharacterType().getCharacterCard());
            }
        }


        if (recruitedCharacterCards.size() >= getRecruitmentLimit(game, gameHistory, recruitmentTeamColor)) {
            return List.of();
        }

        // If the recruited card count is still under the limit, we can return every card
        // with characters able to be placed on the board.
        List<CharacterCard> recruitableCards = new java.util.ArrayList<>();
        for (CharacterCard recruitableCard : getGameModeRecruitmentCards(game, gameHistory)) {
            if (CharacterType.getCharacterTypesMatchingCard(recruitableCard).size() <= recruitmentCells.size()) {
                recruitableCards.add(recruitableCard);
            }
        }
        return recruitableCards;


    }

    @NonNull
    public static List<SelectableCharacterCard> getSelectableRecruitmentCards(@NonNull Game game,
                                                                              @NonNull GameHistory gameHistory) {
        List<SelectableCharacterCard> selectableCards = new ArrayList<>();
        List<CharacterCard> validRecruitableCards = getValidRecruitmentCards(game, gameHistory);
        // First, we add recruitable cards
        for (CharacterCard recruitableCard : getGameModeRecruitmentCards(game, gameHistory)) {
            if (validRecruitableCards.contains(recruitableCard)) {
                selectableCards.add(new SelectableCharacterCard(recruitableCard,
                        CharacterCardSelectionStatus.Recruitable));
            } else {
                selectableCards.add(new SelectableCharacterCard(recruitableCard,
                        CharacterCardSelectionStatus.RecruitmentImpossible));
            }
        }

        // Then we add banished cards as an indication
        for (TeamColor playerTeamColor : TeamColor.values()) {
            for (CharacterCard banishedCard : game.getBanishedCards(playerTeamColor)) {
                selectableCards.add(new SelectableCharacterCard(banishedCard,
                        CharacterCardSelectionStatus.AlreadyBanned));
            }
        }

        return selectableCards;
    }

    /**
     * Returns the set of character cards already recruited by the given team.
     *
     * @param game the current game
     * @param teamColor the team whose recruited characters are considered
     * @param includeLeaders whether leader cards should be included
     */
    @NonNull
    public static Set<CharacterCard> getRecruitedCards(@NonNull Game game, @NonNull TeamColor teamColor, boolean includeLeaders) {
        Set<CharacterCard> teamRecruitmentCards = new HashSet<>();
        for (Character character : game.getRecruitedCharacters()) {
            if (character.getTeamColor() == teamColor &&
                    (includeLeaders || !character.getCharacterType().getCharacterCard().isLeader())) {
                teamRecruitmentCards.add(character.getCharacterType().getCharacterCard());
            }
        }

        return teamRecruitmentCards;
    }
}