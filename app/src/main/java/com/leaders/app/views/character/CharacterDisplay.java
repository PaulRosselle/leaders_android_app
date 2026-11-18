package com.leaders.app.views.character;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.leaders.app.entities.CharacterSkins;
import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.entities.TransformedCharacter;

public final class CharacterDisplay {
    public enum ViewType {
        Character,
        Shine,
        Highlight,
        Transformed
    }

    @NonNull
    private final CharacterView characterView;
    @NonNull
    private final HighlightView highlightView;
    @NonNull
    private final CharacterShineView shineView;
    @NonNull
    private final CharacterTransformedView transformedView;

    private boolean isHighlighted;

    @Nullable
    private OnCharacterDisplayClickListener onClickListener;
    @Nullable
    private OnCharacterDisplayLongClickListener onLongClickListener;


    public CharacterDisplay(@NonNull Context context,
                            @NonNull ConstraintLayout charactersLayer,
                            @NonNull ConstraintLayout effectsLayer) {
        characterView = new CharacterView(context);
        characterView.setOnClickListener(this::onCharacterClick);
        characterView.setOnLongClickListener(this::onCharacterLongClick);
        charactersLayer.addView(characterView, getDefaultLayoutParams());

        shineView = new CharacterShineView(context);
        charactersLayer.addView(shineView, getDefaultLayoutParams());

        highlightView = new HighlightView(context, null);
        effectsLayer.addView(highlightView, getDefaultLayoutParams());

        transformedView = new CharacterTransformedView(context);
        effectsLayer.addView(transformedView, getDefaultLayoutParams());

        reset();
    }

    public void setSize(int size) {
        for (ViewType viewType : ViewType.values()) {
            setSize(getCharacterViewFromType(viewType), (int) (size * getSizeRatioFromType(viewType)));
        }
    }

    private float getSizeRatioFromType(@NonNull ViewType viewType) {
        if (viewType == ViewType.Transformed) {
            return 0.46f;
        }
        return 1f;
    }

    private void setSize(@NonNull View view, int size) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = size;
        layoutParams.width = size;
        view.setLayoutParams(layoutParams);
        view.requestLayout();
    }

    @NonNull
    public CharacterView getCharacterView() {
        return characterView;
    }

    @NonNull
    private ConstraintLayout.LayoutParams getDefaultLayoutParams() {
        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_CONSTRAINT,
                ConstraintLayout.LayoutParams.MATCH_CONSTRAINT
        );

        params.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
        params.leftToLeft = ConstraintLayout.LayoutParams.PARENT_ID;

        return params;
    }

    public void reset() {
        characterView.clearTarget();

        characterView.setScaleX(1f);
        characterView.setScaleY(1f);
        characterView.setAlpha(1f);
        characterView.setVisibility(View.GONE);

        highlightView.stopAnimation();
        highlightView.setVisibility(View.GONE);

        shineView.stopShine();
        shineView.setVisibility(View.GONE);

        transformedView.setVisibility(View.GONE);
    }

    public void setVisibility(int visibility) {
        for (ViewType viewType : ViewType.values()) {
            getCharacterViewFromType(viewType).setVisibility(visibility);
        }
    }

    public void setPosition(float x, float y) {
        for (ViewType viewType : ViewType.values()) {
            setPosition(viewType, x, y);
        }
    }

    public void setPosition(@NonNull ViewType viewType, float x, float y) {
        View view = getCharacterViewFromType(viewType);
        view.setX(x);
        view.setY(y);
    }

    private View getCharacterViewFromType(@NonNull ViewType viewType) {
        switch (viewType) {
            case Character: return characterView;
            case Highlight: return highlightView;
            case Shine: return shineView;
            case Transformed: return transformedView;
            default: throw new IllegalArgumentException("No character view found for type: " + viewType);
        }
    }

    private void onCharacterClick(View v) {
        if (onClickListener != null) {
            onClickListener.onCharacterDisplayClick(this);
        }
    }

    private boolean onCharacterLongClick(View v) {
        return onLongClickListener != null && onLongClickListener.onCharacterDisplayLongClick(this);
    }

    public void setOnClickListener(@Nullable OnCharacterDisplayClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public void setOnCharacterLongClickListener(@Nullable OnCharacterDisplayLongClickListener onLongClickListener) {
        this.onLongClickListener = onLongClickListener;
    }

    public void applyTransformedCharacter(@Nullable TransformedCharacter transformedCharacter,
                                          @NonNull CharacterSkins characterSkins) {
        transformedView.setVisibility(transformedCharacter != null ? View.VISIBLE : View.GONE);

        Character character = transformedCharacter != null ?
                transformedCharacter.getTransformedState().getCharacterBefore() : null;

        if (character != null) {
            transformedView.setCharacter(character, characterSkins.getCharacterSkin(character));
        } else {
            transformedView.clearCharacter();
        }
    }

    public void setIsHighlighted(boolean isHighlighted, boolean animateCharacterScaling) {
        this.isHighlighted = isHighlighted;

        characterView.scaleForHighlight(isHighlighted, animateCharacterScaling);
        highlightView.setVisibility(isHighlighted ? View.VISIBLE : View.GONE);
        // The character shine view visibility depends on the highlight visibility
        shineView.setVisibility(isHighlighted ? View.VISIBLE : View.GONE);
    }

    public void hide(ViewType viewType) {
        getCharacterViewFromType(viewType).setVisibility(View.GONE);
    }

    public boolean isHighlighted() {
        return isHighlighted;
    }

    public void playShineAnimation() {
        if (!isHighlighted) {
            return;
        }

        shineView.playShine();
    }

    public void stopShineAnimation() {
        shineView.stopShine();
    }

    public void startHighlightAnimation() {
        if (isHighlighted) {
            highlightView.startAnimation();
        }
    }

    public void stopHighlightAnimation() {
        highlightView.stopAnimation();
    }

    public void stopEffectsAnimation() {
        stopHighlightAnimation();
        stopShineAnimation();
    }

    public void clearTarget() {
        characterView.clearTarget();
    }
}
