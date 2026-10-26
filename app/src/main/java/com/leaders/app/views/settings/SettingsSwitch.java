package com.leaders.app.views.settings;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.StringRes;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;

import com.leaders.R;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class SettingsSwitch extends FrameLayout {
    private final TextView txvTitle;
    private final SwitchCompat swtState;
    private final int spacing;

    public SettingsSwitch(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);


        swtState = new SwitchCompat(context);
        txvTitle = new TextView(context);

        float density = context.getResources().getDisplayMetrics().density;
        spacing = (int) (16 * density);
        setPadding(spacing, spacing, spacing, spacing);
        setBackgroundResource(R.drawable.round_rect);

        initSwitch(context);
        initTitle(context);

        try (TypedArray customAttrs = context.obtainStyledAttributes(attrs, R.styleable.SettingsSwitch)) {
            int titleResId = customAttrs.getResourceId(R.styleable.SettingsSwitch_titleResId, -1);
            if (titleResId != -1) {
                setTitle(titleResId);
            }

            int switchTextResId = customAttrs.getResourceId(R.styleable.SettingsSwitch_switchTextResId, -1);
            if (switchTextResId != -1) {
                setSwitchText(switchTextResId);
            }
        }
    }

    private void initSwitch(@NonNull Context context) {
        swtState.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        // Thie switch is selected by default
        swtState.setChecked(true);

        // SHAPE
        swtState.setThumbDrawable(ContextCompat.getDrawable(context, R.drawable.seekbar_thumb));

        // TEXT
        swtState.setTextColor(ContextCompat.getColor(context, R.color.darker_font));
        swtState.setSingleLine(false);
        swtState.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        swtState.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));

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
        swtState.setTrackTintList(trackTint);

        addView(swtState);
    }

    private void initTitle(@NonNull Context context) {
        LayoutParams titleParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

        titleParams.gravity = Gravity.TOP | Gravity.START;
        txvTitle.setLayoutParams(titleParams);

        txvTitle.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
        txvTitle.setAllCaps(true);
        txvTitle.setTextColor(ContextCompat.getColor(context, R.color.font));
        txvTitle.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                context.getResources().getDimension(R.dimen.smaller_text_font_size)
        );

        addView(txvTitle);
    }

    // ---------- API PUBLIQUE ----------

    public void setTitle(@Nullable CharSequence title) {
        txvTitle.setText(title);
    }

    public void setTitle(@StringRes int resId) {
        txvTitle.setText(resId);
    }

    public boolean isChecked() {
        return swtState.isChecked();
    }

    public void setChecked(boolean checked) {
        swtState.setChecked(checked);
    }

    public void setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener listener) {
        swtState.setOnCheckedChangeListener(listener);
    }

    public void setSwitchText(@Nullable CharSequence text) {
        swtState.setText(text);
    }
    public void setSwitchText(@StringRes int resId) {
        swtState.setText(resId);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measureChildWithMargins(txvTitle, widthMeasureSpec, 0, heightMeasureSpec, 0);

        LayoutParams switchParams = (LayoutParams) swtState.getLayoutParams();

        // Switch is displayed below the title
        switchParams.topMargin = txvTitle.getMeasuredHeight() + spacing / 2;

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}