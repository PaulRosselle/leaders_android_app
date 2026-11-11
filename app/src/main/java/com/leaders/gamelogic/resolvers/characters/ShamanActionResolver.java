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
import com.leaders.gamelogic.entities.TransformedState;
import com.leaders.gamelogic.enums.CharacterMotionType;
import com.leaders.gamelogic.enums.CharacterType;
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
import java.util.Objects;
import java.util.UUID;

/**
 * Resolves the Shaman's active ability.
 *
 * <p>The Shaman transforms into a frog a character visible in a straight
 * line that is not adjacent to him.</p>
 *
 * <p>The character transformed is selected through a single interaction.
 * The destination of both characters is then determined automatically from
 * their respective starting positions.</p>
 */
public class ShamanActionResolver extends CharacterActionResolver {
    public ShamanActionResolver(@NonNull Game game, @NonNull GameHistory gameHistory, @NonNull Character character) {
        super(game, gameHistory, character);
    }

    @Override
    @Nullable
    public InteractionRequest getNextInteraction(@NonNull CharacterActionBuilder builder) {
        if (!builder.getResults().isEmpty()) {
            return null;
        }

        List<InteractionTarget> legalTargets = new ArrayList<>();

        ResolverUtils.addTargets(legalTargets, TargetCategory.MovementDestination, getNormalMovementValidDestinations(builder));

        if (CharacterAbilityQuery.canUseActiveAbility(game, gameHistory, character)) {
            ResolverUtils.addTargets(legalTargets, TargetCategory.ActiveAbilityTargetPosition, getShamanTargetPositions(builder));
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

        if (isNormalMovementResult(result)) {
            return super.getNextFeedback(builder);
        }

        if (!isAbilityTargetResult(result)) {
            throw new IllegalArgumentException("Invalid Shaman interaction type " + result.getResultType());
        }

        return buildTransformFeedback(result);
    }

    /**
     * Returns all valid characters that can be targeted by the Shaman's active ability.
     *
     * <p>The target must be a non-leader character visible in a straight line and must not
     * be adjacent to the Shaman. Only the first character encountered in each direction
     * can be visible and targeted.</p>
     */
    @NonNull
    private List<Position> getShamanTargetPositions(@NonNull CharacterActionBuilder builder) {
        List<Position> targets = new ArrayList<>();

        for (Cell targetCell : BoardQuery.findVisibleCharacterCells(
                game.getBoard(), characterPos, null, null, true)) {
            if (!targetCell.getCharacter().getCharacterType().getCharacterCard().isLeader()) {
                Position targetPos = targetCell.getPosition();

                InteractionResult swapResult = ResolverUtils.createPositionResult(
                        new InteractionContext(character),
                        TargetCategory.ActiveAbilityTargetPosition, targetPos
                );

                if (isValidWith(builder, swapResult, buildTransformFeedback(swapResult))) {
                    targets.add(targetPos);
                }
            }
        }

        return targets;
    }

    /**
     * Builds feedback for transforming the selected character into a Frog.
     * Any characters already transformed into Frogs are untransformed first.
     *
     * @param result the interaction result containing the selected position
     * @return the interaction feedback representing the untransformations and new transformation
     */
    @NonNull
    private InteractionFeedback buildTransformFeedback(@NonNull InteractionResult result) {
        List<CharacterActionMotion> motions = new ArrayList<>();

        // First we untransform any existing frog
        for (TransformedState transformedFrogState : getExistingTransformedFrogStates()) {
            motions.add(createTransformMotion(
                    CharacterMotionType.Untransform,
                    transformedFrogState.getCharacterAfter(),
                    transformedFrogState.getCharacterBefore(),
                    BoardQuery.getCellByCharacterId(
                            game.getBoard(), transformedFrogState.getCharacterId()
                    ).getPosition()
            ));
        }

        Position targetPos = ResolverUtils.getChosenPosition(result);
        Character target = getCharacterAt(targetPos);
        Character transformedTarget = Character.transform(target, CharacterType.Frog, target.getTeamColor());

        // Finally we add the frog transformation motion
        motions.add(createTransformMotion(CharacterMotionType.Transform, target, transformedTarget, targetPos));


        return InteractionFeedback.createForCharacterAction(motions);
    }

    /**
     * Creates a motion representing the transformation or untransformation of a character.
     *
     * @param motionType the type of motion
     * @param originCharacter the character before the transformation
     * @param transformedCharacter the character after the transformation
     * @param characterPosition the position of the character
     * @return the character action motion representing the transformation
     */
    @NonNull
    private CharacterActionMotion createTransformMotion(@NonNull CharacterMotionType motionType,
                                                        @NonNull Character originCharacter,
                                                        @NonNull Character transformedCharacter,
                                                        @NonNull Position characterPosition) {
        return new CharacterActionMotion(motionType,
                List.of(new CharacterActionTarget(originCharacter, characterPosition, null),
                        new CharacterActionTarget(transformedCharacter, null, characterPosition)
                )
        );
    }

    /**
     * Gets the transformed states of characters currently transformed into Frogs.
     *
     * @return the list of transformed Frog states
     * @throws NullPointerException if a transformed state cannot be found for a character ID
     */
    @NonNull
    private List<TransformedState> getExistingTransformedFrogStates() {
        List<TransformedState> transformedFrogStates = new ArrayList<>();

        for (UUID characterId : game.getTransformedCharacterIds()) {
            TransformedState transformedState = Objects.requireNonNull(
                    game.getTransformedState(characterId),
                    "No transformed character found matching id: " + characterId
            );

            if (transformedState.getCharacterAfter().getCharacterType() == CharacterType.Frog) {
                transformedFrogStates.add(transformedState);
            }
        }

        return transformedFrogStates;
    }
}
