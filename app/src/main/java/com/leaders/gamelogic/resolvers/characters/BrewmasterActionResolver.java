package com.leaders.gamelogic.resolvers.characters;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.gamelogic.entities.Cell;
import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.entities.Game;
import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.gamelogic.entities.Position;
import com.leaders.gamelogic.enums.CharacterMotionType;
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
 * Resolves the Brewmaster's active ability.
 *
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
         if (builder.getResults().isEmpty()) {
            return buildInitialInteraction(builder);
        }

        if (builder.isBuildCancelled()) {
            return null;
        }

        InteractionResult firstResult = builder.getResults().get(0);

        // If the first interaction was a normal movement
        if (isNormalMovementResult(firstResult)) {
            return super.getNextInteraction(builder);
        }

        // Otherwise it should be an ability activation, in which case the next (and last)
        // interaction is to select a target destination
        if (isAbilityTargetResult(firstResult)) {
            if (builder.getResults().size() > 1) {
                if (!isAbilityDestinationResult(builder.getResults().get(1))) {
                    throw new IllegalArgumentException("Invalid result type for a Brewmaster ability activation");
                }
                return null;
            } else {
                return buildTargetDestinationInteraction(builder, firstResult);
            }
        }

        throw new IllegalArgumentException("Invalid Brewmaster action builder");
    }

    @Override
    @Nullable
    public InteractionFeedback getNextFeedback(@NonNull CharacterActionBuilder builder) {
        // Brewmaster actions generate a single feedback.
        if (!ResolverUtils.isFeedbackExpected(builder)) {
            return null;
        }

        InteractionResult firstResult = builder.getResults().get(0);

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

        if (!isAbilityTargetResult(firstResult) ||
                !isAbilityDestinationResult(targetDestResult)) {
            throw new IllegalArgumentException("Invalid result type for a Brewmaster ability activation");
        }

        Position targetOrigin = ResolverUtils.getChosenPosition(firstResult);
        Character target = getCharacterAt(targetOrigin);
        Position targetDest = ResolverUtils.getChosenPosition(targetDestResult);

        return ResolverUtils.createSingleMotionFeedback(CharacterMotionType.Move, target, targetOrigin, targetDest);
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
        ResolverUtils.addTargets(legalTargets, TargetCategory.MovementDestination, getNormalMovementValidDestinations(builder));

        if (CharacterAbilityQuery.canUseActiveAbility(game, character)) {
            for (Position adjacentAllyPos : getAdjacentAllyPositions()) {
                if (!getValidTargetDestinations(builder, adjacentAllyPos, true).isEmpty()) {
                    legalTargets.add(new InteractionTarget(TargetCategory.ActiveAbilityTargetPosition, adjacentAllyPos));
                }
            }
        }

        return ResolverUtils.createPositionRequest(new InteractionContext(character), legalTargets);
    }

    /** * Builds the second interaction required by the Brewmaster's active ability.
     * <p>The first interaction identifies the allied character to move.
     * This interaction then offers every valid empty cell adjacent to that character.</p>*/
    @NonNull
    private InteractionRequest buildTargetDestinationInteraction(@NonNull CharacterActionBuilder builder,
                                                                 @NonNull InteractionResult result) {
        if (!isAbilityTargetResult(result)) {
            throw new IllegalArgumentException("A target position must be transmitted through a PositionChosen InteractionResult");
        }
        Position targetPos = ResolverUtils.getChosenPosition(result);


        List<InteractionTarget> legalTargets = new ArrayList<>();
        ResolverUtils.addTargets(legalTargets, TargetCategory.ActiveAbilityDestination,
                getValidTargetDestinations(builder, targetPos, false));

        Character targetedAlly = getCharacterAt(targetPos);
        return ResolverUtils.createPositionRequest(new InteractionContext(character, targetedAlly), legalTargets);
    }

    /** Returns the positions of all allied characters adjacent to the Brewmaster.
     * <p>Only characters belonging to the Brewmaster's team are considered.
     * Empty cells and enemy characters are ignored.</p> */
    @NonNull
    private List<Position> getAdjacentAllyPositions() {
        List<Cell> allyCells = BoardQuery.getAdjacentCells(game.getBoard(), characterPos, cell ->
                cell.getCharacter() != null && cell.getCharacter().getTeamColor() == character.getTeamColor()
        );
        return ResolverUtils.getPositions(allyCells);
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
        Character target = getCharacterAt(targetPos);

        // Create a builder containing the target selection so that each candidate
        // destination can be evaluated in the same interaction context as the final action.
        CharacterActionBuilder targetBuilder = withAbilityTargetSelected(builder, targetPos);

        // Build and validate a temporary Move feedback for every adjacent empty cell.
        // Only destinations producing a valid action are exposed to the player.
        List<Position> targetDestinations = new ArrayList<>();
        for (Cell destination : BoardQuery.findEmptyCellsAround(game.getBoard(), targetPos, 1)) {
            Position destPos = destination.getPosition();

            InteractionResult destResult = ResolverUtils.createPositionResult(new InteractionContext(character, target),
                    TargetCategory.ActiveAbilityDestination, destPos);
            InteractionFeedback destFeedback = ResolverUtils.createSingleMotionFeedback(
                    CharacterMotionType.Move, target, targetPos, destPos);

            if (isValidWith(targetBuilder, destResult, destFeedback)) {
                targetDestinations.add(destPos);
                // Exiting early can be useful to check if a target has at least one valid destination
                if (exitAfterFirstValidDest) {
                    return targetDestinations;
                }
            }
        }

        return targetDestinations;
    }
}
