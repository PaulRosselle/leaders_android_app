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

    // TODO - javadoc
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

    // TODO - javadoc
    @NonNull
    public static InteractionTarget getFurthestTarget(@NonNull List<InteractionTarget> targets,
                                                      @NonNull Position refPosition) {
        if (targets.isEmpty()) {
            throw new IllegalArgumentException("Cannot get the furthest target from an empty target list");
        }
        if (targets.size() == 1) {
            return targets.get(0);
        }

        InteractionTarget furthestTarget = targets.get(0);
        int furthestDistance = getTargetPositionDistance(furthestTarget, refPosition);
        for (int i = 1; i < targets.size(); i++) {
            final InteractionTarget target = targets.get(i);
            final int targetDistance = getTargetPositionDistance(target, refPosition);
            if (targetDistance > furthestDistance) {
                furthestTarget = target;
                furthestDistance = targetDistance;
            }
        }

        return furthestTarget;
    }

    // TODO - javadoc
    private static int getTargetPositionDistance(@NonNull InteractionTarget target,
                                                 @NonNull Position refPosition) {
        Position position = Objects.requireNonNull(
                target.getChosenPosition(),
                "Invalid target: not a position"
        );
        return position.distanceTo(refPosition);
    }
}
