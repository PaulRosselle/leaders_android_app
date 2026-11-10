package com.leaders.gamelogic.resolvers.characters;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.gamelogic.entities.Cell;
import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.entities.Game;
import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.gamelogic.entities.Position;
import com.leaders.gamelogic.enums.CharacterMotionType;
import com.leaders.gamelogic.enums.Direction;
import com.leaders.gamelogic.interactions.CharacterActionBuilder;
import com.leaders.gamelogic.interactions.InteractionContext;
import com.leaders.gamelogic.interactions.InteractionFeedback;
import com.leaders.gamelogic.interactions.InteractionRequest;
import com.leaders.gamelogic.interactions.InteractionResult;
import com.leaders.gamelogic.interactions.InteractionTarget;
import com.leaders.gamelogic.interactions.TargetCategory;
import com.leaders.gamelogic.queries.BoardQuery;
import com.leaders.gamelogic.queries.CharacterAbilityQuery;
import com.leaders.gamelogic.resolvers.CharacterActionResolver;
import com.leaders.gamelogic.resolvers.ResolverUtils;

import java.util.ArrayList;
import java.util.List;

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
        if (!builder.getResults().isEmpty()) {
            return null;
        }

        List<InteractionTarget> legalTargets = new ArrayList<>();

        // Normal movement has priority over the Claw Launcher's active ability effect.
        List<Position> movementDestinations = getNormalMovementValidDestinations(builder);
        ResolverUtils.addTargets(legalTargets, TargetCategory.MovementDestination, movementDestinations);

        if (CharacterAbilityQuery.canUseActiveAbility(game, gameHistory, character)) {
            for (Position targetPos : getClawLauncherValidTargetPositions()) {
                // The claw launcher can pull itself to any valid target
                Position clawLauncherDestPos = getAbilityPullDestination(targetPos, characterPos);
                if (!movementDestinations.contains(clawLauncherDestPos) &&
                        isValidPull(builder, TargetCategory.ActiveAbilityDestination, clawLauncherDestPos)) {
                    legalTargets.add(new InteractionTarget(TargetCategory.ActiveAbilityDestination, clawLauncherDestPos));
                }

                // The target can be pulled next to the claw launcher
                Character target = game.getBoard().getCell(targetPos).getCharacter();
                if ((target.getTeamColor() == character.getTeamColor() ||
                        CharacterAbilityQuery.canBeMovedByEnemyAbilities(game, target)) &&
                        isValidPull(builder, TargetCategory.ActiveAbilityTargetPosition, targetPos)) {
                    legalTargets.add(new InteractionTarget(TargetCategory.ActiveAbilityTargetPosition, targetPos));
                }
            }
        }

        return ResolverUtils.createPositionRequest(new InteractionContext(character), legalTargets);
    }

    @Override
    @Nullable
    public InteractionFeedback getNextFeedback(@NonNull CharacterActionBuilder builder) {
        if (!ResolverUtils.isFeedbackExpected(builder)) {
            return null;
        }

        InteractionResult result = builder.getResults().get(0);

        if (isNormalMovementResult(result)) {
            return super.getNextFeedback(builder);
        }

        if (isAbilityTargetResult(result) ||
                isAbilityDestinationResult(result)) {
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
    private List<Position> getClawLauncherValidTargetPositions() {
        List<Position> targets = new ArrayList<>();

        for (Direction direction : Direction.values()) {
            Cell adjacentCell = BoardQuery.findAdjacentCell(game.getBoard(), characterPos, direction);
            if (adjacentCell != null && adjacentCell.getCharacter() == null) {
                Cell targetCell = BoardQuery.findVisibleCharacterCell(game.getBoard(), adjacentCell.getPosition(),
                        direction, null, null);

                if (targetCell != null) {
                    targets.add(targetCell.getPosition());
                }
            }
        }

        return targets;
    }

    private boolean isValidPull(@NonNull CharacterActionBuilder builder,
                                @NonNull TargetCategory targetCategory,
                                @NonNull Position targetPos) {
        InteractionResult pullResult = ResolverUtils.createPositionResult(
                new InteractionContext(character), targetCategory, targetPos);

        return isValidWith(builder, pullResult, buildClawLauncherFeedback(pullResult));
    }

    @NonNull
    private InteractionFeedback buildClawLauncherFeedback(@NonNull InteractionResult result) {
        // This can be either a target pos or the claw launcher pull destination
        Position targetPos = ResolverUtils.getChosenPosition(result);

        if (isAbilityTargetResult(result)) {
            Character target = getCharacterAt(targetPos);

            return ResolverUtils.createSingleMotionFeedback(CharacterMotionType.Move,
                    target, targetPos, getAbilityPullDestination(characterPos, targetPos));
        }

        if (isAbilityDestinationResult(result)) {
            return ResolverUtils.createSingleMotionFeedback(CharacterMotionType.Move, character, characterPos, targetPos);
        }

        throw new IllegalArgumentException("Invalid target category for a Claw Launcher ability");
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