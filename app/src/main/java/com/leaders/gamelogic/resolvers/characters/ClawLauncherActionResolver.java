package com.leaders.gamelogic.resolvers.characters;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.gamelogic.actions.CharacterActionMotion;
import com.leaders.gamelogic.actions.CharacterActionTarget;
import com.leaders.gamelogic.entities.Cell;
import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.entities.Game;
import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.gamelogic.entities.Position;
import com.leaders.gamelogic.enums.CharacterMotionType;
import com.leaders.gamelogic.enums.Direction;
import com.leaders.gamelogic.interactions.CharacterActionBuilder;
import com.leaders.gamelogic.interactions.InteractionFeedback;
import com.leaders.gamelogic.interactions.InteractionRequest;
import com.leaders.gamelogic.interactions.InteractionResult;
import com.leaders.gamelogic.interactions.InteractionResultType;
import com.leaders.gamelogic.interactions.InteractionTarget;
import com.leaders.gamelogic.interactions.InteractionType;
import com.leaders.gamelogic.interactions.TargetCategory;
import com.leaders.gamelogic.queries.BoardQuery;
import com.leaders.gamelogic.resolvers.CharacterActionResolver;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Resolves the Claw Launcher's active ability.
 *
 * <p>The Claw Launcher can target a visible character in a straight line
 * and either move onto the target's position or pull the target onto
 * its own position.</p>
 *
 * <p>The choice between these two effects is encoded in the target
 * category selected by the player. The ability therefore requires
 * only a single interaction.</p>
 */
public final class ClawLauncherActionResolver extends CharacterActionResolver {

    public ClawLauncherActionResolver(@NonNull Game game, @NonNull GameHistory gameHistory, @NonNull Character character) {
        super(game, gameHistory, character);
    }

    @Override
    @Nullable
    public InteractionRequest getNextInteraction(@NonNull CharacterActionBuilder builder) {
        // The Claw Launcher only requires a single interaction.
        if (!builder.getInteractionResults().isEmpty()) {
            return null;
        }

        List<InteractionTarget> legalTargets = new ArrayList<>();

        // Normal movement has priority over the Claw Launcher's active ability effect.
        List<Position> movementDestinations = getNormalMovementValidDestinations(builder);
        for (Position destination : movementDestinations) {
            legalTargets.add(new InteractionTarget(TargetCategory.MovementDestination, destination));
        }

        for (Position targetPos : getClawLauncherValidTargetPositions(builder)) {
            // The claw launcher can pull itself to any valid target
            Position clawLauncherDestPos = getAbilityPullDestination(targetPos, characterPos);
            if (!movementDestinations.contains(clawLauncherDestPos) &&
                    isValidPull(builder, TargetCategory.ActiveAbilityDestination, clawLauncherDestPos)) {
                legalTargets.add(new InteractionTarget(TargetCategory.ActiveAbilityDestination, clawLauncherDestPos));
            }

            legalTargets.add(new InteractionTarget(TargetCategory.ActiveAbilityTargetPosition, targetPos));
        }

        return new InteractionRequest(
                InteractionType.PositionExpected,
                legalTargets,
                List.of(InteractionResultType.PositionChosen, InteractionResultType.CancelAction)
        );
    }

    @Override
    @Nullable
    public InteractionFeedback getNextFeedback(@NonNull CharacterActionBuilder builder) {
        if (builder.getInteractionResults().isEmpty() ||
                !builder.getInteractionFeedbacks().isEmpty()) {
            return null;
        }

        InteractionResult result = builder.getInteractionResults().get(0);

        if (result.getResultType() == InteractionResultType.CancelAction) {
            return null;
        }

        if (isNormalMovementResult(result)) {
            return super.getNextFeedback(builder);
        }

        if (isClawLauncherTargetResult(result) ||
                isClawLauncherDestinationResult(result)) {
            return buildClawLauncherFeedback(result);
        }

        throw new IllegalArgumentException(
                "Invalid Claw Launcher interaction type " + result.getResultType());
    }

    /**
     * Returns all visible characters that can be targeted by the Claw Launcher.
     *
     * <p>Only the first character encountered in each direction is visible
     * and can therefore be targeted.</p>
     */
    @NonNull
    private List<Position> getClawLauncherValidTargetPositions(@NonNull CharacterActionBuilder builder) {
        List<Position> targets = new ArrayList<>();

        for (Direction direction : Direction.values()) {
            Cell targetCell = BoardQuery.findVisibleCharacterCell(game.getBoard(), characterPos,
                    direction, null, null);

            if (targetCell != null) {
                Position targetPos = targetCell.getPosition();
                if (isValidPull(builder, TargetCategory.ActiveAbilityTargetPosition, targetPos)) {
                    targets.add(targetPos);
                }
            }
        }

        return targets;
    }

    private boolean isValidPull(@NonNull CharacterActionBuilder builder,
                                @NonNull TargetCategory targetCategory,
                                @NonNull Position targetPos) {
        CharacterActionBuilder pullBuilder = new CharacterActionBuilder(builder);
        InteractionResult pullResult = new InteractionResult(
                InteractionResultType.PositionChosen,
                new InteractionTarget(targetCategory, targetPos)
        );

        pullBuilder.addResult(pullResult);
        pullBuilder.addFeedback(buildClawLauncherFeedback(pullResult));

        return isActionValid(buildAction(pullBuilder));
    }

    @NonNull
    private InteractionFeedback buildClawLauncherFeedback(@NonNull InteractionResult result) {
        // This can be either a target pos or the claw launcher pull destination
        Position targetPos = Objects.requireNonNull(
                Objects.requireNonNull(
                        result.getChosenTarget(),
                        "Claw Launcher interaction result invalid: no data"
                ).getChosenPosition(),
                "Claw Launcher interaction result invalid: no target position"
        );

        if (isClawLauncherTargetResult(result)) {
            Character target = Objects.requireNonNull(
                    game.getBoard().getCell(targetPos).getCharacter(),
                    "Claw Launcher target position should contain a character"
            );

            return new InteractionFeedback(new CharacterActionMotion(
                    CharacterMotionType.Move,
                    List.of(new CharacterActionTarget(
                            target, targetPos, getAbilityPullDestination(characterPos, targetPos)))
            ));
        }

        if (isClawLauncherDestinationResult(result)) {
            return new InteractionFeedback(new CharacterActionMotion(
                    CharacterMotionType.Move,
                    List.of(new CharacterActionTarget(
                            character, characterPos, targetPos))
            ));
        }

        throw new IllegalArgumentException("Invalid target category for a Claw Launcher ability");
    }

    private boolean isClawLauncherTargetResult(@NonNull InteractionResult result) {
        return result.getResultType() == InteractionResultType.PositionChosen &&
                result.getChosenTarget() != null &&
                result.getChosenTarget().getCategory() ==
                        TargetCategory.ActiveAbilityTargetPosition;
    }

    private boolean isClawLauncherDestinationResult(@NonNull InteractionResult result) {
        return result.getResultType() == InteractionResultType.PositionChosen &&
                result.getChosenTarget() != null &&
                result.getChosenTarget().getCategory() ==
                        TargetCategory.ActiveAbilityDestination;
    }

    private int getNormalizedPullVector(int startCoordinate, int endCoordinate) {
        int pullVector = startCoordinate - endCoordinate;
        if (pullVector != 0) {
            pullVector = pullVector > 0 ? 1 : -1;
        }
        return pullVector;
    }

    @NonNull
    private Position getAbilityPullDestination(@NonNull Position anchorPos, @NonNull Position pulledTargetPos) {
        int q = getNormalizedPullVector(pulledTargetPos.getQ(), anchorPos.getQ());
        int r = getNormalizedPullVector(pulledTargetPos.getR(), anchorPos.getR());
        int s = getNormalizedPullVector(pulledTargetPos.getS(), anchorPos.getS());

        for (Direction direction : Direction.values()) {
            if (direction.getQ() == q && direction.getR() == r && direction.getS() == s) {
                Position adjacentPos = anchorPos.adjacent(direction);
                if (adjacentPos != null) {
                    return adjacentPos;
                }
            }
        }

        throw new IllegalArgumentException("Invalid pull target : no destination found");
    }
}