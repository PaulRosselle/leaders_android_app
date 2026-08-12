package com.leaders.gamelogic.queries;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.entities.Game;
import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.gamelogic.enums.CharacterCard;
import com.leaders.gamelogic.enums.GameMode;
import com.leaders.gamelogic.enums.TeamColor;

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

    @NonNull
    public static List<CharacterCard> getBanishableCards(@NonNull Game game) {
        return game.getRecruitableCards();
    }
}