package com.leaders.app.views.character;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.leaders.R;

public final class CharacterDisplay {
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
        setPosition(characterView, x, y);
        setPosition(shadowView, x, y);
        setPosition(highlightView, x, y);
    }

    private void setPosition(@NonNull View view, float x, float y) {
        view.setX(x);
        view.setY(y);
    }

    public void setOnCharacterClickListener(View.OnClickListener onClickListener) {
        characterView.setOnClickListener(onClickListener);
    }

    public void setOnCharacterLongClickListener(View.OnLongClickListener onLongClickListener) {
        characterView.setOnLongClickListener(onLongClickListener);
    }

    public void setHighlighted(boolean highlighted, boolean animate) {
        characterView.scaleForHighlight(highlighted, animate);
        highlightView.setVisibility(highlighted ? View.VISIBLE : View.GONE);
    }
}
