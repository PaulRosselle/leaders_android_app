package com.leaders.app.animators;

import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.app.enums.AnimationSpeed;
import com.leaders.app.views.character.CharacterDisplay;

public abstract class ActionAnimator {
    AnimationSpeed speed;

    protected ActionAnimator(@NonNull AnimationSpeed speed) {
        this.speed = speed;
    }

    protected int getAnimationDuration(int duration) {
        return (int) (duration * speed.getMultiplier());
    }

    protected void animateFadeIn(@NonNull CharacterDisplay characterDisplay, int duration,
                                 @Nullable Runnable onAnimationEnd) {
        characterDisplay.getCharacterView().animate().scaleX(1f).scaleY(1f).alpha(1f)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setDuration(getAnimationDuration(duration))
                .withEndAction(onAnimationEnd)
                .start();
    }

    protected void animateFadeOut(@NonNull CharacterDisplay characterDisplay, int duration,
                                  @Nullable Runnable onAnimationEnd) {
        characterDisplay.stopEffectsAnimation();
        characterDisplay.setIsHighlighted(false, false);
        characterDisplay.hide(CharacterDisplay.ViewType.Transformed);

        characterDisplay.getCharacterView().animate().scaleX(0f).scaleY(0f).alpha(0f)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setDuration(getAnimationDuration(duration))
                .withEndAction(onAnimationEnd)
                .start();
    }
}
