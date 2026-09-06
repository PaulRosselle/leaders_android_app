package com.leaders.app.animators;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.app.enums.AnimationSpeed;
import com.leaders.app.views.character.PortraitView;
import com.leaders.gamelogic.enums.CharacterCard;

public final class BanishmentActionAnimator extends ActionAnimator {
    public enum BanishmentMotionType {
        Ban,
        Unban
    }

    private static final int DURATION_SCALE = 200;
    private static final int DURATION_SCALE_AND_TRANSLATION = 300;
    private static final int DURATION_SHAKE = 400;
    private static final int DURATION_PORTRAIT_CHANGE = 200;

    private static final float SHAKE_ROTATION = 8f;

    public BanishmentActionAnimator(@NonNull AnimationSpeed speed) {
        super(speed);
    }

    public void animate(@NonNull BanishmentMotionType motionType,
                        @NonNull PortraitView portrait,
                        @NonNull float[] origin, @NonNull float[] dest,
                        @NonNull CharacterCard characterCard,
                        @Nullable Runnable onAnimationEnd) {
        switch (motionType) {
            case Ban: animateBan(portrait, origin, dest, characterCard, onAnimationEnd); break;
            case Unban: animateUnban(portrait, dest, origin, characterCard, onAnimationEnd); break;
            default: throw new IllegalArgumentException("Banishment motion animation not handled: " + motionType);
        }
    }

    private void animateBan(@NonNull PortraitView portrait,
                            @NonNull float[] origin, @NonNull float[] dest,
                            @NonNull CharacterCard characterCard,
                            @Nullable Runnable onAnimationEnd) {
        validatePosition(origin, "origin");
        validatePosition(dest, "dest");

        float[] originParent = screenToParentCoordinates(portrait, origin);
        float[] destParent = screenToParentCoordinates(portrait, dest);

        setupPortrait(portrait, originParent, characterCard, false);

        AnimatorSet appear = createScaleAnimation(
                portrait,
                0f,
                1f,
                new DecelerateInterpolator()
        );

        AnimatorSet shakeAndChange = createShakeAndChangeAnimation(
                portrait,
                characterCard,
                true
        );

        AnimatorSet disappear = createScaleAndTranslationAnimation(
                portrait,
                1f,
                0f,
                originParent,
                destParent,
                new AccelerateDecelerateInterpolator()
        );

        AnimatorSet sequence = new AnimatorSet();
        sequence.playSequentially(appear, shakeAndChange, disappear);
        setAnimationEndListener(sequence, portrait, onAnimationEnd);

        sequence.start();
    }

    private void animateUnban(@NonNull PortraitView portrait,
                              @NonNull float[] origin, @NonNull float[] dest,
                              @NonNull CharacterCard characterCard,
                              @Nullable Runnable onAnimationEnd) {
        validatePosition(origin, "origin");
        validatePosition(dest, "dest");

        float[] originParent = screenToParentCoordinates(portrait, origin);
        float[] destParent = screenToParentCoordinates(portrait, dest);

        setupPortrait(portrait, originParent, characterCard, true);

        AnimatorSet appearAndMove = createScaleAndTranslationAnimation(
                portrait,
                0f,
                1f,
                originParent,
                destParent,
                new DecelerateInterpolator()
        );

        AnimatorSet shakeAndChange = createShakeAndChangeAnimation(
                portrait,
                characterCard,
                false
        );

        AnimatorSet disappear = createScaleAnimation(
                portrait,
                1f,
                0f,
                new AccelerateDecelerateInterpolator()
        );

        AnimatorSet sequence = new AnimatorSet();
        sequence.playSequentially(appearAndMove, shakeAndChange, disappear);

        setAnimationEndListener(sequence, portrait, onAnimationEnd);

        sequence.start();
    }

    @NonNull
    private AnimatorSet createScaleAnimation(@NonNull View view,
                                             float fromScale, float toScale,
                                             @NonNull TimeInterpolator interpolator) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, View.SCALE_X, fromScale, toScale);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, View.SCALE_Y, fromScale, toScale);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(scaleX, scaleY);

        set.setDuration(getAnimationDuration(DURATION_SCALE));
        set.setInterpolator(interpolator);

        return set;
    }

    @NonNull
    private AnimatorSet createScaleAndTranslationAnimation(@NonNull View view,
                                                           float fromScale,
                                                           float toScale,
                                                           @NonNull float[] fromPosition,
                                                           @NonNull float[] toPosition,
                                                           @NonNull TimeInterpolator interpolator) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, View.SCALE_X, fromScale, toScale);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, View.SCALE_Y, fromScale, toScale);

        ObjectAnimator translationX = ObjectAnimator.ofFloat(view, View.TRANSLATION_X, fromPosition[0], toPosition[0]);
        ObjectAnimator translationY = ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, fromPosition[1], toPosition[1]);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(scaleX, scaleY, translationX, translationY);

        set.setDuration(getAnimationDuration(DURATION_SCALE_AND_TRANSLATION));
        set.setInterpolator(interpolator);

        return set;
    }

    @NonNull
    private AnimatorSet createShakeAndChangeAnimation(@NonNull PortraitView portrait,
                                                      @NonNull CharacterCard characterCard,
                                                      boolean useBannedDisplay) {
        ObjectAnimator shake = createShakeAnimation(portrait);

        AnimatorSet portraitChange = createPortraitChangeAnimation(portrait, characterCard, useBannedDisplay);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(shake, portraitChange);

        return set;
    }

    @NonNull
    private ObjectAnimator createShakeAnimation(@NonNull View view) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(
                view,
                View.ROTATION,
                0f,
                -SHAKE_ROTATION,
                SHAKE_ROTATION,
                -SHAKE_ROTATION,
                SHAKE_ROTATION,
                0f
        );

        animator.setDuration(getAnimationDuration(DURATION_SHAKE));
        animator.setInterpolator(new AccelerateDecelerateInterpolator());

        return animator;
    }

    @NonNull
    private AnimatorSet createPortraitChangeAnimation(@NonNull PortraitView portrait,
                                                      @NonNull CharacterCard characterCard,
                                                      boolean changeToBanned) {
        int halfDuration = getAnimationDuration(DURATION_PORTRAIT_CHANGE) / 2;

        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(portrait, View.ALPHA, 1f, 0f);
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(portrait, View.ALPHA, 0f, 1f);

        fadeOut.setDuration(halfDuration);
        fadeIn.setDuration(halfDuration);

        fadeOut.setInterpolator(new AccelerateDecelerateInterpolator());
        fadeIn.setInterpolator(new AccelerateDecelerateInterpolator());

        fadeOut.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(@NonNull Animator animation) {
                // We change the portrait card between the fade out and fade in animations
                portrait.setPortraitCard(characterCard);
                portrait.setUseBannedDisplay(changeToBanned);
            }
        });

        AnimatorSet set = new AnimatorSet();
        set.playSequentially(fadeOut, fadeIn);

        return set;
    }

    private void setupPortrait(@NonNull PortraitView portrait,
                               @NonNull float[] position,
                               @NonNull CharacterCard characterCard,
                               boolean startsAsBanned) {
        portrait.setPortraitCard(characterCard);
        portrait.setUseBannedDisplay(startsAsBanned);

        portrait.setVisibility(View.VISIBLE);

        portrait.setAlpha(1f);

        portrait.setScaleX(0f);
        portrait.setScaleY(0f);

        portrait.setRotation(0f);

        portrait.setTranslationX(position[0]);
        portrait.setTranslationY(position[1]);
    }

    private void setAnimationEndListener(@NonNull Animator animator,
                                         @NonNull PortraitView portrait,
                                         @Nullable Runnable onAnimationEnd) {
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(@NonNull Animator animation) {
                portrait.setVisibility(View.GONE);

                portrait.setAlpha(1f);

                portrait.setScaleX(1f);
                portrait.setScaleY(1f);

                portrait.setRotation(0f);

                portrait.setTranslationX(0f);
                portrait.setTranslationY(0f);

                if (onAnimationEnd != null) {
                    onAnimationEnd.run();
                }
            }
        });
    }

    @NonNull
    private float[] screenToParentCoordinates(@NonNull View view, @NonNull float[] screenPosition) {
        int[] viewLocation = new int[2];
        view.getLocationOnScreen(viewLocation);

        // View.TRANSLATION_X / TRANSLATION_Y are relative to the view's original layout position.
        // Therefore: translation = desired screen position - current screen position
        return new float[]{
                screenPosition[0] - viewLocation[0],
                screenPosition[1] - viewLocation[1]
        };
    }

    private void validatePosition(@NonNull float[] position, @NonNull String name) {
        if (position.length != 2) {
            throw new IllegalArgumentException(name + " must contain exactly two values: [x, y]");
        }
    }
}


