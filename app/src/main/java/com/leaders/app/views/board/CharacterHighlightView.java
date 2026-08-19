package com.leaders.app.views.board;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.LinearInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import com.leaders.R;

public final class CharacterHighlightView extends AppCompatImageView {
    private static final int HIGHLIGHT_ANIMATION_DURATION = 800;
    private final ObjectAnimator animator;

    public CharacterHighlightView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setImageResource(R.drawable.character_highlight);

        animator = ObjectAnimator.ofFloat(this, ROTATION, 0f, 360f);
        animator.setDuration(HIGHLIGHT_ANIMATION_DURATION);
        animator.setInterpolator(new LinearInterpolator());
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.RESTART);
    }

    public CharacterHighlightView(@NonNull Context context) {
        this(context, null);
    }

    public void startAnimation() {
        if (animator.isStarted()) {
            stopAnimation();
        }
        animator.start();
    }

    public void stopAnimation() {
        animator.cancel();
        setRotation(0f);
    }

    @Override
    protected void onDetachedFromWindow() {
        stopAnimation();
        super.onDetachedFromWindow();
    }
}
