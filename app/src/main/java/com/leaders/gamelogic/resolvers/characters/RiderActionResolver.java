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
 * Resolves the Rider's active ability.
 *
 * <p>The Rider can move two cells in a straight line. Both cells crossed by
 * the movement must be valid for the movement to be performed.</p>
 */
public final class RiderActionResolver extends CharacterActionResolver {

    public RiderActionResolver(@NonNull Game game, @NonNull GameHistory gameHistory, @NonNull Character character) {
        super(game, gameHistory, character);
    }

    @Override
    @Nullable
    public InteractionRequest getNextInteraction(@NonNull CharacterActionBuilder builder) {
        // Rider actions require a single interaction.
        // If an interaction has already been selected, no further interaction can be added to this action.
        if (!builder.getResults().isEmpty()) {
            return null;
        }

        List<InteractionTarget> legalTargets = new ArrayList<>();

        // Normal movement and Rider dash destinations cannot overlap:
        // a normal move requires an adjacent cell, while a dash destination is at exactly two cells away.
        // For this reason, we can add both destinations without further treatment to avoid duplicates
        for (Position destination : getNormalMovementValidDestinations(builder)) {
            legalTargets.add(new InteractionTarget(TargetCategory.MovementDestination, destination));
        }

        if (CharacterAbilityQuery.canUseActiveAbility(game, character)) {
            for (Position destination : getRiderDashDestinations(builder)) {
                legalTargets.add(new InteractionTarget(TargetCategory.ActiveAbilityDestination, destination));
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
        } else if (isRiderDestinationResult(result)){
            feedback = buildRiderDashFeedback(result);
        } else {
            throw new IllegalArgumentException("Invalid Rider interaction type " + result.getResultType());
        }
        return feedback;
    }

    /**
     * Returns all valid destinations reachable by the Rider's active ability.
     *
     * <p>The Rider moves exactly two cells in a straight line. The intermediate
     * cell must therefore exist and be empty, as must the destination cell.</p>
     */
    @NonNull
    private List<Position> getRiderDashDestinations(@NonNull CharacterActionBuilder builder) {
        List<Position> destinations = new ArrayList<>();

        for (Direction direction : Direction.values()) {
            Cell intermediateCell = BoardQuery.findAdjacentCell(game.getBoard(), characterPos, direction);
            if (intermediateCell != null && intermediateCell.getCharacter() == null) {
                Cell destCell = BoardQuery.findAdjacentCell(game.getBoard(), intermediateCell.getPosition(), direction);
                if (destCell != null && destCell.getCharacter() == null) {
                    Position destPos = destCell.getPosition();

                    CharacterActionBuilder destBuilder = new CharacterActionBuilder(builder);
                    destBuilder.addResult(new InteractionResult(
                            InteractionResultType.PositionChosen,
                            new InteractionContext(character),
                            new InteractionTarget(TargetCategory.ActiveAbilityDestination, destPos)
                    ));

                    destBuilder.addFeedback(InteractionFeedback.createForCharacterAction(
                            List.of(new CharacterActionMotion(
                                    CharacterMotionType.Move,
                                    List.of(new CharacterActionTarget(character, characterPos, destPos))
                            ))
                    ));

                    if (isActionValid(buildAction(destBuilder))) {
                        destinations.add(destPos);
                    }
                }
            }
        }

        return destinations;
    }

    private InteractionFeedback buildRiderDashFeedback(@NonNull InteractionResult result) {
        Position destination = Objects.requireNonNull(
                Objects.requireNonNull(result.getChosenTarget(),
                                "Rider destination interaction result invalid: no data")
                        .getChosenPosition(),
                "Rider destination interaction result invalid: no destination position");

        return InteractionFeedback.createForCharacterAction(List.of(new CharacterActionMotion(
                CharacterMotionType.Move,
                List.of(new CharacterActionTarget(character, characterPos, destination))
        )));
    }

    private boolean isRiderDestinationResult(@NonNull InteractionResult result) {
        return result.getResultType() == InteractionResultType.PositionChosen &&
                result.getChosenTarget() != null &&
                result.getChosenTarget().getCategory() == TargetCategory.ActiveAbilityDestination;
    }
}