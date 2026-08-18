package com.leaders.app.views.board;

import android.content.Context;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;

import com.leaders.R;

public class CharacterHighlightView extends AppCompatImageView {
    private static final int HIGHLIGHT_ANIMATION_DURATION = 800;

    public CharacterHighlightView(@NonNull Context context) {
        super(context);
        setImageResource(R.drawable.character_highlight);
    }

    public void startAnimation() {
        RotateAnimation rotateAnimation = new RotateAnimation(
                0f, 360f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );

        rotateAnimation.setDuration(HIGHLIGHT_ANIMATION_DURATION);
        rotateAnimation.setRepeatCount(Animation.INFINITE);
        rotateAnimation.setInterpolator(new LinearInterpolator());

        startAnimation(rotateAnimation);
    }

    public void stopAnimation() {
        clearAnimation();
    }
}
