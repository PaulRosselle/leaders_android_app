package com.leaders.gamelogic.queries;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.entities.Game;
import com.leaders.gamelogic.enums.CharacterCard;
import com.leaders.gamelogic.enums.TeamColor;

import java.util.List;

public final class BanishmentQuery {

    private BanishmentQuery() {
        // Utility class
    }

    public static boolean canBanish(@NonNull Game game) {
        int banishmentCount = game.getBanishedCards().size();
        int blackRecruitmentCardsCount = RecruitmentQuery.getRecruitedCards(
                game, TeamColor.Black, false).size();
        int whiteRecruitmentCardsCount = RecruitmentQuery.getRecruitedCards(
                game, TeamColor.White, false).size();

        return blackRecruitmentCardsCount == whiteRecruitmentCardsCount
                && (banishmentCount == 0 || banishmentCount == 2)
                && blackRecruitmentCardsCount < banishmentCount + 2;
    }

    @NonNull
    public static List<CharacterCard> getBanishableCards(@NonNull Game game) {
        return game.getRecruitableCards();
    }
}