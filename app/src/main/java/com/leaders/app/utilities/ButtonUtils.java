package com.leaders.app.utilities;

import android.content.Context;
import android.content.res.ColorStateList;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;

import com.google.android.material.button.MaterialButton;
import com.leaders.R;

public final class ButtonUtils {
    private ButtonUtils(){
        throw new AssertionError("Cannot instantiate utility class");
    }

    public static void setButtonEnabled(@NonNull MaterialButton button, boolean enabled) {
        button.setEnabled(enabled);

        int strokeAndFontColor = enabled ? R.color.font : R.color.darker_font;
        Context context = button.getContext();
        button.setTextColor(context.getColor(strokeAndFontColor));
        ColorStateList strokeAndForColorStateList = AppCompatResources.getColorStateList(context, strokeAndFontColor);
        button.setIconTint(strokeAndForColorStateList);
        button.setStrokeColor(strokeAndForColorStateList);

        int backgroundColor = enabled ? R.color.darker_background : R.color.background;
        button.setBackgroundTintList(AppCompatResources.getColorStateList(context, backgroundColor));
    }

}
