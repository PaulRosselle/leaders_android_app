package com.leaders.app.utilities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.app.animators.CharacterActionAnimator;
import com.leaders.app.animators.RecruitmentActionAnimator;
import com.leaders.app.entities.CharacterSkins;
import com.leaders.app.enums.AnimationSpeed;
import com.leaders.app.views.board.BoardView;
import com.leaders.gamelogic.actions.CharacterAction;
import com.leaders.gamelogic.actions.IGameAction;
import com.leaders.gamelogic.actions.RecruitmentAction;
import com.leaders.gamelogic.enums.GameActionType;

import java.util.List;

public final class GameActionUtils {

    private GameActionUtils() {
        throw new AssertionError("Cannot instantiate utility class");
    }

    public static boolean isAnimatable(@NonNull IGameAction action) {
        return List.of(
                GameActionType.CharacterAction,
                GameActionType.Recruitment,
                GameActionType.Banishment
        ).contains(action.getActionType());
    }

    public static void animate(@NonNull BoardView boardView, @NonNull IGameAction action,
                               @Nullable Runnable onAnimationEnd,
                               @NonNull AnimationSpeed speed,
                               @NonNull CharacterSkins characterSkins) {
        switch (action.getActionType()) {
            case CharacterAction:
                new CharacterActionAnimator(speed, characterSkins)
                        .animate(boardView, (CharacterAction) action, onAnimationEnd);
                break;
            case Recruitment:
                new RecruitmentActionAnimator(speed, characterSkins)
                        .animate(boardView, (RecruitmentAction) action, onAnimationEnd);
                break;
            default:
                throw new IllegalStateException("Action animation not handled for: " + action.getActionType());
        }
    }
}