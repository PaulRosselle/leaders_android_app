package com.leaders.app.views.animators;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.app.views.character.CharacterDisplay;
import com.leaders.app.views.character.CharacterView;

public class ActionAnimator {
    private ActionAnimator(){
        throw new AssertionError("Cannot instantiate an animator class");
    }


    public static int getAnimationDuration(int duration) {
        // Will be helpfull to add a global animation speed ratio
        return duration;
    }

    public static void animateFadeIn(@NonNull CharacterDisplay characterDisplay, int duration,
                                      @Nullable Runnable onAnimationEnd) {
        characterDisplay.getCharacterView().animate().scaleX(1f).scaleY(1f).alpha(1f)
                .setDuration(getAnimationDuration(duration))
                .withEndAction(onAnimationEnd)
                .start();
    }

    public static void animateFadeOut(@NonNull CharacterDisplay characterDisplay, int duration,
                                       @Nullable Runnable onAnimationEnd) {
        CharacterView characterView = characterDisplay.getCharacterView();
        characterDisplay.stopHighlightAnimation();
        characterDisplay.getHighlightView().setVisibility(View.GONE);
        characterDisplay.getShadowView().setVisibility(View.GONE);

        characterView.animate().scaleX(0f).scaleY(0f).alpha(0f)
                .setDuration(getAnimationDuration(duration))
                .withEndAction(onAnimationEnd)
                .start();
    }
}
