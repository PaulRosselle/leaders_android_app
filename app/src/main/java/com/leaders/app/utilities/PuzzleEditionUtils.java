package com.leaders.app.utilities;

import android.content.Context;

import androidx.annotation.NonNull;

import com.leaders.R;
import com.leaders.gamelogic.entities.Board;
import com.leaders.gamelogic.entities.Cell;
import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.enums.CharacterCard;
import com.leaders.gamelogic.enums.TeamColor;

import java.util.Arrays;
import java.util.List;

public final class PuzzleEditionUtils {
    private PuzzleEditionUtils(){
        throw new AssertionError("Cannot instantiate utility class");
    }

    public static String getCardAdditionErrors(@NonNull Context context,
                                               @NonNull Board board,
                                               @NonNull CharacterCard cardToAdd,
                                               @NonNull List<TeamColor> addableColors) {
        addableColors.addAll(Arrays.asList(TeamColor.values()));

        if (!cardToAdd.isLeader() && !isCardRestrictedToOnePerTeam(cardToAdd)) {
            return "";
        }

        for (Cell cell : board.getCells().values()) {
            Character character = cell.getCharacter();

            if (character == null) {
                continue;
            }

            CharacterCard existingCard = character.getCharacterType().getCharacterCard();

            // The exact same leader can only exist once on the board.
            if (cardToAdd.isLeader() && cardToAdd == existingCard) {
                return context.getString(R.string.error_toast_limited_character);
            }

            // Leaders and restricted cards cannot be added to a team that already contains one.
            boolean isLeader = cardToAdd.isLeader() && existingCard.isLeader();
            boolean isRestrictedCard = cardToAdd == existingCard && isCardRestrictedToOnePerTeam(cardToAdd);

            if (isLeader || isRestrictedCard) {
                addableColors.remove(character.getTeamColor());
                if (addableColors.isEmpty()) {
                    if (isLeader) {
                        return context.getString(R.string.error_toast_max_two_leaders_on_the_board);
                    }
                    return context.getString(R.string.error_toast_character_limited_to_one_per_team);
                }
            }

        }

        return "";
    }

    private static boolean isCardRestrictedToOnePerTeam(@NonNull CharacterCard characterCard) {
        return characterCard == CharacterCard.Nemesis;
    }
}
