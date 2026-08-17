package com.leaders.app.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.leaders.R;

public final class MainMenuButtonView extends ConstraintLayout {
    public MainMenuButtonView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.view_main_menu_button, this);

        // Loading XML attributes
        try (TypedArray customAttrs = context.obtainStyledAttributes(attrs, R.styleable.MainMenuButtonView)) {
            ImageView imvIcon = findViewById(R.id.imvIcon_vwMainMenuButton);
            imvIcon.setImageResource(customAttrs.getResourceId(R.styleable.MainMenuButtonView_iconResId, R.drawable.icon_pvp));
            TextView txvText = findViewById(R.id.txvText_vwMainMenuButton);
            txvText.setText(customAttrs.getResourceId(R.styleable.MainMenuButtonView_textResId, R.string.duel));
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // The view is always squared, taking its smallest dimension as reference
        int sizeMeasureSpec = widthMeasureSpec;
        if (heightMeasureSpec > 0 && heightMeasureSpec < widthMeasureSpec) {
            sizeMeasureSpec = heightMeasureSpec;
        }
        super.onMeasure(widthMeasureSpec, sizeMeasureSpec);
    }
}
