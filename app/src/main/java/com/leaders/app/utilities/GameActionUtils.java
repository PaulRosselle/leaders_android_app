package com.leaders.app.utilities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.app.views.animators.CharacterActionAnimator;
import com.leaders.app.views.animators.RecruitmentActionAnimator;
import com.leaders.app.views.board.BoardView;
import com.leaders.gamelogic.actions.CharacterAction;
import com.leaders.gamelogic.actions.CharacterActionMotion;
import com.leaders.gamelogic.actions.CharacterActionTarget;
import com.leaders.gamelogic.actions.IGameAction;
import com.leaders.gamelogic.actions.RecruitmentAction;
import com.leaders.gamelogic.actions.RecruitmentActionMotion;
import com.leaders.gamelogic.actions.WarningAction;
import com.leaders.gamelogic.enums.CharacterMotionType;
import com.leaders.gamelogic.enums.GameActionType;
import com.leaders.gamelogic.enums.RecruitmentMotionType;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class dedicated to reversing {@link IGameAction} instances.
 * <p>
 * Every {@link IGameAction} is designed to be 100% reversible: for any action that was
 * applied to the game, {@link #reverse(IGameAction)} produces the action that, once applied
 * in turn, cancels out its effect.
 */
public final class GameActionUtils {

    private GameActionUtils() {
        throw new AssertionError("Cannot instantiate utility class");
    }

    public static boolean isAnimatable(@NonNull IGameAction action) {
        return List.of(
                GameActionType.CharacterAction,
                GameActionType.Recruitment
        ).contains(action.getActionType());
    }

    public static boolean isReversible(@NonNull IGameAction action) {
        return List.of(
                GameActionType.CharacterAction,
                GameActionType.Recruitment,
                GameActionType.Warning
        ).contains(action.getActionType());
    }

    /**
     * Returns the reverse of the given {@link IGameAction}.
     * <p>
     * Applying the returned action undoes the effect of the given action.
     *
     * @param action the action to reverse
     * @return the reverse of {@code action}
     * @throws IllegalArgumentException if the action type is not handled
     */
    @NonNull
    public static IGameAction reverse(@NonNull IGameAction action) {
        switch (action.getActionType()) {
            case CharacterAction:
                return reverseCharacterAction((CharacterAction) action);
            case Recruitment:
                return reverseRecruitmentAction((RecruitmentAction) action);
            case Warning:
                return reverseWarningAction((WarningAction) action);
            default:
                throw new IllegalArgumentException("Non reversible game action type: " + action.getActionType());
        }
    }

    /**
     * Reverses a {@link CharacterAction}.
     * <p>
     * The motions are reversed individually, and their order is reversed as well: undoing a
     * sequence of motions requires undoing the last one first (LIFO order).
     */
    @NonNull
    private static CharacterAction reverseCharacterAction(@NonNull CharacterAction action) {
        List<CharacterActionMotion> motions = action.getMotions();
        List<CharacterActionMotion> reversedMotions = new ArrayList<>(motions.size());

        for (int i = motions.size() - 1; i >= 0; i--) {
            reversedMotions.add(reverseCharacterActionMotion(motions.get(i)));
        }

        return new CharacterAction(action.getSrcCharacter(), reversedMotions);
    }

    /**
     * Reverses a single {@link CharacterActionMotion} according to its {@link CharacterMotionType}.
     */
    @NonNull
    private static CharacterActionMotion reverseCharacterActionMotion(@NonNull CharacterActionMotion motion) {
        List<CharacterActionTarget> targets = motion.getTargets();
        List<CharacterActionTarget> reversedTargets = new ArrayList<>(motion.getTargets().size());

        for (int i = targets.size() - 1; i >= 0; i--) {
            reversedTargets.add(reverseCharacterTarget(targets.get(i)));
        }
        CharacterMotionType reversedMotionType = getReverseCharacterMotionType(motion.getMotionType());

        return new CharacterActionMotion(reversedMotionType, reversedTargets);
    }

    private static CharacterMotionType getReverseCharacterMotionType(@NonNull CharacterMotionType motionType) {
        switch (motionType) {
            case Push:
                return CharacterMotionType.Move;
            case Add:
                return CharacterMotionType.Remove;
            case Remove:
                return CharacterMotionType.Add;
            default:
                return motionType;
        }
    }

    /**
     * Reverses a character target.
     */
    private static CharacterActionTarget reverseCharacterTarget(@NonNull CharacterActionTarget target) {
        return new CharacterActionTarget(target.getCharacter(), target.getDestPos(), target.getOriginPos());
    }

    /**
     * Reverses a {@link RecruitmentAction}.
     * <p>
     * The motions are reversed individually, and their order is reversed as well (LIFO order).
     */
    @NonNull
    private static RecruitmentAction reverseRecruitmentAction(@NonNull RecruitmentAction action) {
        List<RecruitmentActionMotion> motions = action.getMotions();
        List<RecruitmentActionMotion> reversedMotions = new ArrayList<>(motions.size());
        for (int i = motions.size() - 1; i >= 0; i--) {
            reversedMotions.add(reverseRecruitmentActionMotion(motions.get(i)));
        }
        return new RecruitmentAction(reversedMotions);
    }

    /**
     * Reverses a single {@link RecruitmentActionMotion}: {@code Add} becomes {@code Remove} and
     * vice versa, keeping the same character and position.
     */
    @NonNull
    private static RecruitmentActionMotion reverseRecruitmentActionMotion(@NonNull RecruitmentActionMotion motion) {
        RecruitmentMotionType reversedType = motion.getMotionType() == RecruitmentMotionType.Add ?
                RecruitmentMotionType.Remove : RecruitmentMotionType.Add;
        return new RecruitmentActionMotion(reversedType, motion.getCharacter(), motion.getPosition());
    }

    /**
     * Reverses a {@link WarningAction} by negating its count change, keeping the same warning
     * type and team color.
     */
    @NonNull
    private static WarningAction reverseWarningAction(@NonNull WarningAction action) {
        return new WarningAction(action.getWarningType(), action.getTeamColor(), -action.getCountChange());
    }

    public static void animate(@NonNull BoardView boardView,
                               @NonNull IGameAction action,
                               @Nullable Runnable onAnimationEnd) {
        switch (action.getActionType()) {
            case CharacterAction:
                CharacterActionAnimator.animate(boardView, (CharacterAction) action, onAnimationEnd);
                break;
            case Recruitment:
                RecruitmentActionAnimator.animate(boardView, (RecruitmentAction) action, onAnimationEnd);
                break;
            default: throw new IllegalStateException("Action animation not handled for: " + action.getActionType());
        }
    }
}