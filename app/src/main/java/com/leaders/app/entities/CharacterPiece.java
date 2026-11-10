package com.leaders.app.entities;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;

import com.leaders.R;
import com.leaders.app.enums.CharacterSkinType;
import com.leaders.app.utilities.CharacterTypeUtils;
import com.leaders.gamelogic.enums.CharacterType;
import com.leaders.gamelogic.enums.TeamColor;

import java.util.Objects;

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

    @Nullable
    public CharacterType getCharacterType() {
        return characterType;
    }

    @NonNull
    public TeamColor getTeamColor() {
        return teamColor;
    }

    @NonNull
    public Drawable getDrawable(@NonNull Context context) {

        boolean hasNoCharacter = characterType == null;
        // Without a character, the alt white piece is used since it has a better contrast
        Drawable teamColorDrawable = AppCompatResources.getDrawable(
                context, getTeamColorResId(teamColor, hasNoCharacter)
        );
        if (hasNoCharacter) {
            return Objects.requireNonNull(teamColorDrawable,
                    "No character piece drawable found for team color: " + teamColor
            );
        }

        Drawable characterDrawable = AppCompatResources.getDrawable(
                context, CharacterTypeUtils.getPieceResId(characterType, skinType)
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
}
