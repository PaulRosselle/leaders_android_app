package com.leaders.app.views.settings;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;

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
        int padding = dpToPx(density, 16);
        setPadding(padding, padding, padding, padding);
        setThumbDrawable(ContextCompat.getDrawable(context, R.drawable.seekbar_thumb));
        setBackgroundResource(R.drawable.round_rect);

        // TEXT
        setText(R.string.playable_items_animation);
        setTextColor(ContextCompat.getColor(context, R.color.font));
        setAllCaps(true);

        setAutoSizeTextTypeUniformWithConfiguration(
                dpToPx(density, 14),  // min size
                dpToPx(density, 18),  // max size
                dpToPx(density, 1),   // granularity
                TypedValue.COMPLEX_UNIT_PX
        );
        setTypeface(Typeface.create("sans-serif", Typeface.BOLD));

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
