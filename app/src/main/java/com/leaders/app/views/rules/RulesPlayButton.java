package com.leaders.app.views.rules;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.leaders.R;

public class RulesPlayButton extends MaterialButton {
    public RulesPlayButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        Resources resources = context.getResources();

        // DEFAULT DIMENSIONS
        int size = resources.getDimensionPixelSize(R.dimen.play_button_size);
        setHeight(size);
        setWidth(size);

        // SHAPE
        setCornerRadius(size / 2);
        setPadding(0, 0, 0, 0);
        setInsetTop(0);
        setInsetBottom(0);

        // BACKGROUND
        setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.dark_transparent_black));

        // ICON
        setText("");
        setIconTint(ContextCompat.getColorStateList(context, R.color.white));
        setIconPadding(0);
        setIconGravity(ICON_GRAVITY_TEXT_START);
        setIconResource(R.drawable.icon_play);
        setIconSize(resources.getDimensionPixelSize(R.dimen.play_button_icon_size));

        // RATIO
        if (getLayoutParams() instanceof ConstraintLayout.LayoutParams) {
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) getLayoutParams();
            params.dimensionRatio = "1:1";
            setLayoutParams(params);
        }
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // The view is always squared, taking its biggest dimension as reference
        setCornerRadius(Math.max(widthMeasureSpec, heightMeasureSpec) / 2);

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
