package com.leaders.app.views.board;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.leaders.R;
import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.enums.CharacterType;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.gamelogic.interactions.InteractionTarget;

public final class CharacterView extends AppCompatImageView {
    @Nullable
    private InteractionTarget target;

    public CharacterView(Context context) {
        super(context);

        target = null;
        setCharacter(null, TeamColor.Black);
    }

    public CharacterView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        target = null;
        try (TypedArray customAttrs = context.obtainStyledAttributes(attrs, R.styleable.CharacterView)) {
            int characterTypeOrd = customAttrs.getInteger(R.styleable.CharacterView_characterType, -1);
            CharacterType characterType = characterTypeOrd == -1 ? null : CharacterType.values()[characterTypeOrd];
            int teamColorOrd = customAttrs.getInteger(R.styleable.CharacterView_teamColor, TeamColor.Black.ordinal());
            TeamColor teamColor = TeamColor.values()[teamColorOrd];

            setCharacter(characterType, teamColor);
        }
    }

    public void setCharacter(@NonNull Character character) {
        setCharacter(character.getCharacterType(), character.getTeamColor());
    }

    public void setCharacter(@Nullable CharacterType characterType,
                             @NonNull TeamColor teamColor) {
        setImageResource(getCharacterDrawableId(characterType, teamColor));
    }

    private int getCharacterDrawableId(@Nullable CharacterType characterType,
                                       @NonNull TeamColor teamColor) {
        boolean isWhite = teamColor == TeamColor.White;

        if (characterType == null) {
            return isWhite ? R.drawable.character_piece_empty_w : R.drawable.character_piece_empty_b;
        }

        switch (characterType) {
            case Acrobat: return isWhite ? R.drawable.character_piece_acrobat_w : R.drawable.character_piece_acrobat_b;
            case Archer: return isWhite ? R.drawable.character_piece_archer_w : R.drawable.character_piece_archer_b;
            case Assassin: return isWhite ? R.drawable.character_piece_assassin_w : R.drawable.character_piece_assassin_b;
            case Brewmaster: return isWhite ? R.drawable.character_piece_brewmaster_w : R.drawable.character_piece_brewmaster_b;
            case Bruiser: return isWhite ? R.drawable.character_piece_bruiser_w : R.drawable.character_piece_bruiser_b;
            case ClawLauncher: return isWhite ? R.drawable.character_piece_claw_launcher_w : R.drawable.character_piece_claw_launcher_b;
            case Cub: return isWhite ? R.drawable.character_piece_cub_w : R.drawable.character_piece_cub_b;
            case Hermit: return isWhite ? R.drawable.character_piece_hermit_w : R.drawable.character_piece_hermit_b;
            case Illusionist: return isWhite ? R.drawable.character_piece_illusionist_w : R.drawable.character_piece_illusionist_b;
            case Jailer: return isWhite ? R.drawable.character_piece_jailer_w : R.drawable.character_piece_jailer_b;
            case LeaderKing: return isWhite ? R.drawable.character_piece_leader_king_w : R.drawable.character_piece_leader_king_b;
            case LeaderQueen: return isWhite ? R.drawable.character_piece_leader_queen_w : R.drawable.character_piece_leader_queen_b;
            case Manipulator: return isWhite ? R.drawable.character_piece_manipulator_w : R.drawable.character_piece_manipulator_b;
            case Nemesis: return isWhite ? R.drawable.character_piece_nemesis_w : R.drawable.character_piece_nemesis_b;
            case Protector: return isWhite ? R.drawable.character_piece_protector_w : R.drawable.character_piece_protector_b;
            case Rider: return isWhite ? R.drawable.rider_token_w : R.drawable.rider_token_b;
            case RoyalGuard: return isWhite ? R.drawable.character_piece_royal_guard_w : R.drawable.character_piece_royal_guard_b;
            case Vizier: return isWhite ? R.drawable.character_piece_vizier_w : R.drawable.character_piece_vizier_b;
            case Wanderer: return isWhite ? R.drawable.character_piece_wanderer_w : R.drawable.character_piece_wanderer_b;
            default: throw new IllegalArgumentException("No drawable found for character: " + characterType);
        }
    }

    public void clearTarget() {
        target = null;
        setForeground(null);
    }

    public void setAsActiveAbilityTarget(@NonNull InteractionTarget target) {
        this.target = target;
        setForeground(ContextCompat.getDrawable(getContext(), R.drawable.target_ability_character));
    }
}
