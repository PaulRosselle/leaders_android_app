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
 * Base archetype for characters able to move other characters as an Active Ability.
 */
public abstract class MoverActionResolver extends CharacterActionResolver {
    public MoverActionResolver(@NonNull Game game, @NonNull GameHistory gameHistory, @NonNull Character character) {
        super(game, gameHistory, character);
    }

    protected int getTargetMovementMaxDistance() {
        // The default Mover behavior is to move the target by one cell
        return 1;
    }

    protected abstract List<Position> getTargetPositions();

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
                    throw new IllegalArgumentException("Invalid result type for a Mover ability activation");
                }
                return null;
            } else {
                return buildTargetDestinationInteraction(builder, firstResult);
            }
        }

        throw new IllegalArgumentException("Invalid Mover action builder");
    }

    @Override
    @Nullable
    public InteractionFeedback getNextFeedback(@NonNull CharacterActionBuilder builder) {
        // Mover actions generate a single feedback.
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
            throw new IllegalArgumentException("Invalid result type for a Mover ability activation");
        }

        Position targetOrigin = ResolverUtils.getChosenPosition(firstResult);
        Character target = getCharacterAt(targetOrigin);
        Position targetDest = ResolverUtils.getChosenPosition(targetDestResult);

        return ResolverUtils.createSingleMotionFeedback(CharacterMotionType.Move, target, targetOrigin, targetDest);
    }

    /** Builds the first interaction of the action.
     * <p>The player can either choose a normal movement destination for the
     * Mover or select a valid target.</p>*/
    @NonNull
    private InteractionRequest buildInitialInteraction(@NonNull CharacterActionBuilder builder) {
        List<InteractionTarget> legalTargets = new ArrayList<>();

        // A Mover ability target characters. Therefore, its target positions cannot overlap with
        // the normal movement positions which requires empty cells
        ResolverUtils.addTargets(legalTargets, TargetCategory.MovementDestination, getNormalMovementValidDestinations(builder));

        if (canUseActiveAbility()) {
            for (Position targetPos : getTargetPositions()) {
                if (!getValidTargetDestinations(builder, targetPos, true).isEmpty()) {
                    legalTargets.add(new InteractionTarget(TargetCategory.ActiveAbilityTargetPosition, targetPos));
                }
            }
        }

        return ResolverUtils.createPositionRequest(new InteractionContext(character), legalTargets);
    }

    protected boolean canUseActiveAbility() {
        return CharacterAbilityQuery.canUseActiveAbility(game, gameHistory, character);
    }

    /**
     * Builds an interaction request for selecting a destination for the targeted ally.
     *
     * @param builder the character action builder
     * @param result the interaction result containing the selected target position
     * @return the interaction request for selecting a valid destination
     * @throws IllegalArgumentException if the result does not contain a selected target position
     */
    @NonNull
    private InteractionRequest buildTargetDestinationInteraction(@NonNull CharacterActionBuilder builder,
                                                                 @NonNull InteractionResult result) {
        if (!isAbilityTargetResult(result)) {
            throw new IllegalArgumentException("A target position must be transmitted through a PositionChosen InteractionResult");
        }
        Position targetPos = ResolverUtils.getChosenPosition(result);

        List<InteractionTarget> legalTargets = new ArrayList<>();
        ResolverUtils.addTargets(
                legalTargets, TargetCategory.ActiveAbilityDestination,
                getValidTargetDestinations(builder, targetPos, false)
        );

        Character targetedAlly = getCharacterAt(targetPos);
        return ResolverUtils.createPositionRequest(new InteractionContext(character, targetedAlly), legalTargets);
    }

    /**
     * Gets the valid destination positions for the targeted character.
     *
     * @param builder the character action builder
     * @param targetPos the position of the targeted character
     * @param exitAfterFirstValidDest whether to stop after finding the first valid destination
     * @return the list of valid destination positions
     */
    @NonNull
    protected List<Position> getValidTargetDestinations(@NonNull CharacterActionBuilder builder,
                                                        @NonNull Position targetPos,
                                                        boolean exitAfterFirstValidDest) {
        Character target = getCharacterAt(targetPos);

        List<Position> destinations = new ArrayList<>();


        CharacterActionBuilder targetBuilder = withAbilityTargetSelected(builder, targetPos);

        for (Cell destinationCell : BoardQuery.findEmptyCellsAround(game.getBoard(), targetPos, getTargetMovementMaxDistance())) {
            Position destination = destinationCell.getPosition();

            InteractionResult destinationResult = ResolverUtils.createPositionResult(new InteractionContext(character, target),
                    TargetCategory.ActiveAbilityDestination, destination);
            InteractionFeedback destinationFeedback = ResolverUtils.createSingleMotionFeedback(
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
