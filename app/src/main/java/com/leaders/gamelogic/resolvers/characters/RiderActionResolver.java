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
        if (!builder.getResults().isEmpty()) {
            return null;
        }

        List<InteractionTarget> legalTargets = new ArrayList<>();

        // Normal movement and Rider dash destinations cannot overlap:
        // a normal move requires an adjacent cell, while a dash destination is at exactly two cells away.
        // For this reason, we can add both destinations without further treatment to avoid duplicates
        ResolverUtils.addTargets(legalTargets, TargetCategory.MovementDestination, getNormalMovementValidDestinations(builder));

        if (CharacterAbilityQuery.canUseActiveAbility(game, character)) {
            ResolverUtils.addTargets(legalTargets, TargetCategory.ActiveAbilityDestination, getRiderDashDestinations(builder));
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

                    InteractionResult dashResult = ResolverUtils.createPositionResult(new InteractionContext(character),
                            TargetCategory.ActiveAbilityDestination, destPos);

                    if (isValidWith(builder, dashResult, buildRiderDashFeedback(dashResult))) {
                        destinations.add(destPos);
                    }
                }
            }
        }

        return destinations;
    }

    private InteractionFeedback buildRiderDashFeedback(@NonNull InteractionResult result) {
        Position destination = ResolverUtils.getChosenPosition(result);

        return ResolverUtils.createSingleMotionFeedback(CharacterMotionType.Move, character, characterPos, destination);
    }
}