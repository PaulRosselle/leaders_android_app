package com.leaders.gamelogic.resolvers;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.actions.CharacterActionMotion;
import com.leaders.gamelogic.actions.CharacterActionTarget;
import com.leaders.gamelogic.entities.Cell;
import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.entities.CharacterPath;
import com.leaders.gamelogic.entities.Position;
import com.leaders.gamelogic.enums.CharacterMotionType;
import com.leaders.gamelogic.interactions.CharacterActionBuilder;
import com.leaders.gamelogic.interactions.InteractionContext;
import com.leaders.gamelogic.interactions.InteractionFeedback;
import com.leaders.gamelogic.interactions.InteractionRequest;
import com.leaders.gamelogic.interactions.InteractionResult;
import com.leaders.gamelogic.interactions.InteractionResultType;
import com.leaders.gamelogic.interactions.InteractionTarget;
import com.leaders.gamelogic.interactions.InteractionType;
import com.leaders.gamelogic.interactions.TargetCategory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Stateless helpers shared by the action resolvers.
 *
 * <p>Everything that depends on a resolver's state (game, character, validation of a candidate
 * action) belongs to {@link CharacterActionResolver} instead.</p>
 *
 * <p>This class is public because resolvers living in sub-packages
 * (e.g. {@code resolvers.characters}) use it.</p>
 */
public final class ResolverUtils {
    private ResolverUtils() {
        throw new AssertionError("Cannot instantiate utility class");
    }

    /**
     * Checks whether the result represents a chosen position in the given target category.
     *
     * @param result the interaction result to check
     * @param category the expected target category
     * @return {@code true} if a position was chosen in the given category, otherwise {@code false}
     */
    public static boolean isPositionResult(@NonNull InteractionResult result, @NonNull TargetCategory category) {
        InteractionTarget target = result.getChosenTarget();
        return result.getResultType() == InteractionResultType.PositionChosen &&
                target != null && target.getCategory() == category;
    }

    /**
     * Returns the position chosen in the interaction result.
     *
     * @param result the interaction result containing the chosen position
     * @return the chosen position
     * @throws NullPointerException if the result has no target or the target has no chosen position
     */
    @NonNull
    public static Position getChosenPosition(@NonNull InteractionResult result) {
        InteractionTarget target = Objects.requireNonNull(
                result.getChosenTarget(),
                "No target in result: " + result
        );
        return Objects.requireNonNull(target.getChosenPosition(), "No position in result: " + result);
    }

    /**
     * Creates an interaction result for a chosen position.
     *
     * @param context the interaction context
     * @param category the category of the chosen position
     * @param position the chosen position
     * @return the interaction result containing the chosen position
     */
    @NonNull
    public static InteractionResult createPositionResult(@NonNull InteractionContext context,
                                                         @NonNull TargetCategory category,
                                                         @NonNull Position position) {
        return new InteractionResult(
                InteractionResultType.PositionChosen,
                context,
                new InteractionTarget(category, position)
        );
    }

    /**
     * Adds interaction targets for the given positions.
     *
     * @param targets the list to which the targets are added
     * @param category the category assigned to each target
     * @param positions the positions to convert into interaction targets
     */
    public static void addTargets(@NonNull List<InteractionTarget> targets,
                                  @NonNull TargetCategory category,
                                  @NonNull Collection<Position> positions) {
        for (Position position : positions) {
            targets.add(new InteractionTarget(category, position));
        }
    }

    /**
     * Returns the positions of the given cells.
     *
     * @param cells the cells whose positions are retrieved
     * @return the list of positions corresponding to the given cells
     */
    @NonNull
    public static List<Position> getPositions(@NonNull Collection<Cell> cells) {
        List<Position> positions = new ArrayList<>();
        for (Cell cell : cells) {
            positions.add(cell.getPosition());
        }
        return positions;
    }

    /**
     * Creates an interaction request expecting a position selection or an action cancellation.
     *
     * @param context the interaction context
     * @param targets the available position targets
     * @return the interaction request accepting position selection or action cancellation
     */
    @NonNull
    public static InteractionRequest createPositionRequest(@NonNull InteractionContext context,
                                                           @NonNull List<InteractionTarget> targets) {
        return new InteractionRequest(InteractionType.PositionExpected, context, targets,
                List.of(InteractionResultType.PositionChosen, InteractionResultType.CancelAction)
        );
    }


    /**
     * Checks whether feedback is expected from the given builder.
     *
     * @param builder the character action builder
     * @return {@code true} if the builder has results, no feedbacks and has not been cancelled,
     *         otherwise {@code false}
     */
    public static boolean isFeedbackExpected(@NonNull CharacterActionBuilder builder) {
        return !builder.getResults().isEmpty() &&
                builder.getFeedbacks().isEmpty() &&
                !builder.isBuildCancelled();
    }


    /**
     * Creates feedback for a character motion.
     *
     * @param type the type of character motion
     * @param targets the targets involved in the motion
     * @return the interaction feedback representing the character motion
     */
    @NonNull
    public static InteractionFeedback createMotionFeedback(@NonNull CharacterMotionType type,
                                                           @NonNull CharacterActionTarget... targets) {
        return InteractionFeedback.createForCharacterAction(List.of(
                new CharacterActionMotion(type, List.of(targets))
        ));
    }

    /**
     * Creates feedback for a motion involving a single character.
     *
     * @param type the type of character motion
     * @param character the character performing the motion
     * @param from the starting position
     * @param to the destination position
     * @return the interaction feedback representing the character motion
     */
    @NonNull
    public static InteractionFeedback createSingleMotionFeedback(@NonNull CharacterMotionType type,
                                                                 @NonNull Character character,
                                                                 @NonNull Position from,
                                                                 @NonNull Position to) {
        return createMotionFeedback(type, new CharacterActionTarget(character, from, to));
    }

    /**
     * Creates feedback for the motions along a character path.
     *
     * @param character the character performing the motions
     * @param path the path to follow
     * @param motionType the type of motion
     * @return the interaction feedback representing the path motions
     * @throws IllegalArgumentException if the path contains more than two steps
     */
    @NonNull
    public static InteractionFeedback createPathFeedback(@NonNull Character character,
                                                         @NonNull CharacterPath path,
                                                         @NonNull CharacterMotionType motionType) {
        if (path.getStepsCount() > 2) {
            throw new IllegalArgumentException("Invalid " + motionType + " path: path should not exceed two steps");
        }

        List<CharacterActionMotion> motions = new ArrayList<>();
        Position previousPos = path.getStart();
        for (Position position : path.getPositions()) {
            if (!position.equals(path.getStart())) {
                motions.add(new CharacterActionMotion(motionType,
                        List.of(new CharacterActionTarget(character, previousPos, position)))
                );
                previousPos = position;
            }
        }

        return InteractionFeedback.createForCharacterAction(motions);
    }
}
