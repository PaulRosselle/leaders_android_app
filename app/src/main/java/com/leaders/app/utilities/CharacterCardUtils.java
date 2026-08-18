package com.leaders.app.utilities;

import android.content.Context;
import android.content.res.Resources;

import androidx.annotation.NonNull;

import com.leaders.R;
import com.leaders.gamelogic.enums.AbilityType;
import com.leaders.gamelogic.enums.CharacterCard;

import java.util.Comparator;
import java.util.List;

public class CharacterCardUtils {
    private CharacterCardUtils(){
        throw new AssertionError("Cannot instantiate utility class");
    }

    private static int getAbilitySortValue(@NonNull CharacterCard characterCard) {
        // Priorité order is : Leader > Active > Passive > Special
        if (characterCard.isLeader()) {
            return 0;
        }

        AbilityType[] cardAbilities = characterCard.getAbilityTypes();
        if (cardAbilities.length == 0) {
            throw new IllegalStateException("Non-leader characters should always have an ability");
        }

        // Cards with multiple abilites are always sorted after single ones
        boolean hasMultipleAbilities = cardAbilities.length > 1;
        switch (cardAbilities[0]) {
            case Active: return hasMultipleAbilities ? 2 : 1;
            case Passive: return hasMultipleAbilities ? 4 : 3;
            case Special: return hasMultipleAbilities ? 6 : 5;
            default: throw new IllegalStateException("Impossible to sort ability: " + cardAbilities[0]);
        }
    }

    public static int getNameId(@NonNull CharacterCard characterCard) {
        switch (characterCard) {
            case Acrobat: return R.string.card_name_acrobat;
            case Archer: return R.string.card_name_archer;
            case Assassin: return R.string.card_name_assassin;
            case Brewmaster: return R.string.card_name_brewmaster;
            case Bruiser: return R.string.card_name_bruiser;
            case ClawLauncher: return R.string.card_name_claw_launcher;
            case HermitAndCub: return R.string.card_name_hermit_and_cub;
            case Illusionist: return R.string.card_name_illusionist;
            case Jailer: return R.string.card_name_jailer;
            case LeaderKing: return R.string.card_name_leader_king;
            case LeaderQueen: return R.string.card_name_leader_queen;
            case Manipulator: return R.string.card_name_manipulator;
            case Nemesis: return R.string.card_name_nemesis;
            case Protector: return R.string.card_name_protector;
            case Rider: return R.string.card_name_rider;
            case RoyalGuard: return R.string.card_name_royal_guard;
            case Vizier: return R.string.card_name_vizier;
            case Wanderer: return R.string.card_name_wanderer;
            default: throw new IllegalArgumentException("No name found for character card: " + characterCard);
        }
    }

    public static void sort(@NonNull Context context, @NonNull List<CharacterCard> characterCards) {
        Resources res = context.getResources();
        characterCards.sort(
                Comparator.comparingInt(o -> getAbilitySortValue((CharacterCard) o))
                        .thenComparing(o -> res.getString(getNameId((CharacterCard) o)))
        );
    }
}
