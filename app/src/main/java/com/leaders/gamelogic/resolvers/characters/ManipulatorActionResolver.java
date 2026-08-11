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
import com.leaders.gamelogic.interactions.InteractionContext;
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

        InteractionResult firstResult = builder.getResults().get(0);
        if (firstResult.getResultType() == InteractionResultType.CancelAction) {
            return null;
        }

        if (isNormalMovementResult(firstResult)) {
            return super.getNextInteraction(builder);
        }

        if (isManipulatorTargetResult(firstResult)) {
            return buildTargetDestinationInteraction(builder, firstResult);
        }

        throw new IllegalArgumentException("Invalid Manipulator action builder");
    }

    @Override
    @Nullable
    public InteractionFeedback getNextFeedback(@NonNull CharacterActionBuilder builder) {
        if (!builder.getFeedbacks().isEmpty()) {
            return null;
        }

        if (builder.getResults().isEmpty()) {
            return null;
        }

        InteractionResult firstResult = builder.getResults().get(0);

        if (firstResult.getResultType() == InteractionResultType.CancelAction) {
            return null;
        }

        if (isNormalMovementResult(firstResult)) {
            return super.getNextFeedback(builder);
        }

        if (builder.getResults().size() < 2) {
            return null;
        }

        InteractionResult destinationResult = builder.getResults().get(1);

        if (!isManipulatorTargetResult(firstResult) ||
                !isManipulatorDestinationResult(destinationResult)) {
            throw new IllegalArgumentException("Invalid result types for a Manipulator ability activation");
        }

        Position targetOrigin = Objects.requireNonNull(
                Objects.requireNonNull(firstResult.getChosenTarget(),
                                "Manipulator target interaction result invalid: no data")
                        .getChosenPosition(),
                "Manipulator target interaction result invalid: no target position");

        Position targetDestination = Objects.requireNonNull(
                Objects.requireNonNull(destinationResult.getChosenTarget(),
                                "Manipulator destination interaction result invalid: no data")
                        .getChosenPosition(),
                "Manipulator destination interaction result invalid: no destination");

        Character target = Objects.requireNonNull(
                game.getBoard().getCell(targetOrigin).getCharacter(),
                "Manipulator target position should contain a character");

        return InteractionFeedback.createForCharacterAction(List.of(new CharacterActionMotion(
                CharacterMotionType.Move,
                List.of(new CharacterActionTarget(target, targetOrigin, targetDestination))
        )));
    }

    @NonNull
    private InteractionRequest buildInitialInteraction(@NonNull CharacterActionBuilder builder) {
        List<InteractionTarget> legalTargets = new ArrayList<>();

        for (Position destination : getNormalMovementValidDestinations(builder)) {
            legalTargets.add(new InteractionTarget(TargetCategory.MovementDestination, destination));
        }

        for (Position enemyPosition : getManipulatorTargetPositions()) {
            if (!getValidTargetDestinations(builder, enemyPosition, true).isEmpty()) {
                legalTargets.add(new InteractionTarget(TargetCategory.ActiveAbilityTargetPosition, enemyPosition));
            }
        }

        return new InteractionRequest(
                InteractionType.PositionExpected,
                new InteractionContext(character),
                legalTargets,
                List.of(InteractionResultType.PositionChosen, InteractionResultType.CancelAction)
        );
    }

    @NonNull
    private InteractionRequest buildTargetDestinationInteraction(@NonNull CharacterActionBuilder builder,
                                                                 @NonNull InteractionResult result) {
        if (!isManipulatorTargetResult(result)) {
            throw new IllegalArgumentException(
                    "A Manipulator target must be selected through a PositionChosen result");
        }

        Position targetPos = Objects.requireNonNull(Objects.requireNonNull(result.getChosenTarget(),
                        "Manipulator target interaction invalid : no data"
                ).getChosenPosition(),
                "Manipulator target interaction invalid : no target position");

        List<InteractionTarget> legalTargets = new ArrayList<>();

        for (Position destination : getValidTargetDestinations(builder, targetPos, false)) {
            legalTargets.add(new InteractionTarget(TargetCategory.ActiveAbilityDestination, destination));
        }

        Character targetedEnemy = Objects.requireNonNull(game.getBoard().getCell(targetPos).getCharacter(),
                "A Manipulator target position must contain an enemy");
        return new InteractionRequest(
                InteractionType.PositionExpected,
                new InteractionContext(targetedEnemy), // Here the request has for subjet the target
                legalTargets,
                List.of(InteractionResultType.PositionChosen, InteractionResultType.CancelAction)
        );
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
            // The Manipulator cannot target adjacent characters.
            Cell adjacentCell = BoardQuery.findAdjacentCell(game.getBoard(), characterPos, direction);
            if (adjacentCell != null && adjacentCell.getCharacter() == null) {
                Cell targetCell = BoardQuery.findVisibleCharacterCell(game.getBoard(), characterPos,
                        direction, character.getTeamColor().getOpposite(), null);
                if (targetCell != null) {
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
        Character target = Objects.requireNonNull(game.getBoard().getCell(targetPos).getCharacter(),
                "A cell targeted by the Manipulator's ability should always contain a character");

        List<Position> destinations = new ArrayList<>();


        CharacterActionBuilder targetBuilder = new CharacterActionBuilder(builder);
        if (targetBuilder.getResults().isEmpty()) {
            targetBuilder.addResult(new InteractionResult(
                    InteractionResultType.PositionChosen,
                    new InteractionContext(character),
                    new InteractionTarget(TargetCategory.ActiveAbilityTargetPosition, targetPos)
            ));
        }

        for (Cell destinationCell : BoardQuery.findEmptyCellsAround(game.getBoard(), targetPos, 1)) {
            Position destination = destinationCell.getPosition();

            CharacterActionBuilder destinationBuilder = new CharacterActionBuilder(targetBuilder);
            destinationBuilder.addResult(new InteractionResult(
                    InteractionResultType.PositionChosen,
                    new InteractionContext(target), // Here the result has for subjet the target
                    new InteractionTarget(TargetCategory.ActiveAbilityDestination, destination)
            ));

            destinationBuilder.addFeedback(InteractionFeedback.createForCharacterAction(
                    List.of(new CharacterActionMotion(
                            CharacterMotionType.Move,
                            List.of(new CharacterActionTarget(target, targetPos, destination))
                    ))
            ));

            if (isActionValid(buildAction(destinationBuilder))) {
                destinations.add(destination);

                if (exitAfterFirstValidDest) {
                    return destinations;
                }
            }
        }

        return destinations;
    }

    private boolean isManipulatorTargetResult(@NonNull InteractionResult result) {
        return result.getResultType() == InteractionResultType.PositionChosen &&
                result.getChosenTarget() != null &&
                result.getChosenTarget().getCategory() == TargetCategory.ActiveAbilityTargetPosition;
    }

    private boolean isManipulatorDestinationResult(@NonNull InteractionResult result) {
        return result.getResultType() == InteractionResultType.PositionChosen &&
                result.getChosenTarget() != null &&
                result.getChosenTarget().getCategory() == TargetCategory.ActiveAbilityDestination;
    }
}