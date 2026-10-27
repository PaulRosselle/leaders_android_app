package com.leaders.app.views.character;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.appcompat.widget.AppCompatImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator;

import com.leaders.R;
import com.leaders.app.entities.CharacterPiece;
import com.leaders.app.enums.CharacterSkinType;
import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.enums.CharacterType;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.gamelogic.interactions.InteractionTarget;

public final class CharacterView extends AppCompatImageView {
    private static final int SCALE_FOR_HIGHLIGHT_ANIMATION_DURATION = 120;

    @Nullable
    private InteractionTarget target;
    @NonNull
    private CharacterPiece piece;


    public CharacterView(Context context) {
        this(context, null);
    }

    public CharacterView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        target = null;
        try (TypedArray customAttrs = context.obtainStyledAttributes(attrs, R.styleable.CharacterView)) {
            int characterTypeOrd = customAttrs.getInteger(R.styleable.CharacterView_characterType, -1);
            CharacterType characterType = characterTypeOrd == -1 ? null : CharacterType.values()[characterTypeOrd];
            int teamColorOrd = customAttrs.getInteger(R.styleable.CharacterView_teamColor, TeamColor.Black.ordinal());
            TeamColor teamColor = TeamColor.values()[teamColorOrd];
            CharacterSkinType skinType = CharacterSkinType.Default;

            this.piece = new CharacterPiece(characterType, skinType, teamColor);
            setCharacter(piece);
        }

        setLayerType(View.LAYER_TYPE_HARDWARE, null);
    }

    public void setCharacter(@NonNull Character character, @NonNull CharacterSkinType skinType) {
        setCharacter(character.getCharacterType(), skinType, character.getTeamColor());
    }

    public void setCharacter(@Nullable CharacterType characterType,
                             @NonNull CharacterSkinType skinType,
                             @NonNull TeamColor teamColor) {
        setCharacter(new CharacterPiece(characterType, skinType, teamColor));
    }

    public void setCharacter(@NonNull CharacterPiece piece) {
        this.piece = piece;
        setImageDrawable(piece.getDrawable(getContext()));
    }

    public void clearTarget() {
        target = null;
        setForeground(null);
    }

    public void setAsActiveAbilityTarget(@NonNull InteractionTarget target) {
        this.target = target;
        setForeground(ContextCompat.getDrawable(getContext(), R.drawable.target_ability_character));
    }

    public void setAsPlayableTarget(@NonNull InteractionTarget target) {
        this.target = target;
        // There is no visual feedback within the characterView for the moment
        // since playable characters must be highlighted using a HighlightView
    }

    public void setAsTarget(@NonNull InteractionTarget target) {
        this.target = target;
        // This method is made to target a characterView without visual feedacks
    }


    @Nullable
    public InteractionTarget getTarget() {
        return target;
    }

    @Nullable
    public CharacterType getCharacterType() {
        return piece.getCharacterType();
    }

    @NonNull
    public TeamColor getTeamColor() {
        return piece.getTeamColor();
    }

    private float getScaleForHighlightValue() {
        // Android float dimensions must be gotten through a typed value
        TypedValue outValue = new TypedValue();
        getResources().getValue(R.dimen.highlighted_token, outValue, true);
        return outValue.getFloat();
    }

    public void scaleForHighlight(boolean highlighted, boolean animate) {
        float scaleValue = highlighted ? getScaleForHighlightValue() : 1f;
        if (animate) {
            animate().scaleX(scaleValue).scaleY(scaleValue)
                    .setInterpolator(new LinearOutSlowInInterpolator())
                    .setDuration(SCALE_FOR_HIGHLIGHT_ANIMATION_DURATION)
                    .start();
        } else {
            setScaleX(scaleValue);
            setScaleY(scaleValue);
        }
    }

    public void animateSetCharacter(@Nullable CharacterType characterType,
                                    @NonNull CharacterSkinType skinType,
                                    @NonNull TeamColor teamColor,
                                    int duration,
                                    @Nullable Runnable onAnimationEnd) {
        this.piece = new CharacterPiece(characterType, skinType, teamColor);

        Drawable[] drawables = new Drawable[2];
        drawables[0] = getDrawable();
        drawables[1] = piece.getDrawable(getContext());

        TransitionDrawable fadeTransition = new TransitionDrawable(drawables);
        fadeTransition.setCrossFadeEnabled(true);
        setImageDrawable(fadeTransition);
        fadeTransition.startTransition(duration);
        if (onAnimationEnd != null) {
            new Handler(Looper.getMainLooper()).postDelayed(onAnimationEnd, duration);
        }
    }
}
