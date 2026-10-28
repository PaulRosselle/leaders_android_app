package com.leaders.puzzlelogic.utilities;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.entities.Position;
import com.leaders.gamelogic.interactions.InteractionTarget;

import java.util.List;
import java.util.Objects;

public final class BotUtils {
    private BotUtils(){
        throw new AssertionError("Cannot instantiate utility class");
    }

    /**
     * Returns the target closest to the reference position.
     *
     * @param targets the list of targets to search
     * @param refPosition the reference position
     * @return the target closest to the reference position
     * @throws IllegalArgumentException if the target list is empty
     */
    @NonNull
    public static InteractionTarget getClosestTarget(@NonNull List<InteractionTarget> targets,
                                                     @NonNull Position refPosition) {
        if (targets.isEmpty()) {
            throw new IllegalArgumentException("Cannot get the closest target from an empty target list");
        }

        if (targets.size() == 1) {
            return targets.get(0);
        }

        InteractionTarget closestTarget = targets.get(0);
        int closestDistance = getTargetPositionDistance(closestTarget, refPosition);
        for (int i = 1; i < targets.size(); i++) {
            final InteractionTarget target = targets.get(i);
            final int targetDistance = getTargetPositionDistance(target, refPosition);
            if (targetDistance < closestDistance) {
                closestTarget = target;
                closestDistance = targetDistance;
            }
        }

        return closestTarget;
    }

    /**
     * Returns the distance between a target's chosen position and a reference position.
     *
     * @param target the target whose position is used
     * @param refPosition the reference position
     * @return the distance between the two positions
     * @throws NullPointerException if the target has no chosen position
     */
    private static int getTargetPositionDistance(@NonNull InteractionTarget target,
                                                 @NonNull Position refPosition) {
        Position position = Objects.requireNonNull(
                target.getChosenPosition(),
                "Invalid target: not a position"
        );
        return position.distanceTo(refPosition);
    }
}
