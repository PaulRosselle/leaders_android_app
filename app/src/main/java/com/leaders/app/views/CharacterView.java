package com.leaders.app.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.R;
import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.enums.CharacterType;
import com.leaders.gamelogic.enums.TeamColor;

public class CharacterView extends AppCompatImageView {
    @Nullable
    private CharacterType characterType;
    @NonNull
    private TeamColor teamColor;

    public CharacterView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        try (TypedArray customAttrs = context.obtainStyledAttributes(attrs, R.styleable.CharacterView)) {
            int characterTypeOrd = customAttrs.getInteger(R.styleable.CharacterView_characterType, -1);
            characterType = characterTypeOrd == -1 ? null : CharacterType.values()[characterTypeOrd];
            int teamColorOrd = customAttrs.getInteger(R.styleable.CharacterView_teamColor, TeamColor.Black.ordinal());
            teamColor = TeamColor.values()[teamColorOrd];

            setCharacter(characterType, teamColor);
        }
    }

    public void setCharacter(@NonNull Character character) {
        setCharacter(character.getCharacterType(), character.getTeamColor());
    }

    public void setCharacter(@Nullable CharacterType characterType,
                             @NonNull TeamColor teamColor) {
        this.characterType = characterType;
        this.teamColor = teamColor;
        setImageResource(getCharacterDrawableId(characterType, teamColor));
    }

    private int getCharacterDrawableId(@Nullable CharacterType characterType,
                                       @NonNull TeamColor teamColor) {
        boolean isWhite = teamColor == TeamColor.White;

        if (characterType == null) {
            return isWhite ? R.drawable.empty_token_w : R.drawable.empty_token_b;
        }

        switch (characterType) {
            case Acrobat: return isWhite ? R.drawable.acrobat_token_w : R.drawable.acrobat_token_b;
            case Archer: return isWhite ? R.drawable.archer_token_w : R.drawable.archer_token_b;
            case Assassin: return isWhite ? R.drawable.assassin_token_w : R.drawable.assassin_token_b;
            case Brewmaster: return isWhite ? R.drawable.brewmaster_token_w : R.drawable.brewmaster_token_b;
            case Bruiser: return isWhite ? R.drawable.bruiser_token_w : R.drawable.bruiser_token_b;
            case ClawLauncher: return isWhite ? R.drawable.claw_launcher_token_w : R.drawable.claw_launcher_token_b;
            case Cub: return isWhite ? R.drawable.cub_token_w : R.drawable.cub_token_b;
            case Hermit: return isWhite ? R.drawable.hermit_token_w : R.drawable.hermit_token_b;
            case Illusionist: return isWhite ? R.drawable.illusionist_token_w : R.drawable.illusionist_token_b;
            case Jailer: return isWhite ? R.drawable.jailer_token_w : R.drawable.jailer_token_b;
            case LeaderKing: return isWhite ? R.drawable.leader_king_token_w : R.drawable.leader_king_token_b;
            case LeaderQueen: return isWhite ? R.drawable.leader_queen_token_w : R.drawable.leader_queen_token_b;
            case Manipulator: return isWhite ? R.drawable.manipulator_token_w : R.drawable.manipulator_token_b;
            case Nemesis: return isWhite ? R.drawable.nemesis_token_w : R.drawable.nemesis_token_b;
            case Protector: return isWhite ? R.drawable.protector_token_w : R.drawable.protector_token_b;
            case Rider: return isWhite ? R.drawable.rider_token_w : R.drawable.rider_token_b;
            case RoyalGuard: return isWhite ? R.drawable.royal_guard_token_w : R.drawable.royal_guard_token_b;
            case Vizier: return isWhite ? R.drawable.vizier_token_w : R.drawable.vizier_token_b;
            case Wanderer: return isWhite ? R.drawable.wanderer_token_w : R.drawable.wanderer_token_b;
            default: throw new IllegalArgumentException("No drawable found for character: " + characterType);
        }
    }

    @Nullable
    public CharacterType getCharacterType() {
        return characterType;
    }

    @NonNull
    public TeamColor getTeamColor() {
        return teamColor;
    }
}
