package com.leaders.gamelogic.queries;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.actions.CharacterActionMotion;
import com.leaders.gamelogic.actions.CharacterActionTarget;
import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.entities.TransformedState;
import com.leaders.gamelogic.enums.CharacterMotionType;

import java.util.List;
import java.util.UUID;

public final class CharacterTransformUtils {
    private CharacterTransformUtils(){
        throw new AssertionError("Cannot instantiate utility class");
    }

    /**
     * Gets the motion types that represent character transformations.
     *
     * @return the list of transform and untransform motion types
     */
    @NonNull
    private static List<CharacterMotionType> getTransformMotionTypes() {
        return List.of(CharacterMotionType.Transform, CharacterMotionType.Untransform);
    }

    /**
     * Checks whether the given motion is a transform or untransform motion.
     *
     * @param motion the motion to check
     * @return {@code true} if the motion type represents a transformation, otherwise {@code false}
     */
    public static boolean isTransformMotion(@NonNull CharacterActionMotion motion) {
        return getTransformMotionTypes().contains(motion.getMotionType());
    }

    /**
     * Validates a transform motion and returns an error message if it is invalid.
     *
     * @param motion the motion to validate
     * @return an empty string if the motion is valid, or an error message otherwise
     */
    @NonNull
    private static String getTranformMotionError(@NonNull CharacterActionMotion motion) {
        if (!isTransformMotion(motion)) {
            return "Invalid transform motion type: " + motion.getMotionType();
        }

        List<CharacterActionTarget> targets = motion.getTargets();
        if (targets.size() != 2) {
            return "Invalid transform motion: exactly 2 targets expected";
        }

        Character beforeCharacter = targets.get(0).getCharacter();
        Character afterCharacter = targets.get(1).getCharacter();

        UUID characterId = beforeCharacter.getId();
        if (!characterId.equals(afterCharacter.getId())) {
            return "Invalid transform motion: a single character is required";
        }

        return "";
    }

    /**
     * Extracts the transformed state from a character transform motion.
     *
     * @param motion the transform motion to process
     * @return the transformed state containing the original and transformed characters
     * @throws IllegalStateException if the motion is invalid
     */
    @NonNull
    public static TransformedState getStateFromTranformMotion(@NonNull CharacterActionMotion motion) {
        String error = getTranformMotionError(motion);
        if (!error.isEmpty()) {
            throw new IllegalStateException(error);
        }

        List<CharacterActionTarget> targets = motion.getTargets();
        return new TransformedState(targets.get(0).getCharacter(), targets.get(1).getCharacter());
    }
}
