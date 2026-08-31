package com.leaders.gamelogic.queries;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.entities.Game;
import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.gamelogic.entities.SelectableCharacterCard;
import com.leaders.gamelogic.enums.CharacterCard;
import com.leaders.gamelogic.enums.CharacterCardSelectionStatus;
import com.leaders.gamelogic.enums.TeamColor;

import java.util.ArrayList;
import java.util.List;

public class SelectableCardsQuery {
    private SelectableCardsQuery() {
        throw new AssertionError("Cannot instantiate utility class");
    }

    public static List<SelectableCharacterCard> getSelectableCards(@NonNull Game game,
                                                                   @NonNull GameHistory gameHistory) {
        List<SelectableCharacterCard> selectableCards = new ArrayList<>();
        // First, we add recruitable cards as banishable ones
        for (CharacterCard banishableCard : RecruitmentQuery.getAvailableCards(game, gameHistory)) {
            selectableCards.add(new SelectableCharacterCard(banishableCard,
                    CharacterCardSelectionStatus.RecruitmentImpossible));
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
