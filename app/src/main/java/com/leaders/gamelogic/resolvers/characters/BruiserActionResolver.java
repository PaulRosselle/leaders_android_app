package com.leaders.gamelogic.resolvers.characters;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.gamelogic.actions.CharacterActionTarget;
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
 * Resolves the Bruiser's active ability.
 *
 * <p>The Bruiser moves onto the cell occupied by an adjacent enemy and pushes
 * that enemy onto one of the three cells opposite to the Bruiser's origin.</p>
 *
 * <p>The ability requires the player to first select an adjacent enemy, then
 * select a valid destination for the pushed enemy.</p>
 */
public final class BruiserActionResolver extends CharacterActionResolver {

    public BruiserActionResolver(@NonNull Game game, @NonNull GameHistory gameHistory, @NonNull Character character) {
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
                    throw new IllegalArgumentException("Invalid result type for a Bruiser ability activation");
                }
                return null;
            } else {
                return buildPushDestinationInteraction(builder, firstResult);
            }
        }

        throw new IllegalArgumentException("Invalid Bruiser action builder");
    }

    @Override
    @Nullable
    public InteractionFeedback getNextFeedback(@NonNull CharacterActionBuilder builder) {
        if (!ResolverUtils.isFeedbackExpected(builder)) {
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

        if (!isAbilityTargetResult(firstResult) || !isAbilityDestinationResult(destinationResult)) {
            throw new IllegalArgumentException("Invalid result types for a Bruiser ability activation");
        }

        Position targetOrigin = ResolverUtils.getChosenPosition(firstResult);
        Position destination = ResolverUtils.getChosenPosition(destinationResult);
        Character target = getCharacterAt(targetOrigin);

        return ResolverUtils.createMotionFeedback(CharacterMotionType.Push,
                new CharacterActionTarget(character, characterPos, targetOrigin),
                new CharacterActionTarget(target, targetOrigin, destination));
    }

    @NonNull
    private InteractionRequest buildInitialInteraction(@NonNull CharacterActionBuilder builder) {
        List<InteractionTarget> legalTargets = new ArrayList<>();

        ResolverUtils.addTargets(legalTargets, TargetCategory.MovementDestination, getNormalMovementValidDestinations(builder));

        if (CharacterAbilityQuery.canUseActiveAbility(game, gameHistory, character)) {
            for (Position enemyPosition : getAdjacentEnemyPositions()) {
                if (!getValidPushDestinations(builder, enemyPosition, true).isEmpty()) {
                    legalTargets.add(new InteractionTarget(TargetCategory.ActiveAbilityTargetPosition, enemyPosition));
                }
            }
        }

        return ResolverUtils.createPositionRequest(new InteractionContext(character), legalTargets);
    }

    @NonNull
    private InteractionRequest buildPushDestinationInteraction(@NonNull CharacterActionBuilder builder,
                                                               @NonNull InteractionResult result) {
        if (!isAbilityTargetResult(result)) {
            throw new IllegalArgumentException("A Bruiser ability target must be selected through a PositionChosen result");
        }

        Position targetPos = ResolverUtils.getChosenPosition(result);

        List<InteractionTarget> legalTargets = new ArrayList<>();

        ResolverUtils.addTargets(legalTargets, TargetCategory.ActiveAbilityDestination,
                getValidPushDestinations(builder, targetPos, false));

        Character targetedEnemy = getCharacterAt(targetPos);
        return ResolverUtils.createPositionRequest(new InteractionContext(character, targetedEnemy), legalTargets);
    }

    /**
     * Returns the positions of all enemies adjacent to the Bruiser.
     *
     * <p>Only enemy characters are considered as valid targets for the active
     * ability.</p>
     */
    @NonNull
    private List<Position> getAdjacentEnemyPositions() {
        List<Cell> enemyCells = BoardQuery.getAdjacentCells(game.getBoard(), characterPos, cell -> {
            Character target = cell.getCharacter();
            return target != null && target.getTeamColor() != character.getTeamColor() &&
                    CharacterAbilityQuery.canBeMovedByEnemyAbilities(game, target);
        });
        return ResolverUtils.getPositions(enemyCells);
    }

    /**
     * Returns the valid destinations to which the selected enemy can be pushed.
     *
     * <p>The Bruiser can push the target onto any of the three cells opposite
     * to the Bruiser relative to the target's current position. The cell
     * corresponding to the Bruiser's current position is therefore excluded.</p>
     *
     * <p>Each candidate destination is validated by building the corresponding
     * temporary action and passing it through the standard action validation.</p>
     *
     * @param builder current action builder
     * @param targetPos current position of the enemy targeted by the ability
     * @param exitAfterFirstValidDest whether the search can stop after finding
     *                                the first valid destination
     */
    @NonNull
    private List<Position> getValidPushDestinations(@NonNull CharacterActionBuilder builder,
                                                    @NonNull Position targetPos,
                                                    boolean exitAfterFirstValidDest) {
        Character target = getCharacterAt(targetPos);

        Direction straightPushDirection = findStraightPushDirection(targetPos);

        List<Direction> pushDirections = List.of(
                straightPushDirection.getNext(false),
                straightPushDirection,
                straightPushDirection.getNext(true)
        );

        // If the target result is missing, we add it before entering the destination loop
        CharacterActionBuilder targetBuilder = withAbilityTargetSelected(builder, targetPos);

        List<Position> destinations = new ArrayList<>();
        for (Direction direction : pushDirections) {
            Cell pushCell = BoardQuery.findAdjacentCell(game.getBoard(), targetPos, direction);
            if (pushCell != null && pushCell.getCharacter() == null) {
                Position pushDestPos = pushCell.getPosition();

                InteractionResult pushResult = ResolverUtils.createPositionResult(new InteractionContext(character, target),
                        TargetCategory.ActiveAbilityDestination, pushDestPos);
                InteractionFeedback pushFeedback = ResolverUtils.createMotionFeedback(CharacterMotionType.Push,
                        new CharacterActionTarget(character, characterPos, targetPos),
                        new CharacterActionTarget(target, targetPos, pushDestPos));

                if (isValidWith(targetBuilder, pushResult, pushFeedback)) {
                    destinations.add(pushDestPos);

                    if (exitAfterFirstValidDest) {
                        return destinations;
                    }
                }
            }
        }

        return destinations;
    }

    @NonNull
    private Direction findStraightPushDirection(@NonNull Position targetPos) {
        for (Direction direction : Direction.values()) {
            if (targetPos.equals(characterPos.adjacent(direction))) {
                return direction;
            }
        }

        throw new IllegalArgumentException("Target position is not adjacent to the Bruiser");
    }
}
