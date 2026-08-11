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

/** Resolves the Brewmaster's active ability.
 * <p>The Brewmaster can move an adjacent allied character by one cell.
 * The ability requires the player to first select an adjacent ally, then
 * select an empty cell adjacent to that ally.</p>
 */
public final class BrewmasterActionResolver extends CharacterActionResolver {

    public BrewmasterActionResolver(@NonNull Game game, @NonNull GameHistory gameHistory, @NonNull Character character) {
        super(game, gameHistory, character);
    }

    @Override
    @Nullable
    public InteractionRequest getNextInteraction(@NonNull CharacterActionBuilder builder) {
        // For the first interaction, both normal movement and ability activation are possible
         if (builder.getResults().isEmpty()) {
            return buildInitialInteraction(builder);
        }

        InteractionResult firstResult = builder.getResults().get(0);
        if (firstResult.getResultType() == InteractionResultType.CancelAction) {
            return null;
        }

        // If the first interaction was a normal movement
        if (isNormalMovementResult(firstResult)) {
            return super.getNextInteraction(builder);
        }

        // Otherwise it should be an ability activation, in which case the next (and last)
        // interaction is to select a target destination
        if (isBrewmasterTargetResult(firstResult)) {
            return buildTargetDestinationInteraction(builder, firstResult);
        }

        throw new IllegalArgumentException("Invalid Brewmaster action builder");
    }

    @Override
    @Nullable
    public InteractionFeedback getNextFeedback(@NonNull CharacterActionBuilder builder) {
        // Brewmaster actions generate a single feedback.
        if (!builder.getFeedbacks().isEmpty()) {
            return null;
        }

        InteractionResult firstResult = builder.getResults().get(0);
        if (firstResult.getResultType() == InteractionResultType.CancelAction) {
            return null;
        }

        // A normal movement only requires the first interaction, so its feedback
        // can be generated directly by the default resolver.
        if (isNormalMovementResult(firstResult)) {
            return super.getNextFeedback(builder);
        }

        // An ability activation requires two interactions: the selected ally
        // and the destination to which that ally should be moved.
        if (builder.getResults().size() < 2) {
            return null;
        }

        InteractionResult targetDestResult = builder.getResults().get(1);

        if (!isBrewmasterTargetResult(firstResult) ||
                !isBrewmasterTargetDestinationResult(targetDestResult)) {
            throw new IllegalArgumentException("Invalid result types for a Brewmaster ability activation");
        }

        Position targetOrigin = Objects.requireNonNull(
                Objects.requireNonNull(firstResult.getChosenTarget(),
                        "Brewmaster target interaction result invalid : no data")
                        .getChosenPosition(),
                "Brewmaster target interaction result invalid : no target position");
        Character target = game.getBoard().getCell(targetOrigin).getCharacter();
        Position targetDest = Objects.requireNonNull(
                Objects.requireNonNull(targetDestResult.getChosenTarget(),
                                "Brewmaster target destination interaction result invalid : no data")
                        .getChosenPosition(),
                "Brewmaster target destination interaction result invalid : no destination");

        return InteractionFeedback.createForCharacterAction(List.of(new CharacterActionMotion(
                CharacterMotionType.Move,
                List.of(new CharacterActionTarget(target, targetOrigin, targetDest))
        )));
    }

    /** * Builds the first interaction of the action.
     * <p>The player can either choose a normal movement destination for the
     * Brewmaster or select an adjacent allied character as the target of its active ability.
     * An ally is only offered as an ability target if it has at least one valid destination.</p>*/
    @NonNull
    private InteractionRequest buildInitialInteraction(@NonNull CharacterActionBuilder builder) {
         List<InteractionTarget> legalTargets = new ArrayList<>();

        // Normal movement and Brewmaster ability target cannot overlap:
        // a normal move requires an adjacent empty cell, while the brewmaster can only target adjacent allies.
        // For this reason, we can add both set of positions without further treatment to avoid duplicates
        for (Position destination : getNormalMovementValidDestinations(builder)) {
            legalTargets.add(new InteractionTarget(TargetCategory.MovementDestination, destination));
        }
        for (Position adjacentAllyPos : getAdjacentAllyPositions()) {
            if (!getValidTargetDestinations(builder, adjacentAllyPos, true).isEmpty()) {
                legalTargets.add(new InteractionTarget(TargetCategory.ActiveAbilityTargetPosition, adjacentAllyPos));
            }
        }

        return new InteractionRequest(
                InteractionType.PositionExpected,
                new InteractionContext(character),
                legalTargets,
                List.of(InteractionResultType.PositionChosen, InteractionResultType.CancelAction)
        );
    }

    /** * Builds the second interaction required by the Brewmaster's active ability.
     * <p>The first interaction identifies the allied character to move.
     * This interaction then offers every valid empty cell adjacent to that character.</p>*/
    @NonNull
    private InteractionRequest buildTargetDestinationInteraction(@NonNull CharacterActionBuilder builder,
                                                                 @NonNull InteractionResult result) {
        if (!isBrewmasterTargetResult(result)) {
            throw new IllegalArgumentException("A target position must be transmitted through a PositionChosen InteractionResult");
        }
        Position targetPos = Objects.requireNonNull(Objects.requireNonNull(result.getChosenTarget(),
                "Brewmaster target interaction invalid : no data"
                        ).getChosenPosition(),
                "Brewmaster target interaction invalid : no target position");


        List<InteractionTarget> legalTargets = new ArrayList<>();
        for (Position destination : getValidTargetDestinations(builder, targetPos, false)) {
            legalTargets.add(new InteractionTarget(TargetCategory.ActiveAbilityTargetPosition, destination));
        }

        Character targetedAlly = Objects.requireNonNull(game.getBoard().getCell(targetPos).getCharacter(),
                "A Brewmaster target position must contain an ally");
        return new InteractionRequest(
                InteractionType.PositionExpected,
                new InteractionContext(targetedAlly), // Here the request has for subjet the target
                legalTargets,
                List.of(InteractionResultType.PositionChosen, InteractionResultType.CancelAction)
        );
    }

    /** Returns the positions of all allied characters adjacent to the Brewmaster.
     * <p>Only characters belonging to the Brewmaster's team are considered.
     * Empty cells and enemy characters are ignored.</p> */
    @NonNull
    private List<Position> getAdjacentAllyPositions() {
        List<Position> allies = new ArrayList<>();

        for (Direction direction : Direction.values()) {
            Cell adjacentCell  =BoardQuery.findAdjacentCell(game.getBoard(), characterPos, direction);
            if (adjacentCell != null) {
                Character target = adjacentCell.getCharacter();
                if (target != null && target.getTeamColor() == character.getTeamColor()) {
                    allies.add(adjacentCell.getPosition());
                }
            }
        }

        return allies;
    }

    /** * Returns the valid destinations for the character targeted by the Brewmaster.
     * <p>Each adjacent empty cell is tested by building the corresponding
     * temporary action and validating it. This ensures that the destinations
     * exposed to the player respect the same action validity rules as the final action.</p>
     * <p>When {@code exitAfterFirstValidDest} is true, the method stops as soon as
     * one valid destination is found. This is used when determining whether a
     * character should be offered as an ability target in the first interaction.</p> */
    @NonNull
    private List<Position> getValidTargetDestinations(@NonNull CharacterActionBuilder builder,
                                                      @NonNull Position targetPos,
                                                      boolean exitAfterFirstValidDest) {
        Character target = Objects.requireNonNull(game.getBoard().getCell(targetPos).getCharacter(),
                "A cell targeted by the Brewmaster's ability should always contain a character");

        // Create a builder containing the target selection so that each candidate
        // destination can be evaluated in the same interaction context as the final action.
        CharacterActionBuilder targetBuilder = new CharacterActionBuilder(builder);
        if (targetBuilder.getResults().isEmpty()) {
            targetBuilder.addResult(new InteractionResult(
                    InteractionResultType.PositionChosen,
                    new InteractionContext(character),
                    new InteractionTarget(TargetCategory.ActiveAbilityTargetPosition, targetPos)
            ));
        }

        // Build and validate a temporary Move feedback for every adjacent empty cell.
        // Only destinations producing a valid action are exposed to the player.
        List<Position> targetDestinations = new ArrayList<>();
        for (Cell destination : BoardQuery.findEmptyCellsAround(game.getBoard(), targetPos, 1)) {
            CharacterActionBuilder destBuilder = new CharacterActionBuilder(targetBuilder);
            Position destPos = destination.getPosition();
            destBuilder.addResult(new InteractionResult(
                    InteractionResultType.PositionChosen,
                    new InteractionContext(target), // Here the result has for subjet the target
                    new InteractionTarget(TargetCategory.ActiveAbilityDestination, destPos)
            ));
            destBuilder.addFeedback(InteractionFeedback.createForCharacterAction(
                    List.of(new CharacterActionMotion(
                        CharacterMotionType.Move,
                        List.of(new CharacterActionTarget(target, targetPos, destPos))
                    )))
            );
            if (isActionValid(buildAction(destBuilder))) {
                targetDestinations.add(destPos);
                // Exiting early can be useful to check if a target has at least one valid destination
                if (exitAfterFirstValidDest) {
                    return targetDestinations;
                }
            }
        }

        return targetDestinations;
    }

    private boolean isBrewmasterTargetResult(@NonNull InteractionResult result) {
        return result.getResultType() == InteractionResultType.PositionChosen &&
                result.getChosenTarget() != null &&
                result.getChosenTarget().getCategory() == TargetCategory.ActiveAbilityTargetPosition;
    }

    private boolean isBrewmasterTargetDestinationResult(@NonNull InteractionResult result) {
        return result.getResultType() == InteractionResultType.PositionChosen &&
                result.getChosenTarget() != null &&
                result.getChosenTarget().getCategory() == TargetCategory.ActiveAbilityDestination;
    }
}
