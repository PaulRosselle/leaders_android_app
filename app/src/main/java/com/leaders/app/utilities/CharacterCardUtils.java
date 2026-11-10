package com.leaders.app.utilities;

import android.content.Context;
import android.content.res.Resources;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.RawRes;
import androidx.annotation.StringRes;

import com.leaders.R;
import com.leaders.app.enums.CharacterSkinType;
import com.leaders.gamelogic.enums.AbilityType;
import com.leaders.gamelogic.enums.CharacterCard;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final  class CharacterCardUtils {
    private CharacterCardUtils(){
        throw new AssertionError("Cannot instantiate utility class");
    }

    //region NAME METHODS

    @StringRes
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
            case Wisp: return R.string.card_name_wisp;
            case Sniper: return R.string.card_name_sniper;
            default: throw new IllegalArgumentException("No name found for character card: " + characterCard);
        }
    }

    @StringRes
    public static int getFormattedNameId(@NonNull CharacterCard characterCard) {
        switch (characterCard) {
            case Acrobat: return R.string.card_formatted_name_acrobat;
            case Archer: return R.string.card_formatted_name_archer;
            case Assassin: return R.string.card_formatted_name_assassin;
            case Brewmaster: return R.string.card_formatted_name_brewmaster;
            case Bruiser: return R.string.card_formatted_name_bruiser;
            case ClawLauncher: return R.string.card_formatted_name_claw_launcher;
            case HermitAndCub: return R.string.card_formatted_name_hermit_and_cub;
            case Illusionist: return R.string.card_formatted_name_illusionist;
            case Jailer: return R.string.card_formatted_name_jailer;
            case LeaderKing:
            case LeaderQueen:
                return R.string.card_formatted_name_leader;
            case Manipulator: return R.string.card_formatted_name_manipulator;
            case Nemesis: return R.string.card_formatted_name_nemesis;
            case Protector: return R.string.card_formatted_name_protector;
            case Rider: return R.string.card_formatted_name_rider;
            case RoyalGuard: return R.string.card_formatted_name_royal_guard;
            case Vizier: return R.string.card_formatted_name_vizier;
            case Wanderer: return R.string.card_formatted_name_wanderer;
            case Wisp: return R.string.card_formatted_name_wisp;
            case Sniper: return R.string.card_formatted_name_sniper;
            default: throw new IllegalArgumentException("No formatted name found for character card: " + characterCard);
        }
    }

    @StringRes
    public static int getDescriptionId(@NonNull CharacterCard characterCard) {
        switch (characterCard) {
            case Acrobat: return R.string.card_description_acrobat;
            case Archer: return R.string.card_description_archer;
            case Assassin: return R.string.card_description_assassin;
            case Brewmaster: return R.string.card_description_brewmaster;
            case Bruiser: return R.string.card_description_bruiser;
            case ClawLauncher: return R.string.card_description_claw_launcher;
            case HermitAndCub: return R.string.card_description_hermit_and_cub;
            case Illusionist: return R.string.card_description_illusionist;
            case Jailer: return R.string.card_description_jailer;
            case LeaderKing:
            case LeaderQueen:
                return R.string.card_description_leader;
            case Manipulator: return R.string.card_description_manipulator;
            case Nemesis: return R.string.card_description_nemesis;
            case Protector: return R.string.card_description_protector;
            case Rider: return R.string.card_description_rider;
            case RoyalGuard: return R.string.card_description_royal_guard;
            case Vizier: return R.string.card_description_vizier;
            case Wanderer: return R.string.card_description_wanderer;
            case Wisp: return R.string.card_description_wisp;
            case Sniper: return R.string.card_description_sniper;
            default: throw new IllegalArgumentException("No name found for character card: " + characterCard);
        }
    }

    //endregion

    //region APPEARANCE METHODS

    @DrawableRes
    public static int getPortraitDrawableId(@NonNull CharacterCard card,
                                            @NonNull CharacterSkinType skinType) {
        switch (card) {
            case Acrobat: return R.drawable.card_portrait_acrobat;
            case Archer: return R.drawable.card_portrait_archer;
            case Assassin: return R.drawable.card_portrait_assassin;
            case Brewmaster: return R.drawable.card_portrait_brewmaster;
            case Bruiser: return R.drawable.card_portrait_bruiser;
            case ClawLauncher: return R.drawable.card_portrait_claw_launcher;
            case HermitAndCub: return R.drawable.card_portrait_hermit_and_cub;
            case Illusionist: {
                if (skinType == CharacterSkinType.LaughingMoonSage) {
                    return R.drawable.card_portrait_illusionist_lms;
                }
                return R.drawable.card_portrait_illusionist;
            }
            case Jailer: return R.drawable.card_portrait_jailer;
            case LeaderKing: return R.drawable.card_portrait_leader_king;
            case LeaderQueen: return R.drawable.card_portrait_leader_queen;
            case Manipulator: return R.drawable.card_portrait_manipulator;
            case Nemesis: return R.drawable.card_portrait_nemesis;
            case Protector: return R.drawable.card_portrait_protector;
            case Rider: return R.drawable.card_portrait_rider;
            case RoyalGuard: {
                if (skinType == CharacterSkinType.LaughingMoonSage) {
                    return R.drawable.card_portrait_royal_guard_lms;
                }
                return R.drawable.card_portrait_royal_guard;
            }
            case Vizier: {
                if (skinType == CharacterSkinType.LaughingMoonSage) {
                    return R.drawable.card_portrait_vizier_lms;
                }
                return R.drawable.card_portrait_vizier;
            }
            case Wanderer: return R.drawable.card_portrait_wanderer;
            case Wisp: return R.drawable.card_portrait_wisp;
            case Sniper: return R.drawable.card_portrait_sniper;
            default: throw new IllegalArgumentException("No portrait drawable for card: " + card);
        }
    }

    @DrawableRes
    public static int getPortraitBannedDrawableId(@NonNull CharacterCard card,
                                                  @NonNull CharacterSkinType skinType) {
        switch (card) {
            case Acrobat: return R.drawable.card_portrait_banned_acrobat;
            case Archer: return R.drawable.card_portrait_banned_archer;
            case Assassin: return R.drawable.card_portrait_banned_assassin;
            case Brewmaster: return R.drawable.card_portrait_banned_brewmaster;
            case Bruiser: return R.drawable.card_portrait_banned_bruiser;
            case ClawLauncher: return R.drawable.card_portrait_banned_claw_launcher;
            case HermitAndCub: return R.drawable.card_portrait_banned_hermit_and_cub;
            case Illusionist: {
                if (skinType == CharacterSkinType.LaughingMoonSage) {
                    return R.drawable.card_portrait_banned_illusionist_lms;
                }
                return R.drawable.card_portrait_banned_illusionist;
            }
            case Jailer: return R.drawable.card_portrait_banned_jailer;
            case Manipulator: return R.drawable.card_portrait_banned_manipulator;
            case Nemesis: return R.drawable.card_portrait_banned_nemesis;
            case Protector: return R.drawable.card_portrait_banned_protector;
            case Rider: return R.drawable.card_portrait_banned_rider;
            case RoyalGuard: {
                if (skinType == CharacterSkinType.LaughingMoonSage) {
                    return R.drawable.card_portrait_banned_royal_guard_lms;
                }
                return R.drawable.card_portrait_banned_royal_guard;
            }
            case Vizier: {
                if (skinType == CharacterSkinType.LaughingMoonSage) {
                    return R.drawable.card_portrait_banned_vizier_lms;
                }
                return R.drawable.card_portrait_banned_vizier;
            }
            case Wanderer: return R.drawable.card_portrait_banned_wanderer;
            case Wisp: return R.drawable.card_portrait_banned_wisp;
            case Sniper: return R.drawable.card_portrait_banned_sniper;
            default: throw new IllegalArgumentException("No portrait drawable for card: " + card);
        }
    }

    @DrawableRes
    public static int getPortraitHexagonalDrawableId(@NonNull CharacterCard card,
                                                     @NonNull CharacterSkinType skinType) {
        switch (card) {
            case Acrobat: return R.drawable.card_hex_portrait_acrobat;
            case Archer: return R.drawable.card_hex_portrait_archer;
            case Assassin: return R.drawable.card_hex_portrait_assassin;
            case Brewmaster: return R.drawable.card_hex_portrait_brewmaster;
            case Bruiser: return R.drawable.card_hex_portrait_bruiser;
            case ClawLauncher: return R.drawable.card_hex_portrait_claw_launcher;
            case HermitAndCub: return R.drawable.card_hex_portrait_hermit_and_cub;
            case Illusionist: {
                if (skinType == CharacterSkinType.LaughingMoonSage) {
                    return R.drawable.card_hex_portrait_illusionist_lms;
                }
                return R.drawable.card_hex_portrait_illusionist;
            }
            case Jailer: return R.drawable.card_hex_portrait_jailer;
            case LeaderKing: return R.drawable.card_hex_portrait_leader_king;
            case LeaderQueen: return R.drawable.card_hex_portrait_leader_queen;
            case Manipulator: return R.drawable.card_hex_portrait_manipulator;
            case Nemesis: return R.drawable.card_hex_portrait_nemesis;
            case Protector: return R.drawable.card_hex_portrait_protector;
            case Rider: return R.drawable.card_hex_portrait_rider;
            case RoyalGuard: {
                if (skinType == CharacterSkinType.LaughingMoonSage) {
                    return R.drawable.card_hex_portrait_royal_guard_lms;
                }
                return R.drawable.card_hex_portrait_royal_guard;
            }
            case Vizier: {
                if (skinType == CharacterSkinType.LaughingMoonSage) {
                    return R.drawable.card_hex_portrait_vizier_lms;
                }
                return R.drawable.card_hex_portrait_vizier;
            }
            case Wanderer: return R.drawable.card_hex_portrait_wanderer;
            case Wisp: return R.drawable.card_hex_portrait_wisp;
            case Sniper: return R.drawable.card_hex_portrait_sniper;
            default: throw new IllegalArgumentException("No portrait hex drawable for card: " + card);
        }
    }

    @DrawableRes
    public static int getPortraitHexagonalBannedDrawableId(@NonNull CharacterCard card,
                                                           @NonNull CharacterSkinType skinType) {
        switch (card) {
            case Acrobat: return R.drawable.card_hex_portrait_banned_acrobat;
            case Archer: return R.drawable.card_hex_portrait_banned_archer;
            case Assassin: return R.drawable.card_hex_portrait_banned_assassin;
            case Brewmaster: return R.drawable.card_hex_portrait_banned_brewmaster;
            case Bruiser: return R.drawable.card_hex_portrait_banned_bruiser;
            case ClawLauncher: return R.drawable.card_hex_portrait_banned_claw_launcher;
            case HermitAndCub: return R.drawable.card_hex_portrait_banned_hermit_and_cub;
            case Illusionist: {
                if (skinType == CharacterSkinType.LaughingMoonSage) {
                    return R.drawable.card_hex_portrait_banned_illusionist_lms;
                }
                return R.drawable.card_hex_portrait_banned_illusionist;
            }
            case Jailer: return R.drawable.card_hex_portrait_banned_jailer;
            case Manipulator: return R.drawable.card_hex_portrait_banned_manipulator;
            case Nemesis: return R.drawable.card_hex_portrait_banned_nemesis;
            case Protector: return R.drawable.card_hex_portrait_banned_protector;
            case Rider: return R.drawable.card_hex_portrait_banned_rider;
            case RoyalGuard: {
                if (skinType == CharacterSkinType.LaughingMoonSage) {
                    return R.drawable.card_hex_portrait_banned_royal_guard_lms;
                }
                return R.drawable.card_hex_portrait_banned_royal_guard;
            }
            case Vizier: {
                if (skinType == CharacterSkinType.LaughingMoonSage) {
                    return R.drawable.card_hex_portrait_banned_vizier_lms;
                }
                return R.drawable.card_hex_portrait_banned_vizier;
            }
            case Wanderer: return R.drawable.card_hex_portrait_banned_wanderer;
            case Wisp: return R.drawable.card_hex_portrait_banned_wisp;
            case Sniper: return R.drawable.card_hex_portrait_banned_sniper;
            default: throw new IllegalArgumentException("No hex portrait drawable for card: " + card);
        }
    }

    @DrawableRes
    public static int getArtworkResId(@NonNull CharacterCard characterCard) {
        switch (characterCard) {
            case Acrobat: return R.drawable.character_artwork_acrobat;
            case Archer: return R.drawable.character_artwork_archer;
            case Assassin: return R.drawable.character_artwork_assassin;
            case Brewmaster: return R.drawable.character_artwork_brewmaster;
            case Bruiser: return R.drawable.character_artwork_bruiser;
            case ClawLauncher: return R.drawable.character_artwork_claw_launcher;
            case HermitAndCub: return R.drawable.character_artwork_hermit_and_cub;
            case Illusionist: return R.drawable.character_artwork_illusionist;
            case Jailer: return R.drawable.character_artwork_jailer;
            case LeaderKing: return R.drawable.character_artwork_leader_king;
            case LeaderQueen: return R.drawable.character_artwork_leader_queen;
            case Manipulator: return R.drawable.character_artwork_manipulator;
            case Nemesis: return R.drawable.character_artwork_nemesis;
            case Protector: return R.drawable.character_artwork_protector;
            case Rider: return R.drawable.character_artwork_rider;
            case RoyalGuard: return R.drawable.character_artwork_royal_guard;
            case Vizier: return R.drawable.character_artwork_vizier;
            case Wanderer: return R.drawable.character_artwork_wanderer;
            case Wisp: return R.drawable.character_artwork_wisp;
            case Sniper: return R.drawable.character_artwork_sniper;
            default: throw new IllegalArgumentException("No formatted name found for character card: " + characterCard);
        }
    }

    @DrawableRes
    public static int getAbilityResId(@NonNull CharacterCard card) {
        if (card.isLeader()) {
            return R.drawable.icon_leader;
        }

        AbilityType[] abilityTypes = card.getAbilityTypes();
        if (abilityTypes.length < 1) {
            throw new IllegalStateException("Non leader cards must have an ability (" + card + ")");
        }

        AbilityType abilityType = abilityTypes[0];
        switch (abilityType) {
            case Active: return R.drawable.icon_ability_active;
            case Passive: return R.drawable.icon_ability_passive;
            case Special: return R.drawable.icon_ability_special;
            default: throw new IllegalStateException("Unexpected ability type: " + abilityType);
        }
    }

    public static List<CharacterSkinType> getSkins(@NonNull CharacterCard characterCard) {
        switch (characterCard) {
            case Illusionist:
            case RoyalGuard:
            case Vizier:
                return List.of(CharacterSkinType.Default, CharacterSkinType.LaughingMoonSage);
            default:
                return Collections.singletonList(CharacterSkinType.Default);
        }
    }

    //endregion

    //region DEMO METHODS

    @RawRes
    public static int getCharacterDemoResId(@NonNull CharacterCard characterCard) {
        switch (characterCard) {
            case Acrobat: return R.raw.character_demo_acrobat;
            case Archer: return R.raw.character_demo_archer;
            case Assassin: return R.raw.character_demo_assassin;
            case Brewmaster: return R.raw.character_demo_brewmaster;
            case Bruiser: return R.raw.character_demo_bruiser;
            case ClawLauncher: return R.raw.character_demo_claw_launcher;
            case HermitAndCub: return R.raw.character_demo_hermit_and_cub;
            case Illusionist: return R.raw.character_demo_illusionist;
            case Jailer: return R.raw.character_demo_jailer;
            case LeaderKing: return R.raw.character_demo_leader_king;
            case LeaderQueen: return R.raw.character_demo_leader_queen;
            case Manipulator: return R.raw.character_demo_manipulator;
            case Nemesis: return R.raw.character_demo_nemesis;
            case Protector: return R.raw.character_demo_protector;
            case Rider: return R.raw.character_demo_rider;
            case RoyalGuard: return R.raw.character_demo_royal_guard;
            case Vizier: return R.raw.character_demo_vizier;
            case Wanderer: return R.raw.character_demo_wanderer;
            case Wisp: return R.raw.character_demo_wisp;
            case Sniper: return R.raw.character_demo_sniper;
            default: throw new IllegalStateException("No character demo file found for character card: " + characterCard);
        }
    }

    //endregion

    //region SORTING METHODS

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

    public static void sort(@NonNull Context context, @NonNull List<CharacterCard> characterCards) {
        Resources res = context.getResources();
        characterCards.sort(
                Comparator.comparingInt(o -> getAbilitySortValue((CharacterCard) o))
                        .thenComparing(o -> res.getString(getNameId((CharacterCard) o)))
        );
    }

    //endregion
}
