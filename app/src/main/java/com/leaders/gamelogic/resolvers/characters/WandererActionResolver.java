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
        if (!builder.getInteractionResults().isEmpty()) {
            return null;
        }

        List<InteractionTarget> legalTargets = new ArrayList<>();

        // Normal movement and Wanderer destinations may overlap. We give priority to movement
        // meaning a cell reachable via normal movement won't be added from the active ability cells
        List<Position> movementDestinations = getNormalMovementValidDestinations(builder);
        for (Position movementDest : movementDestinations) {
            legalTargets.add(new InteractionTarget(TargetCategory.MovementDestination, movementDest));
        }
        for (Position flightDest : getWandererFlightDestinations(builder)) {
            if (!movementDestinations.contains(flightDest)) {
                legalTargets.add(new InteractionTarget(TargetCategory.ActiveAbilityDestination, flightDest));
            }
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

        InteractionFeedback feedback;
        if (isNormalMovementResult(result)) {
            feedback = super.getNextFeedback(builder);
        } else if (isWandererDestinationResult(result)){
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

                CharacterActionBuilder destinationBuilder = new CharacterActionBuilder(builder);
                destinationBuilder.addResult(new InteractionResult(
                        InteractionResultType.PositionChosen,
                        new InteractionTarget(
                                TargetCategory.ActiveAbilityDestination,
                                destPos
                        )
                ));

                destinationBuilder.addFeedback(new InteractionFeedback(
                        new CharacterActionMotion(
                                CharacterMotionType.Move,
                                List.of(new CharacterActionTarget(
                                        character, characterPos, destPos))
                        )
                ));

                if (isActionValid(buildAction(destinationBuilder))) {
                    destinations.add(destPos);
                }
            }
        }

        return destinations;
    }

    private boolean isCellAdjacentToEnemy(@NonNull Position cellPos) {
        for (Direction direction : Direction.values()) {
            Cell adjacentCell = BoardQuery.findAdjacentCell(game.getBoard(), cellPos, direction);
            if (adjacentCell != null && adjacentCell.getCharacter() != null &&
                    adjacentCell.getCharacter().getTeamColor() != character.getTeamColor()) {
                return true;
            }
        }
        return false;
    }

    @NonNull
    private InteractionFeedback buildWandererMovementFeedback(@NonNull InteractionResult result) {
        Position destination = Objects.requireNonNull(
                Objects.requireNonNull(
                        result.getChosenTarget(),
                        "Wanderer destination interaction result invalid: no data"
                ).getChosenPosition(),
                "Wanderer destination interaction result invalid: no destination position"
        );

        return new InteractionFeedback(new CharacterActionMotion(
                CharacterMotionType.Move,
                List.of(new CharacterActionTarget(character, characterPos, destination))
        ));
    }

    private boolean isWandererDestinationResult(@NonNull InteractionResult result) {
        return result.getResultType() == InteractionResultType.PositionChosen &&
                result.getChosenTarget() != null &&
                result.getChosenTarget().getCategory() == TargetCategory.ActiveAbilityDestination;
    }
}