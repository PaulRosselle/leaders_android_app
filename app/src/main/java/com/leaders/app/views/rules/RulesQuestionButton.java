package com.leaders.app.views.rules;

import static androidx.core.util.TypedValueCompat.dpToPx;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.material.button.MaterialButton;
import com.leaders.R;

public class RulesQuestionButton extends MaterialButton {
    public RulesQuestionButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        Resources resources = getResources();

        // SHAPE
        DisplayMetrics metrics = resources.getDisplayMetrics();
        // TODO - use static int for values
        setCornerRadius(resources.getDimensionPixelSize(R.dimen.default_corner_radius));
        int horizontalPadding = (int) dpToPx(16, metrics);
        int verticalPadding = (int) dpToPx(8, metrics);
        setPaddingRelative(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding);
        setInsetTop(0);
        setInsetBottom(0);
        setInsetLeft(0);
        setInsetRight(0);

        // STROKE
        setStrokeWidth(resources.getDimensionPixelSize(R.dimen.default_stroke_width));
        setStrokeColor(ContextCompat.getColorStateList(context, R.color.font));

        // BACKGROUND
        setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.rules_button));

        // FONT
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        setTypeface(ResourcesCompat.getFont(context, R.font.bobby_jones_condensed));
        setIncludeFontPadding(false);
        setTextAlignment(TEXT_ALIGNMENT_CENTER);
    }
}
