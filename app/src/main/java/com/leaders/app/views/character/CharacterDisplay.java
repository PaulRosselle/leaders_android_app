package com.leaders.app.views.character;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

public final class CharacterDisplay {
    public enum ViewType {
        Character,
        Highlight,
        Shine
    }

    @NonNull
    private final CharacterView characterView;
    @NonNull
    private final HighlightView highlightView;
    @NonNull
    private final CharacterShineView shineView;

    private boolean isHighlighted;

    private OnCharacterDisplayClickListener onClickListener;

    public CharacterDisplay(@NonNull Context context, @NonNull ViewGroup parentView) {
        characterView = new CharacterView(context);
        characterView.setOnClickListener(this::onCharacterClick);
        parentView.addView(characterView, getDefaultLayoutParams());

        shineView = new CharacterShineView(context);
        parentView.addView(shineView, getDefaultLayoutParams());

        highlightView = new HighlightView(context);
        parentView.addView(highlightView, getDefaultLayoutParams());

        reset();
    }

    public void setSize(int size) {
        for (ViewType viewType : ViewType.values()) {
            setSize(getCharacterViewFromType(viewType), size);
        }
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

        setIsHighlighted(false, false);

        characterView.setVisibility(View.GONE);
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
            default: throw new IllegalArgumentException("No character view found for type: " + viewType);
        }
    }

    private void onCharacterClick(View v) {
        if (onClickListener != null) {
            onClickListener.onCharacterDisplayClick(this);
        }
    }

    public void setOnClickListener(OnCharacterDisplayClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public void setOnCharacterLongClickListener(View.OnLongClickListener onLongClickListener) {
        characterView.setOnLongClickListener(onLongClickListener);
    }

    public void setIsHighlighted(boolean isHighlighted, boolean animateCharacterScaling) {
        this.isHighlighted = isHighlighted;

        characterView.scaleForHighlight(isHighlighted, animateCharacterScaling);
        highlightView.setVisibility(isHighlighted ? View.VISIBLE : View.GONE);
        shineView.setVisibility(isHighlighted ? View.VISIBLE : View.GONE);
    }

    public boolean isHighlighted() {
        return isHighlighted;
    }

    public void playShineAnimation() {
        if (!isHighlighted) {
            return;
        }

        bringToFront();
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

    public void bringToFront() {
        characterView.bringToFront();
        shineView.bringToFront();
        highlightView.bringToFront();
    }

    public void clearTarget() {
        characterView.clearTarget();
    }
}
