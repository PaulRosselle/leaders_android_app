package com.leaders.app.animators;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.app.enums.AnimationSpeed;
import com.leaders.app.views.board.BoardView;
import com.leaders.app.views.board.CellView;
import com.leaders.app.views.character.CharacterDisplay;
import com.leaders.app.views.character.CharacterView;
import com.leaders.gamelogic.actions.RecruitmentAction;
import com.leaders.gamelogic.actions.RecruitmentActionMotion;
import com.leaders.gamelogic.entities.Position;

import java.util.List;

public final class RecruitmentActionAnimator extends ActionAnimator<RecruitmentAction> {
    private static final int DURATION_ADD = 200;
    private static final int DURATION_REMOVE = 200;

    public RecruitmentActionAnimator(@NonNull AnimationSpeed speed) {
        super(speed);
    }

    public void animate(@NonNull BoardView boardView,
                               @NonNull RecruitmentAction action,
                               @Nullable Runnable onAnimationEnd) {
        animate(boardView, action.getMotions(), onAnimationEnd);
    }

    public void animate(@NonNull BoardView boardView,
                               @NonNull List<RecruitmentActionMotion> motions,
                               @Nullable Runnable onAnimationEnd) {
        if (motions.isEmpty()) {
            if (onAnimationEnd != null) {
                onAnimationEnd.run();
            }
            return;
        }

        animateMotionSequence(boardView, motions, 0, onAnimationEnd);
    }

    public void animate(@NonNull BoardView boardView,
                               @NonNull RecruitmentActionMotion motion,
                               @Nullable Runnable onAnimationEnd) {
        switch (motion.getMotionType()) {
            case Add: animateAddCharacter(boardView, motion, onAnimationEnd); break;
            case Remove: animateRemoveCharacter(boardView, motion, onAnimationEnd); break;
            default: throw new IllegalArgumentException("Recruitment motion animation not handled: " + motion.getMotionType());
        }
    }

    private void animateMotionSequence(@NonNull BoardView boardView,
                                              @NonNull List<RecruitmentActionMotion> motions,
                                              int index, @Nullable Runnable onAnimationEnd) {
        if (index >= motions.size()) {
            if (onAnimationEnd != null) {
                onAnimationEnd.run();
            } return;
        }

        RecruitmentActionMotion motion = motions.get(index);

        animate(boardView, motion, () ->
                animateMotionSequence(boardView, motions, index + 1, onAnimationEnd)
        );
    }

    private void animateAddCharacter(@NonNull BoardView boardView,
                                            @NonNull RecruitmentActionMotion motion,
                                            @Nullable Runnable onAnimationEnd) {

        Position addPos = motion.getPosition();

        CharacterDisplay characterDisplay = boardView.acquireCharacterDisplay(addPos);
        CellView addCellView = boardView.getCellView(addPos);
        characterDisplay.setPosition(addCellView.getX(), addCellView.getY());
        CharacterView characterView = characterDisplay.getCharacterView();
        characterView.setCharacter(motion.getCharacter());
        characterView.setScaleX(0f);
        characterView.setScaleY(0f);
        characterView.setAlpha(0f);
        characterView.setVisibility(View.VISIBLE);

        animateFadeIn(characterDisplay, DURATION_ADD, onAnimationEnd);
    }

    private void animateRemoveCharacter(@NonNull BoardView boardView,
                                               @NonNull RecruitmentActionMotion motion,
                                               @Nullable Runnable onAnimationEnd) {
        final Position removePos = motion.getPosition();

        CharacterDisplay characterDisplay = boardView.getCharacterDisplay(removePos);
        animateFadeOut(characterDisplay, DURATION_REMOVE, () -> {
            boardView.releaseCharacterDisplay(removePos);

            if (onAnimationEnd != null) {
                onAnimationEnd.run();
            }
        });
    }
}
