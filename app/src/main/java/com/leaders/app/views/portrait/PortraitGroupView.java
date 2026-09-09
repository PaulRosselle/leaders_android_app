package com.leaders.app.views.portrait;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.R;
import com.leaders.app.entities.PortraitInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class PortraitGroupView extends LinearLayout {
    private static final int PORTRAIT_DEFAULT_MARGIN = 2;

    private final ArrayList<PortraitView> portraitViews;
    private OnClickListener onPortraitClickListener;
    private OnLongClickListener onPortraitLongClickListener;
    private int portraitSpacing;

    public PortraitGroupView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        portraitViews = new ArrayList<>();
        onPortraitClickListener = null;
        onPortraitLongClickListener = null;
        portraitSpacing = PORTRAIT_DEFAULT_MARGIN;

        try (TypedArray customAttrs = context.obtainStyledAttributes(attrs, R.styleable.PortraitGroupView)) {
            int groupSize = customAttrs.getInteger(R.styleable.PortraitGroupView_maxGroupSize, 6);
            setPortraits(new ArrayList<>(), groupSize);
        }

        setOrientation(HORIZONTAL);
    }

    public PortraitGroupView(Context context, @NonNull List<PortraitInfo> portraitInfos, int groupSize) {
        this(context, null);

        setPortraits(portraitInfos, groupSize);
    }

    public void setPortraits(@NonNull List<PortraitInfo> portraitInfos, int groupSize) {
        // The smallest value for group max size is 2 since it wouldn't make
        // sense to create a group view for less than two portraits
        groupSize = Math.max(groupSize, 2);

        portraitViews.clear();
        removeAllViews();

        for (int i = 0; i < groupSize; i++) {
            PortraitView portraitView = new PortraitView(getContext(), null);

            if (i < portraitInfos.size()) {
                portraitView.setVisibility(VISIBLE);
                portraitView.setInfo(portraitInfos.get(i));
            } else {
                portraitView.setVisibility(INVISIBLE);
            }

            addView(portraitView, getPortraitLayoutParams());
            portraitViews.add(portraitView);
        }

        updatePortraitsClickListener();
        updatePortraitsLongClickListener();
    }

    public List<PortraitView> getPortraits() {
        List<PortraitView> portraitViews = new ArrayList<>();
        for (PortraitView portraitView : this.portraitViews) {
            if (portraitView.getVisibility() == VISIBLE) {
                portraitViews.add(portraitView);
            }
        }

        return Collections.unmodifiableList(portraitViews);
    }

    public void setPortraitSpacing(int portraitSpacing) {
        this.portraitSpacing = portraitSpacing;
        for (PortraitView ptvPortrait : portraitViews) {
            ptvPortrait.setLayoutParams(getPortraitLayoutParams());
        }
    }

    private LinearLayout.LayoutParams getPortraitLayoutParams() {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        int marginValueInPx = (int) (portraitSpacing * getResources().getDisplayMetrics().density);
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
        for (PortraitView portraitView : portraitViews) {
            portraitView.setOnClickListener(onPortraitClickListener);
        }
    }

    private void updatePortraitsLongClickListener() {
        for (PortraitView portraitView : portraitViews) {
            portraitView.setOnLongClickListener(onPortraitLongClickListener);
        }
    }
}
