package com.leaders.app.views.settings;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.util.AttributeSet;

import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;

import com.leaders.R;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class PlayableItemsAnimationView extends SwitchCompat {
    public PlayableItemsAnimationView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        // Selected by default
        setChecked(true);

        // SHAPE
        float density = context.getResources().getDisplayMetrics().density;
        int paddingHorizontal = dpToPx(density, 16);
        int paddingVertical = dpToPx(density, 12);
        setPadding(paddingHorizontal, paddingVertical, paddingHorizontal, paddingVertical);
        setThumbDrawable(ContextCompat.getDrawable(context, R.drawable.seekbar_thumb));
        setBackgroundResource(R.drawable.round_rect);

        // TEXT
        setText(R.string.playable_items_animation);
        setTextColor(ContextCompat.getColor(context, R.color.font));
        setTextSize(getResources().getDimensionPixelSize(R.dimen.default_text_font_size));
        setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));

        // TRACK COLOR
        ColorStateList trackTint = new ColorStateList(
                new int[][] {
                        new int[] { android.R.attr.state_checked },
                        new int[] {}
                },
                new int[] {
                        ContextCompat.getColor(context, R.color.track_on_golden),
                        ContextCompat.getColor(context, R.color.track_off_golden)
                }
        );
        setTrackTintList(trackTint);
    }

    private static int dpToPx(float density, int dp) {
        return (int) (dp * density);
    }
}
