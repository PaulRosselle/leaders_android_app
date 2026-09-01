package com.leaders.gamelogic.queries;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.gamelogic.entities.Game;
import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.gamelogic.entities.SelectableCharacterCard;
import com.leaders.gamelogic.enums.CharacterCard;
import com.leaders.gamelogic.enums.CharacterCardSelectionStatus;
import com.leaders.gamelogic.enums.GameMode;
import com.leaders.gamelogic.enums.TeamColor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SelectableCardsQuery {
    private SelectableCardsQuery() {
        throw new AssertionError("Cannot instantiate utility class");
    }

    @NonNull
    public static List<CharacterCard> getAvailableCards(@NonNull Game game,
                                                        @NonNull GameHistory gameHistory) {
        // The recruitment is restricted to a pool of 3 randomly chosen cards in Discovery mode
        if (gameHistory.getConfig().getGameMode() == GameMode.Discovery) {
            return game.getRecruitableCards().stream().limit(3).collect(Collectors.toList());
        }
        // By default, all recruitable cards are returned
        return game.getRecruitableCards();
    }



    public static List<SelectableCharacterCard> getSelectableCards(@NonNull Game game,
                                                                   @NonNull GameHistory gameHistory) {
        return getSelectableCards(
                game, gameHistory, null,
                CharacterCardSelectionStatus.NotSelectable,
                CharacterCardSelectionStatus.NotSelectable
        );
    }

    public static List<SelectableCharacterCard> getSelectableCards(@NonNull Game game,
                                                                   @NonNull GameHistory gameHistory,
                                                                   @Nullable List<CharacterCard> validCards,
                                                                   @NonNull CharacterCardSelectionStatus validCardStatus,
                                                                   @NonNull CharacterCardSelectionStatus invalidCardStatus) {
        List<SelectableCharacterCard> selectableCards = new ArrayList<>();
        // First, we add recruitable cards as banishable ones
        for (CharacterCard availableCard : getAvailableCards(game, gameHistory)) {
            if (validCards == null || validCards.contains(availableCard)) {
                selectableCards.add(new SelectableCharacterCard(availableCard, validCardStatus));
            } else {
                selectableCards.add(new SelectableCharacterCard(availableCard, invalidCardStatus));
            }
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
