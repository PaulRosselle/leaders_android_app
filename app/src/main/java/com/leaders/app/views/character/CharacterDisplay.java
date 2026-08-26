package com.leaders.app.views.character;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

public final class CharacterDisplay {
    public enum ViewType {
        Character,
        Highlight
    }

    @NonNull
    private final CharacterView characterView;

    @NonNull
    private final CharacterHighlightView highlightView;

    private OnCharacterDisplayClickListener onClickListener;

    public CharacterDisplay(@NonNull Context context, @NonNull ViewGroup parentView) {
        characterView = new CharacterView(context);
        characterView.setOnClickListener(this::onCharacterClick);
        parentView.addView(characterView, getDefaultLayoutParams());

        highlightView = new CharacterHighlightView(context);
        parentView.addView(highlightView, getDefaultLayoutParams());

        reset();
    }

    public void setSize(int size) {
        setSize(highlightView, size);
        setSize(characterView, size);
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
    public CharacterHighlightView getHighlightView() {
        return highlightView;
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

        characterView.setVisibility(View.GONE);
        highlightView.setVisibility(View.GONE);
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

    public void setHighlighted(boolean highlighted, boolean animateCharacterScaling) {
        characterView.scaleForHighlight(highlighted, animateCharacterScaling);
        highlightView.setVisibility(highlighted ? View.VISIBLE : View.GONE);
    }

    public void startHighlightAnimation() {
        highlightView.startAnimation();
    }

    public void stopHighlightAnimation() {
        highlightView.stopAnimation();
    }

    public void clearTarget() {
        characterView.clearTarget();
    }
}
