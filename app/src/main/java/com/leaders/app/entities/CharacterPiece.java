package com.leaders.app.entities;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;

import com.leaders.R;
import com.leaders.app.enums.CharacterSkinType;
import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.enums.CharacterType;
import com.leaders.gamelogic.enums.TeamColor;

public class CharacterPiece {
    @Nullable
    private final CharacterType characterType;
    @NonNull
    private final CharacterSkinType skinType;
    @NonNull
    private final TeamColor teamColor;

    public CharacterPiece(@Nullable CharacterType characterType,
                          @NonNull CharacterSkinType skinType,
                          @NonNull TeamColor teamColor) {
        this.characterType = characterType;
        this.skinType = skinType;
        this.teamColor = teamColor;
    }

    public CharacterPiece(@NonNull Character character,
                          @NonNull CharacterSkinType skinType) {
        this.characterType = character.getCharacterType();
        this.skinType = skinType;
        this.teamColor = character.getTeamColor();
    }


    @Nullable
    public CharacterType getCharacterType() {
        return characterType;
    }

    @NonNull
    public CharacterSkinType getSkinType() {
        return skinType;
    }

    @NonNull
    public TeamColor getTeamColor() {
        return teamColor;
    }

    @NonNull
    public Drawable getDrawable(@NonNull Context context) {

        boolean hasCharacter = characterType == null;
        // Without a character, the alt white piece is used since it has a better contrast
        Drawable teamColorDrawable = AppCompatResources.getDrawable(
                context, getTeamColorResId(teamColor, hasCharacter)
        );
        if (hasCharacter) {
            return teamColorDrawable;
        }

        Drawable characterDrawable = AppCompatResources.getDrawable(
                context, getCharacterResId(characterType, skinType)
        );

        int outlineResId = getOutlineResId(characterType, skinType);
        if (outlineResId != -1) {
            return new LayerDrawable(new Drawable[]{
                    teamColorDrawable,
                    AppCompatResources.getDrawable(context, outlineResId),
                    characterDrawable
            });
        }

        return new LayerDrawable(new Drawable[]{teamColorDrawable, characterDrawable});
    }

    private int getOutlineResId(@NonNull CharacterType characterType,
                                @NonNull CharacterSkinType skinType) {
        if (characterType.getCharacterCard().isLeader()) {
            return R.drawable.character_piece_outline_leader;
        }

        switch (skinType) {
            case Default: return -1;
            case LaughingMoonSage: return R.drawable.character_piece_outline_lms;
            default: throw new IllegalStateException("Not outline found matching skin type: " + skinType);
        }
    }

    private int getTeamColorResId(@NonNull TeamColor teamColor, boolean useAlt) {
        if (teamColor == TeamColor.Black) {
            return R.drawable.character_piece_empty_b;
        }

        return useAlt ? R.drawable.character_piece_empty_alt_w : R.drawable.character_piece_empty_w;
    }

    private int getCharacterResId(@NonNull CharacterType characterType,
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
            default: throw new IllegalArgumentException("No drawable found for character: " + characterType);
        }

    }
}
