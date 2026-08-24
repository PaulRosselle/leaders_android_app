package com.leaders.puzzlelogic.utilities.solver;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.actions.CharacterAction;
import com.leaders.gamelogic.actions.CharacterActionMotion;
import com.leaders.gamelogic.actions.CharacterActionTarget;

import java.util.List;
import java.util.Objects;

public final class SolutionComparatorUtils {
    public enum SolutionCompareValue {
        NotSimilar,
        StructurallyEqual,
        FirstIsBetter,
        SecondIsBetter
    }

    private SolutionComparatorUtils(){
        throw new AssertionError("Cannot instantiate utility class");
    }

    /**
     * Compares two solutions based on the character actions they contain.
     * <p>
     * A solution is considered better when all of its actions are present in the
     * other solution and the other solution contains additional actions.
     * Solutions of equal size are structurally equal when all their actions match.
     *
     * @param first the first solution to compare
     * @param second the second solution to compare
     * @return the structural comparison result between the two solutions
     */
    public static SolutionCompareValue compareSolutions(@NonNull List<CharacterAction> first,
                                                        @NonNull List<CharacterAction> second) {
        boolean firstIsLonger = first.size() > second.size();
        
        List<CharacterAction> longerSolution = firstIsLonger ? first : second;
        List<CharacterAction> shorterSolution = firstIsLonger ? second : first;

        // We iterate through the longer solution to count every action in common
        int actionsInCommonCount = 0;
        for (CharacterAction firstSolutionAction : longerSolution) {
            for (CharacterAction secondSolutionAction : shorterSolution) {
                if (areActionsSimilar(firstSolutionAction, secondSolutionAction)) {
                    actionsInCommonCount++;
                }
            }
        }

        // All character actions in common and same length -> identical actions
        if (actionsInCommonCount == first.size() && second.size() == first.size()) {
            return SolutionCompareValue.StructurallyEqual;
        }
        // All actions in S1 are within S2 and S2 has superfluous actions -> S1 is better
        if (actionsInCommonCount == first.size() && second.size() > first.size()) {
            return SolutionCompareValue.FirstIsBetter;
        }
        // All actions in S2 are within S1 and S1 has superfluous actions -> S2 is better
        if (actionsInCommonCount == second.size() && first.size() > second.size()) {
            return SolutionCompareValue.SecondIsBetter;
        }

        // Solutions aren't similar
        return SolutionCompareValue.NotSimilar;
    }

    /**
     * Determines whether two character actions are structurally equivalent.
     * <p>
     * Actions are considered similar when they have the same source character,
     * motion types, target characters, and target positions in the same order.
     *
     * @param first the first action to compare
     * @param second the second action to compare
     * @return {@code true} if the actions are similar; {@code false} otherwise
     */
    private static boolean areActionsSimilar(@NonNull CharacterAction first,
                                             @NonNull CharacterAction second) {
        if (!first.getSrcCharacter().getId().equals(second.getSrcCharacter().getId())) {
            return false;
        }

        if (first.getMotions().size() != second.getMotions().size()) {
            return false;
        }

        for (int i = 0; i < first.getMotions().size(); i++) {
            CharacterActionMotion firstMotion = first.getMotions().get(i);
            CharacterActionMotion secondMotion = second.getMotions().get(i);

            if (firstMotion.getMotionType() != secondMotion.getMotionType()) {
                return false;
            }

            if (firstMotion.getTargets().size() != secondMotion.getTargets().size()) {
                return false;
            }

            for (int j = 0; j < firstMotion.getTargets().size(); j++) {
                CharacterActionTarget firstTarget = firstMotion.getTargets().get(j);
                CharacterActionTarget secondTarget = secondMotion.getTargets().get(j);

                if (!firstTarget.getCharacter().getId().equals(secondTarget.getCharacter().getId())) {
                    return false;
                }

                if (!Objects.equals(firstTarget.getOriginPos(), secondTarget.getOriginPos())) {
                    return false;
                }

                if (!Objects.equals(firstTarget.getDestPos(), secondTarget.getDestPos())) {
                    return false;
                }
            }
        }

        return true;
    }
}
