package com.leaders.app.views.character;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.leaders.R;

public final class CharacterDisplay {
    public enum ViewType {
        Shadow,
        Character,
        Highlight
    }

    @NonNull
    private final CharacterView characterView;

    @NonNull
    private final ImageView shadowView;

    @NonNull
    private final CharacterHighlightView highlightView;


    public CharacterDisplay(@NonNull Context context, @NonNull ViewGroup parentView) {
        characterView = new CharacterView(context);
        parentView.addView(characterView, getDefaultLayoutParams());

        shadowView = new ImageView(context);
        shadowView.setImageResource(R.drawable.character_piece_shadow);
        parentView.addView(shadowView, getDefaultLayoutParams());

        highlightView = new CharacterHighlightView(context);
        parentView.addView(highlightView, getDefaultLayoutParams());

        reset();
    }

    public void setSize(int size) {
        setSize(highlightView, size);
        setSize(characterView, size);
        setSize(shadowView, size);
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
    public ImageView getShadowView() {
        return shadowView;
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
        // TODO - take into account in progress animations ?

        characterView.clearTarget();

        characterView.setVisibility(View.GONE);
        shadowView.setVisibility(View.GONE);
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
            case Shadow: return shadowView;
            case Character: return characterView;
            case Highlight: return highlightView;
            default: throw new IllegalArgumentException("No character view found for type: " + viewType);
        }
    }

    public void setOnCharacterClickListener(View.OnClickListener onClickListener) {
        characterView.setOnClickListener(onClickListener);
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
}
