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

import java.util.ArrayList;
import java.util.List;

import static com.leaders.gamelogic.resolvers.ResolverUtils.addTargets;
import static com.leaders.gamelogic.resolvers.ResolverUtils.createPositionRequest;
import static com.leaders.gamelogic.resolvers.ResolverUtils.createPositionResult;
import static com.leaders.gamelogic.resolvers.ResolverUtils.createSingleMotionFeedback;
import static com.leaders.gamelogic.resolvers.ResolverUtils.getChosenPosition;
import static com.leaders.gamelogic.resolvers.ResolverUtils.isFeedbackExpected;

/**
 * Resolves the Manipulator's active ability.
 *
 * <p>The Manipulator moves an enemy visible in a straight line and not adjacent
 * by one cell. The enemy can be moved to any valid adjacent empty cell.</p>
 *
 * <p>The ability requires the player to first select the enemy to move, then
 * select its destination.</p>
 */
public final class ManipulatorActionResolver extends CharacterActionResolver {

    public ManipulatorActionResolver(@NonNull Game game, @NonNull GameHistory gameHistory,
                                     @NonNull Character character) {
        super(game, gameHistory, character);
    }

    @Override
    @Nullable
    public InteractionRequest getNextInteraction(@NonNull CharacterActionBuilder builder) {
        if (builder.getResults().isEmpty()) {
            return buildInitialInteraction(builder);
        }

        if (builder.isBuildCancelled()) {
            return null;
        }

        InteractionResult firstResult = builder.getResults().get(0);

        if (isNormalMovementResult(firstResult)) {
            return super.getNextInteraction(builder);
        }

        if (isAbilityTargetResult(firstResult)) {
            if (builder.getResults().size() > 1) {
                if (!isAbilityDestinationResult(builder.getResults().get(1))) {
                    throw new IllegalArgumentException("Invalid result type for a Manipulator ability activation");
                }
                return null;
            } else {
                return buildTargetDestinationInteraction(builder, firstResult);
            }
        }

        throw new IllegalArgumentException("Invalid Manipulator action builder");
    }

    @Override
    @Nullable
    public InteractionFeedback getNextFeedback(@NonNull CharacterActionBuilder builder) {
        if (!isFeedbackExpected(builder)) {
            return null;
        }

        InteractionResult firstResult = builder.getResults().get(0);

        if (isNormalMovementResult(firstResult)) {
            return super.getNextFeedback(builder);
        }

        if (builder.getResults().size() < 2) {
            return null;
        }

        InteractionResult destinationResult = builder.getResults().get(1);

        if (!isAbilityTargetResult(firstResult) ||
                !isAbilityDestinationResult(destinationResult)) {
            throw new IllegalArgumentException("Invalid result types for a Manipulator ability activation");
        }

        Position targetOrigin = getChosenPosition(firstResult);
        Position targetDestination = getChosenPosition(destinationResult);
        Character target = getCharacterAt(targetOrigin);

        return createSingleMotionFeedback(CharacterMotionType.Move, target, targetOrigin, targetDestination);
    }

    @NonNull
    private InteractionRequest buildInitialInteraction(@NonNull CharacterActionBuilder builder) {
        List<InteractionTarget> legalTargets = new ArrayList<>();

        addTargets(legalTargets, TargetCategory.MovementDestination, getNormalMovementValidDestinations(builder));

        if (CharacterAbilityQuery.canUseActiveAbility(game, character)) {
            for (Position enemyPosition : getManipulatorTargetPositions()) {
                if (!getValidTargetDestinations(builder, enemyPosition, true).isEmpty()) {
                    legalTargets.add(new InteractionTarget(TargetCategory.ActiveAbilityTargetPosition, enemyPosition));
                }
            }
        }

        return createPositionRequest(new InteractionContext(character), legalTargets);
    }

    @NonNull
    private InteractionRequest buildTargetDestinationInteraction(@NonNull CharacterActionBuilder builder,
                                                                 @NonNull InteractionResult result) {
        if (!isAbilityTargetResult(result)) {
            throw new IllegalArgumentException(
                    "A Manipulator target must be selected through a PositionChosen result");
        }

        Position targetPos = getChosenPosition(result);

        List<InteractionTarget> legalTargets = new ArrayList<>();

        addTargets(legalTargets, TargetCategory.ActiveAbilityDestination,
                getValidTargetDestinations(builder, targetPos, false));

        Character targetedEnemy = getCharacterAt(targetPos);
        return createPositionRequest(new InteractionContext(character, targetedEnemy), legalTargets);
    }

    /**
     * Returns the positions of all enemies visible to the Manipulator.
     *
     * <p>Only the first character encountered in each direction is considered.
     * Adjacent characters are excluded because the Manipulator can only target
     * non-adjacent enemies.</p>
     */
    @NonNull
    private List<Position> getManipulatorTargetPositions() {
        List<Position> targets = new ArrayList<>();

        for (Direction direction : Direction.values()) {
            Cell adjacentCell = BoardQuery.findAdjacentCell(game.getBoard(), characterPos, direction);
            if (adjacentCell != null && adjacentCell.getCharacter() == null) {
                Cell targetCell = BoardQuery.findVisibleCharacterCell(game.getBoard(), adjacentCell.getPosition(),
                        direction, character.getTeamColor().getOpposite(), null);
                if (targetCell != null &&
                        CharacterAbilityQuery.canBeMovedByEnemyAbilities(game, targetCell.getCharacter())) {
                    targets.add(targetCell.getPosition());
                }
            }
        }

        return targets;
    }

    /**
     * Returns the valid destinations for an enemy targeted by the Manipulator.
     *
     * <p>The target can be moved to any empty cell adjacent to its current
     * position. Each candidate is validated using the standard action
     * validation mechanism before being exposed to the player.</p>
     *
     * @param builder current action builder
     * @param targetPos current position of the targeted enemy
     * @param exitAfterFirstValidDest whether the search can stop after finding
     *                                the first valid destination
     */
    @NonNull
    private List<Position> getValidTargetDestinations(@NonNull CharacterActionBuilder builder,
                                                      @NonNull Position targetPos,
                                                      boolean exitAfterFirstValidDest) {
        Character target = getCharacterAt(targetPos);

        List<Position> destinations = new ArrayList<>();


        CharacterActionBuilder targetBuilder = withAbilityTargetSelected(builder, targetPos);

        for (Cell destinationCell : BoardQuery.findEmptyCellsAround(game.getBoard(), targetPos, 1)) {
            Position destination = destinationCell.getPosition();

            InteractionResult destinationResult = createPositionResult(new InteractionContext(character, target),
                    TargetCategory.ActiveAbilityDestination, destination);
            InteractionFeedback destinationFeedback = createSingleMotionFeedback(
                    CharacterMotionType.Move, target, targetPos, destination);

            if (isValidWith(targetBuilder, destinationResult, destinationFeedback)) {
                destinations.add(destination);

                if (exitAfterFirstValidDest) {
                    return destinations;
                }
            }
        }

        return destinations;
    }
}