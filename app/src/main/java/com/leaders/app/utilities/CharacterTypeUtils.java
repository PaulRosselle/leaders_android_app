package com.leaders.app.utilities;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.R;
import com.leaders.app.enums.CharacterSkinType;
import com.leaders.gamelogic.enums.CharacterType;

import java.util.NoSuchElementException;

public class CharacterTypeUtils {

    //region APPEARANCE METHODS

    @DrawableRes
    public static int getPieceResId(@NonNull CharacterType characterType,
                                    @NonNull CharacterSkinType skinType) {
        switch (characterType) {
            case Acrobat: return R.drawable.character_piece_acrobat;
            case Archer: return R.drawable.character_piece_archer;
            case Assassin: return R.drawable.character_piece_assassin;
            case Brewmaster: return R.drawable.character_piece_brewmaster;
            case Bruiser: return R.drawable.character_piece_bruiser;
            case ClawLauncher: return R.drawable.character_piece_claw_launcher;
            case Cub: return R.drawable.character_piece_cub;
            case Hermit: return R.drawable.character_piece_hermit;
            case Illusionist: {
                if (skinType == CharacterSkinType.LaughingMoonSage) {
                    return R.drawable.character_piece_illusionist_lms;
                }
                return R.drawable.character_piece_illusionist;
            }
            case Jailer: return R.drawable.character_piece_jailer;
            case LeaderKing: return R.drawable.character_piece_leader_king;
            case LeaderQueen: return R.drawable.character_piece_leader_queen;
            case Manipulator: return R.drawable.character_piece_manipulator;
            case Nemesis: return R.drawable.character_piece_nemesis;
            case Protector: return R.drawable.character_piece_protector;
            case Rider: return R.drawable.character_piece_rider;
            case RoyalGuard: {
                if (skinType == CharacterSkinType.LaughingMoonSage) {
                    return R.drawable.character_piece_royal_guard_lms;
                }
                return R.drawable.character_piece_royal_guard;
            }
            case Vizier: {
                if (skinType == CharacterSkinType.LaughingMoonSage) {
                    return R.drawable.character_piece_vizier_lms;
                }
                return R.drawable.character_piece_vizier;
            }
            case Wanderer: return R.drawable.character_piece_wanderer;
            case LeaderVermilion: return R.drawable.character_piece_leader_vermilion;
            case Shaman: return R.drawable.character_piece_shaman;
            case Frog: return R.drawable.character_piece_frog;
            case Sniper: return R.drawable.character_piece_sniper;
            case Tactician: return R.drawable.character_piece_tactician;
            case Wisp: return R.drawable.character_piece_wisp;
            case EliteArcher: return R.drawable.character_piece_elite_archer;
            default: throw new IllegalArgumentException("No drawable found for character: " + characterType);
        }
    }

    //endregion

    //region ALTERNATIVE CHARACTERS

    @Nullable
    public static CharacterType getBaseCharacter(@NonNull CharacterType characterType) {
        if (characterType == CharacterType.EliteArcher) {
            return CharacterType.Archer;
        }
        return null;
    }

    public boolean isAlternativeCharacter(@NonNull CharacterType characterType) {
        return getBaseCharacter(characterType) != null;
    }

    //endregion

    //region IMPORT / EXPORT METHODS

    public static int getCharacterExportId(@NonNull CharacterType characterType) {
        switch (characterType) {
            case Acrobat: return 3;
            case Assassin: return 5;
            case Brewmaster: return 16;
            case Bruiser: return 7;
            case ClawLauncher: return 11;
            case Cub: return 18;
            case Hermit: return 17;
            case Illusionist: return 10;
            case Jailer: return 9;
            case LeaderKing: return 1;
            case LeaderQueen: return 2;
            case Manipulator: return 12;
            case Nemesis: return 13;
            case Protector: return 14;
            case Rider: return 6;
            case RoyalGuard: return 8;
            case Vizier: return 19;
            case Wanderer: return 15;
            case Archer: return 4;
            case LeaderVermilion: return 25;
            case Shaman: return 21;
            case Frog: return 22; // TODO - handle the "frogified" tag
            case Sniper: return 23;
            case Tactician: return 24;
            case Wisp: return 20;
            // TODO - replace by a dedicated ID once Mathieu implements the Elite Archer
            case EliteArcher: return 4;
            default: throw new NoSuchElementException("No LBE ID found for: " + characterType);
        }
    }

    //endregion
}
