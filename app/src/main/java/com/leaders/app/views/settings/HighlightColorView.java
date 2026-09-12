package com.leaders.app.views.settings;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.leaders.R;
import com.leaders.app.enums.HighlightColor;

import java.util.ArrayList;
import java.util.List;

public class HighlightColorView extends ConstraintLayout {
    public interface onColorChangeListener {
        void onColorChange(@NonNull HighlightColor color);
    }

    @NonNull
    private final List<ImageView> colorViews;
    @NonNull
    private HighlightColor color;

    private onColorChangeListener changeListener;


    public HighlightColorView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        colorViews = new ArrayList<>();

        inflate(context, R.layout.view_highlight_color, this);

        setBackgroundResource(R.drawable.round_rect);

        initColors(context);
        setColor(HighlightColor.Default);
    }

    private void initColors(@NonNull Context context) {
        LinearLayout llyColors = findViewById(R.id.llyColors_vwHighlightColor);
        int highlightColorSize = getResources().getDimensionPixelSize(R.dimen.round_button_size);

        for (HighlightColor highlightColor : HighlightColor.values()) {
            ImageView imvHighlightColor = new ImageView(context);
            imvHighlightColor.setImageResource(highlightColor.getColorResId());
            imvHighlightColor.setForeground(AppCompatResources.getDrawable(context, R.drawable.circular_ripple));
            imvHighlightColor.setOnClickListener(this::onColorClick);

            LinearLayout llyContainer = new LinearLayout(context);
            llyContainer.setGravity(Gravity.CENTER);
            llyContainer.addView(imvHighlightColor, getImvColorLayoutParams(highlightColorSize));
            llyColors.addView(llyContainer, getLlyContainerLayoutParams());

            colorViews.add(imvHighlightColor);
        }
    }

    public void setColor(@NonNull HighlightColor color) {
        this.color = color;

        for (int i = 0; i < colorViews.size(); i++) {
            colorViews.get(i).animate()
                    .alpha(i == color.ordinal() ? 1f : 0.3f)
                    .setDuration(200)
                    .start();
        }
    }

    @NonNull
    public HighlightColor getColor() {
        return color;
    }

    public void setChangeListener(onColorChangeListener changeListener) {
        this.changeListener = changeListener;
    }

    private LinearLayout.LayoutParams getImvColorLayoutParams(int size) {
        return new LinearLayout.LayoutParams(size, size);
    }

    private LinearLayout.LayoutParams getLlyContainerLayoutParams() {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        layoutParams.weight = 1;

        return layoutParams;
    }

    private void onColorClick(View v) {
        HighlightColor changeColor = HighlightColor.values()[colorViews.indexOf((ImageView) v)];
        setColor(changeColor);

        if (changeListener != null) {
            changeListener.onColorChange(changeColor);
        }
    }
}
