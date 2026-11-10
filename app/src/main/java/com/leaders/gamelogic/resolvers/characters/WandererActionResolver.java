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
 * Resolves the Wanderer's active ability.
 *
 * <p>The Wanderer can move to any empty cell that is not adjacent to an enemy.</p>
 *
 * <p>The destination is selected directly by the player through a single
 * interaction. The selected destination is then used to build the movement
 * feedback.</p>
 */
public final class WandererActionResolver extends CharacterActionResolver {

    public WandererActionResolver(@NonNull Game game, @NonNull GameHistory gameHistory,
                                  @NonNull Character character) {
        super(game, gameHistory, character);
    }

    @Override
    @Nullable
    public InteractionRequest getNextInteraction(@NonNull CharacterActionBuilder builder) {
        // Wanderer actions require a single interaction.
        // If an interaction has already been selected, no further interaction can be added to this action.
        if (!builder.getResults().isEmpty()) {
            return null;
        }

        List<InteractionTarget> legalTargets = new ArrayList<>();

        // Normal movement and Wanderer destinations may overlap. We give priority to movement
        // meaning a cell reachable via normal movement won't be added from the active ability cells
        List<Position> movementDestinations = getNormalMovementValidDestinations(builder);
        ResolverUtils.addTargets(legalTargets, TargetCategory.MovementDestination, movementDestinations);

        if (CharacterAbilityQuery.canUseActiveAbility(game, character)) {
            for (Position flightDest : getWandererFlightDestinations(builder)) {
                if (!movementDestinations.contains(flightDest)) {
                    legalTargets.add(new InteractionTarget(TargetCategory.ActiveAbilityDestination, flightDest));
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

        InteractionFeedback feedback;
        if (isNormalMovementResult(result)) {
            feedback = super.getNextFeedback(builder);
        } else if (isAbilityDestinationResult(result)){
            feedback = buildWandererMovementFeedback(result);
        } else {
            throw new IllegalArgumentException("Invalid Wanderer interaction type " + result.getResultType());
        }
        return feedback;
    }

    /**
     * Returns all empty cells that are not adjacent to an enemy.
     *
     * <p>Each candidate is validated through the standard action validation
     * mechanism before being exposed to the player.</p>
     */
    @NonNull
    private List<Position> getWandererFlightDestinations(@NonNull CharacterActionBuilder builder) {
        List<Position> destinations = new ArrayList<>();

        for (Cell cell : game.getBoard().getCells().values()) {
            if (cell.getCharacter() == null && !isCellAdjacentToEnemy(cell.getPosition())) {
                Position destPos = cell.getPosition();

                InteractionResult flightResult = ResolverUtils.createPositionResult(new InteractionContext(character),
                        TargetCategory.ActiveAbilityDestination, destPos);

                if (isValidWith(builder, flightResult, buildWandererMovementFeedback(flightResult))) {
                    destinations.add(destPos);
                }
            }
        }

        return destinations;
    }

    private boolean isCellAdjacentToEnemy(@NonNull Position cellPos) {
        return !BoardQuery.getAdjacentCells(game.getBoard(), cellPos, cell ->
                cell.getCharacter() != null &&
                        cell.getCharacter().getTeamColor() != character.getTeamColor()
        ).isEmpty();
    }

    @NonNull
    private InteractionFeedback buildWandererMovementFeedback(@NonNull InteractionResult result) {
        Position destination = ResolverUtils.getChosenPosition(result);

        return ResolverUtils.createSingleMotionFeedback(CharacterMotionType.Fly, character, characterPos, destination);
    }
}