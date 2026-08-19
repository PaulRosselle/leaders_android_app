package com.leaders.app.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.R;
import com.leaders.gamelogic.enums.CharacterCard;

import java.util.ArrayList;

public final class CharacterCardPortraitGroupView extends LinearLayout {
    private static final int PORTRAIT_DEFAULT_MARGIN = 2;

    private final ArrayList<CharacterCardPortraitView> characterCardPortraitViews;
    private OnClickListener onPortraitClickListener;
    private OnLongClickListener onPortraitLongClickListener;
    private int portraitMargins;


    public CharacterCardPortraitGroupView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        characterCardPortraitViews = new ArrayList<>();
        onPortraitClickListener = null;
        onPortraitLongClickListener = null;
        portraitMargins = PORTRAIT_DEFAULT_MARGIN;

        try (TypedArray customAttrs = context.obtainStyledAttributes(attrs, R.styleable.CharacterCardPortraitGroupView)) {
            int groupSize = customAttrs.getInteger(R.styleable.CharacterCardPortraitGroupView_maxGroupSize, 6);
            setPortraits(new ArrayList<>(), groupSize);
        }

        setOrientation(HORIZONTAL);
    }

    public CharacterCardPortraitGroupView(@NonNull Context context,
                                          @NonNull ArrayList<CharacterCard> portraitCards,
                                          int groupSize) {
        this(context, null);
        setPortraits(portraitCards, groupSize);
    }

    public void setPortraits(@NonNull ArrayList<CharacterCard> portraitCards, int groupSize) {
        // The smallest value for group max size is 2 since it wouldn't make
        // sense to create a group view for less than two portraits
        groupSize = Math.max(groupSize, 2);

        characterCardPortraitViews.clear();
        removeAllViews();
        for (int i = 0; i < groupSize; i++) {
            CharacterCardPortraitView portraitView = new CharacterCardPortraitView(getContext(), null);

            if (i < portraitCards.size()) {
                portraitView.setVisibility(VISIBLE);
                portraitView.setPortraitCard(portraitCards.get(i));
            } else {
                portraitView.setVisibility(INVISIBLE);
            }

            addView(portraitView, getPortraitLayoutParams());
            characterCardPortraitViews.add(portraitView);
        }

        updatePortraitsClickListener();
        updatePortraitsLongClickListener();
    }

    public void setPortraitMargins(int portraitMargins) {
        this.portraitMargins = portraitMargins;
        for (CharacterCardPortraitView ptvPortrait : characterCardPortraitViews) {
            ptvPortrait.setLayoutParams(getPortraitLayoutParams());
        }
    }

    private LinearLayout.LayoutParams getPortraitLayoutParams() {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        int marginValueInPx = (int) (portraitMargins * getResources().getDisplayMetrics().density);
        layoutParams.setMarginStart(marginValueInPx);
        layoutParams.setMarginEnd(marginValueInPx);
        layoutParams.weight = 1;
        return layoutParams;
    }

    public void setPortraitsClickListener(OnClickListener onPortraitClickListener) {
        this.onPortraitClickListener = onPortraitClickListener;
        updatePortraitsClickListener();
    }

    public void setPortraitsLongClickListener(OnLongClickListener onPortraitLongClickListener) {
        this.onPortraitLongClickListener = onPortraitLongClickListener;
        updatePortraitsLongClickListener();
    }

    private void updatePortraitsClickListener() {
        for (CharacterCardPortraitView characterCardPortraitView : characterCardPortraitViews) {
            characterCardPortraitView.setOnClickListener(onPortraitClickListener);
        }
    }

    private void updatePortraitsLongClickListener() {
        for (CharacterCardPortraitView characterCardPortraitView : characterCardPortraitViews) {
            characterCardPortraitView.setOnLongClickListener(onPortraitLongClickListener);
        }
    }

}
