package com.leaders.app.animators;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.app.enums.AnimationSpeed;
import com.leaders.app.views.board.BoardView;
import com.leaders.app.views.character.CharacterDisplay;
import com.leaders.app.views.character.CharacterView;
import com.leaders.gamelogic.actions.IGameAction;

public abstract class ActionAnimator<T extends IGameAction> {
    AnimationSpeed speed;

    protected ActionAnimator(@NonNull AnimationSpeed speed) {
        this.speed = speed;
    }

    public abstract void animate(@NonNull BoardView boardView,
                                 @NonNull T action,
                                 @Nullable Runnable onAnimationEnd);

    protected int getAnimationDuration(int duration) {
        return (int) (duration * speed.getMultiplier());
    }

    protected void animateFadeIn(@NonNull CharacterDisplay characterDisplay, int duration,
                              @Nullable Runnable onAnimationEnd) {
        characterDisplay.getCharacterView().animate().scaleX(1f).scaleY(1f).alpha(1f)
                .setDuration(getAnimationDuration(duration))
                .withEndAction(onAnimationEnd)
                .start();
    }

    protected void animateFadeOut(@NonNull CharacterDisplay characterDisplay, int duration,
                                  @Nullable Runnable onAnimationEnd) {
        CharacterView characterView = characterDisplay.getCharacterView();
        characterDisplay.setIsHighlighted(false, false);
        characterDisplay.stopHighlightAnimation();

        characterView.animate().scaleX(0f).scaleY(0f).alpha(0f)
                .setDuration(getAnimationDuration(duration))
                .withEndAction(onAnimationEnd)
                .start();
    }
}
