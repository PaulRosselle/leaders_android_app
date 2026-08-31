package com.leaders.gamelogic.queries;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.entities.Game;
import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.gamelogic.entities.SelectableCharacterCard;
import com.leaders.gamelogic.enums.CharacterCard;
import com.leaders.gamelogic.enums.CharacterCardSelectionStatus;
import com.leaders.gamelogic.enums.GameMode;
import com.leaders.gamelogic.enums.TeamColor;

import java.util.ArrayList;
import java.util.List;

public final class BanishmentQuery {

    private BanishmentQuery(){
        throw new AssertionError("Cannot instantiate utility class");
    }

    public static boolean canBanish(@NonNull Game game, @NonNull GameHistory gameHistory,
                                    @NonNull TeamColor teamColor) {
        // No banishment are allowed outside of strategist mode
        if (gameHistory.getConfig().getGameMode() != GameMode.Strategist) {
            return false;
        }

        int recruitedCardsCount = RecruitmentQuery.getRecruitedCards(
                game, teamColor, false).size();
        int opponentRecruitedCardsCount = RecruitmentQuery.getRecruitedCards(
                game, teamColor.getOpposite(), false).size();

        int teamBanishmentCount = game.getBanishedCards(teamColor).size();

        // Banishment are allowed before player recruitments and once both have recruited 2 cards
        return recruitedCardsCount == opponentRecruitedCardsCount &&
                (recruitedCardsCount == 0 || recruitedCardsCount == 2) &&
                teamBanishmentCount < recruitedCardsCount / 2 + 1;
    }

    public static List<SelectableCharacterCard> getCurrentSelectableCards(@NonNull Game game) {
        List<SelectableCharacterCard> selectableCards = new ArrayList<>();
        // First, we add recruitable cards as banishable ones
        for (CharacterCard banishableCard : game.getRecruitableCards()) {
            selectableCards.add(new SelectableCharacterCard(banishableCard,
                    CharacterCardSelectionStatus.Banishable));
        }

        // Then we add already banished cards as an indication
        for (TeamColor playerTeamColor : TeamColor.values()) {
            for (CharacterCard banishedCard : game.getBanishedCards(playerTeamColor)) {
                selectableCards.add(new SelectableCharacterCard(banishedCard,
                        CharacterCardSelectionStatus.AlreadyBanned));
            }
        }

        return selectableCards;
    }
}