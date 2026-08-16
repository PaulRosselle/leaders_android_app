package com.leaders.app.views;

import static androidx.core.util.TypedValueCompat.dpToPx;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.material.button.MaterialButton;
import com.leaders.R;

public final class ActionsMenuView extends LinearLayout {
    public ActionsMenuView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        // Actions menu must be displayed vertically
        setOrientation(VERTICAL);
    }

    public void addActionButton(int iconResId, int textResId, int tag, @NonNull OnClickListener onClickListener) {
        Context context = getContext();
        // We use material button for the action buttons since they are easy to customize
        MaterialButton btnAction = new MaterialButton(context);
        // First we apply the values given as arguments
        btnAction.setIconResource(iconResId);
        btnAction.setText(textResId);
        btnAction.setTag(tag);
        btnAction.setOnClickListener(onClickListener);

        // Then we update the button global appearance
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        btnAction.setCornerRadius((int) dpToPx(8, metrics));
        btnAction.setPaddingRelative((int) dpToPx(8, metrics), 0, (int) dpToPx(16, metrics), 0);
        btnAction.setStrokeWidth((int) dpToPx(1, metrics));
        btnAction.setInsetTop(0);
        btnAction.setInsetBottom(0);
        btnAction.setBackgroundTintList(AppCompatResources.getColorStateList(context, R.color.darker_background));
        // Text appearance
        btnAction.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        btnAction.setTypeface(ResourcesCompat.getFont(context, R.font.bobby_jones_condensed));
        btnAction.setIncludeFontPadding(false);
        btnAction.setTextAlignment(TEXT_ALIGNMENT_TEXT_START);
        // Icon appearance
        btnAction.setIconGravity(MaterialButton.ICON_GRAVITY_START);
        btnAction.setIconPadding((int) dpToPx(8, metrics));
        btnAction.setIconSize((int) dpToPx(24, metrics));
        // Every button is initialized as enabled
        setButtonEnabled(btnAction, true);
        addView(btnAction, getButtonLP());
    }

    public void setButtonEnabled(int btnTag, boolean enabled) {
        // Action buttons can be found using their tag
        for (int i = 0; i < getChildCount(); i++) {
            MaterialButton btnChild = (MaterialButton) getChildAt(i);
            if (Integer.parseInt(btnChild.getTag().toString()) == btnTag) {
                setButtonEnabled(btnChild, enabled);
            }
        }
    }

    private void setButtonEnabled(@NonNull MaterialButton button, boolean enabled) {
        button.setEnabled(enabled);
        int colorId;
        if (enabled) {
            colorId = R.color.font;
        } else {
            colorId = R.color.darker_font;
        }
        int colorValue = getContext().getColor(colorId);
        button.setTextColor(colorValue);
        ColorStateList colorStateList = AppCompatResources.getColorStateList(getContext(), colorId);
        button.setIconTint(colorStateList);
        button.setStrokeColor(colorStateList);
    }

    private LinearLayout.LayoutParams getButtonLP() {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
        );

        int marginValueInPx = (int) dpToPx(2, getResources().getDisplayMetrics());
        layoutParams.topMargin = marginValueInPx;
        layoutParams.bottomMargin = marginValueInPx;
        return layoutParams;
    }
}
