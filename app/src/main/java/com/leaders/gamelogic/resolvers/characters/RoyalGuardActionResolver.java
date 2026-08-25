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
import com.leaders.gamelogic.queries.CharacterAbilityQuery;
import com.leaders.gamelogic.resolvers.CharacterActionResolver;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Resolves the Royal Guard's active ability.
 *
 * <p>The Royal Guard can move directly to an empty cell adjacent to its Leader.
 * After this movement, it may move one additional cell.</p>
 *
 * <p>The destination selected by the player represents the final position of
 * the Royal Guard. The intermediate position adjacent to the Leader is
 * determined automatically.</p>
 */
public final class RoyalGuardActionResolver extends CharacterActionResolver {

    public RoyalGuardActionResolver(@NonNull Game game, @NonNull GameHistory gameHistory,
                                    @NonNull Character character) {
        super(game, gameHistory, character);
    }

    @Override
    @Nullable
    public InteractionRequest getNextInteraction(@NonNull CharacterActionBuilder builder) {
        // Royal Guard actions require a single interaction.
        // If an interaction has already been selected, no further interaction can be added.
        if (!builder.getResults().isEmpty()) {
            return null;
        }

        List<InteractionTarget> legalTargets = new ArrayList<>();

        // Normal movement and Royal Guard ability destinations may overlap.
        // Normal movement has priority in that case.
        List<Position> movementDestinations = getNormalMovementValidDestinations(builder);

        for (Position destination : movementDestinations) {
            legalTargets.add(new InteractionTarget(TargetCategory.MovementDestination, destination));
        }


        if (CharacterAbilityQuery.canUseActiveAbility(game, character)) {
            for (Position destination : getRoyalGuardDestinations(builder)) {
                if (!movementDestinations.contains(destination)) {
                    legalTargets.add(new InteractionTarget(TargetCategory.ActiveAbilityDestination, destination));
                }
            }
        }

        return new InteractionRequest(
                InteractionType.PositionExpected,
                new InteractionContext(character),
                legalTargets,
                List.of(InteractionResultType.PositionChosen, InteractionResultType.CancelAction)
        );
    }

    @Override
    @Nullable
    public InteractionFeedback getNextFeedback(@NonNull CharacterActionBuilder builder) {
        if (builder.getResults().isEmpty() ||
                !builder.getFeedbacks().isEmpty()) {
            return null;
        }

        InteractionResult result = builder.getResults().get(0);

        if (result.getResultType() == InteractionResultType.CancelAction) {
            return null;
        }

        InteractionFeedback feedback;
        if (isNormalMovementResult(result)) {
            feedback = super.getNextFeedback(builder);
        } else if (isRoyalGuardDestinationResult(result)){
            feedback = buildRoyalGuardMovementFeedback(result);
        } else {
            throw new IllegalArgumentException("Invalid Royal Guard interaction type " + result.getResultType());
        }
        return feedback;
    }

    /**
     * Returns all valid final destinations reachable through the Royal Guard's
     * active ability.
     *
     * <p>The first movement must place the Royal Guard on an empty cell adjacent
     * to its Leader. From each such intermediate position, the Guard may then
     * move to an adjacent empty cell. Each resulting action is validated before
     * its final destination is exposed to the player.</p>
     */
    @NonNull
    private List<Position> getRoyalGuardDestinations(@NonNull CharacterActionBuilder builder) {
        List<Position> destinations = new ArrayList<>();

        Cell leaderCell = BoardQuery.findLeaderCell(game.getBoard(), character.getTeamColor());
        if (leaderCell == null) {
            return destinations;
        }

        for (Cell destCell : BoardQuery.findEmptyCellsAround(game.getBoard(), leaderCell.getPosition(), 2)) {
            Position destPos = destCell.getPosition();

            CharacterActionBuilder destBuilder = new CharacterActionBuilder(builder);
            InteractionResult result = new InteractionResult(
                    InteractionResultType.PositionChosen,
                    new InteractionContext(character),
                    new InteractionTarget(TargetCategory.ActiveAbilityDestination, destPos)
            );
            destBuilder.addResult(result);

            destBuilder.addFeedback(buildRoyalGuardMovementFeedback(result));

            if (isActionValid(buildAction(destBuilder))) {
                destinations.add(destPos);
            }
        }

        return destinations;
    }

    @NonNull
    private InteractionFeedback buildRoyalGuardMovementFeedback(@NonNull InteractionResult result) {
        Position destPos = Objects.requireNonNull(
                Objects.requireNonNull(
                        result.getChosenTarget(),
                        "Royal Guard destination interaction result invalid: no data"
                ).getChosenPosition(),
                "Royal Guard destination interaction result invalid: no destination position"
        );


        List<CharacterActionMotion> abilityMotions = new ArrayList<>();

        Position intermediatePos = getIntermediatePosition(destPos);
        if (intermediatePos != null) {
            abilityMotions.add(new CharacterActionMotion(CharacterMotionType.Teleport,
                    List.of(new CharacterActionTarget(character, characterPos, intermediatePos))));
            abilityMotions.add(new CharacterActionMotion(CharacterMotionType.Move,
                    List.of(new CharacterActionTarget(character, intermediatePos, destPos))));
        } else {
            abilityMotions.add(new CharacterActionMotion(CharacterMotionType.Teleport,
                    List.of(new CharacterActionTarget(character, characterPos, destPos))));
        }

        return InteractionFeedback.createForCharacterAction(abilityMotions);
    }

    @Nullable
    private Position getIntermediatePosition(@NonNull Position destPos) {
        Cell leaderCell = BoardQuery.findLeaderCell(game.getBoard(), character.getTeamColor());
        if (leaderCell == null) {
            throw new IllegalStateException("Cannot find a royal guard teleportation destination without an ally leader");
        }

        List<Position> intermediatePositions = new ArrayList<>();
        for (Direction direction : Direction.values()) {
            Position teleportPos = leaderCell.getPosition().adjacent(direction);
            if (teleportPos == null) {
                continue;
            }

            // Destination is accessible with the initial teleportation -> no intermediate needed
            if (teleportPos.equals(destPos)) {
                return null;
            }

            if (teleportPos.distanceTo(destPos) == 1) {
                intermediatePositions.add(teleportPos);
            }
        }

        // If we get here, it means the destPos is not adjacent to the leader
        if (!intermediatePositions.isEmpty()) {
            return intermediatePositions.get(0);
        }

        throw new IllegalArgumentException("Unreachable Royal Guard destination: " + destPos);
    }

    private boolean isRoyalGuardDestinationResult(@NonNull InteractionResult result) {
        return result.getResultType() == InteractionResultType.PositionChosen &&
                result.getChosenTarget() != null &&
                result.getChosenTarget().getCategory() == TargetCategory.ActiveAbilityDestination;
    }
}