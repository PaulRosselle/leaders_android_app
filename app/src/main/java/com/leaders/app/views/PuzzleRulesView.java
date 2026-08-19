package com.leaders.app.views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import com.leaders.R;

public final class PuzzleRulesView extends TextView {
    public PuzzleRulesView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        setBackgroundResource(R.drawable.round_rect_gloden_outline_bg);

        setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.puzzle_rules_text_font_size));
        setTypeface(ResourcesCompat.getFont(context, R.font.bourbon_test_regular));
        setTextColor(getContext().getColor(R.color.font));
        setIncludeFontPadding(false);
        setGravity(Gravity.CENTER);


        float densityRatio = getResources().getDisplayMetrics().density;
        int defaultPadding = (int) (4 * densityRatio);
        int bottomPadding = defaultPadding + (int) (4 * densityRatio);
        setPadding(defaultPadding, defaultPadding, defaultPadding, bottomPadding);

        setClickable(false);

        setText(R.string.formatted_puzzle_rules);
    }
}
