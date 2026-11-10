package com.leaders.gamelogic.resolvers.characters;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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
import com.leaders.gamelogic.interactions.InteractionTarget;
import com.leaders.gamelogic.interactions.TargetCategory;
import com.leaders.gamelogic.queries.BoardQuery;
import com.leaders.gamelogic.queries.CharacterAbilityQuery;
import com.leaders.gamelogic.resolvers.CharacterActionResolver;

import java.util.ArrayList;
import java.util.List;

import static com.leaders.gamelogic.resolvers.ResolverUtils.addTargets;
import static com.leaders.gamelogic.resolvers.ResolverUtils.createMotionFeedback;
import static com.leaders.gamelogic.resolvers.ResolverUtils.createPositionRequest;
import static com.leaders.gamelogic.resolvers.ResolverUtils.createPositionResult;
import static com.leaders.gamelogic.resolvers.ResolverUtils.getChosenPosition;
import static com.leaders.gamelogic.resolvers.ResolverUtils.isFeedbackExpected;

/**
 * Resolves the Illusionist's active ability.
 *
 * <p>The Illusionist swaps positions with a visible character in a straight
 * line that is not adjacent.</p>
 *
 * <p>The character to swap with is selected through a single interaction.
 * The destination of both characters is then determined automatically from
 * their respective starting positions.</p>
 */
public final class IllusionistActionResolver extends CharacterActionResolver {

    public IllusionistActionResolver(@NonNull Game game, @NonNull GameHistory gameHistory, @NonNull Character character) {
        super(game, gameHistory, character);
    }

    @Override
    @Nullable
    public InteractionRequest getNextInteraction(@NonNull CharacterActionBuilder builder) {
        if (!builder.getResults().isEmpty()) {
            return null;
        }

        // Normal movement and Illusionist swap targets cannot overlap:
        // a normal move requires an adjacent cell, while a targets cannot be adjacent.
        // For this reason, we can add both destinations without further treatment to avoid duplicates
        List<InteractionTarget> legalTargets = new ArrayList<>();

        addTargets(legalTargets, TargetCategory.MovementDestination, getNormalMovementValidDestinations(builder));

        if (CharacterAbilityQuery.canUseActiveAbility(game, character)) {
            addTargets(legalTargets, TargetCategory.ActiveAbilityTargetPosition, getIllusionistTargetPositions(builder));
        }

        return createPositionRequest(new InteractionContext(character), legalTargets);
    }

    @Override
    @Nullable
    public InteractionFeedback getNextFeedback(@NonNull CharacterActionBuilder builder) {
        if (!isFeedbackExpected(builder)) {
            return null;
        }

        InteractionResult result = builder.getResults().get(0);

        if (isNormalMovementResult(result)) {
            return super.getNextFeedback(builder);
        }

        if (!isAbilityTargetResult(result)) {
            throw new IllegalArgumentException(
                    "Invalid Illusionist interaction type " + result.getResultType());
        }

        return buildSwapFeedback(result);
    }

    /**
     * Returns all valid characters that can be targeted by the Illusionist's
     * active ability.
     *
     * <p>The target must be an enemy visible in a straight line and must not
     * be adjacent to the Illusionist. Only the first character encountered
     * in each direction can be visible and targeted.</p>
     */
    @NonNull
    private List<Position> getIllusionistTargetPositions(@NonNull CharacterActionBuilder builder) {
        List<Position> targets = new ArrayList<>();

        for (Direction direction : Direction.values()) {
            Cell adjacentCell = BoardQuery.findAdjacentCell(game.getBoard(), characterPos, direction);
            if (adjacentCell != null && adjacentCell.getCharacter() == null) {
                Cell targetCell = BoardQuery.findVisibleCharacterCell(game.getBoard(),
                        adjacentCell.getPosition(), direction, null, null);
                if (targetCell != null &&
                        (targetCell.getCharacter().getTeamColor() == character.getTeamColor() ||
                                CharacterAbilityQuery.canBeMovedByEnemyAbilities(game, targetCell.getCharacter()))) {
                    Position targetPos = targetCell.getPosition();

                    InteractionResult swapResult = createPositionResult(new InteractionContext(character),
                            TargetCategory.ActiveAbilityTargetPosition, targetPos);

                    if (isValidWith(builder, swapResult, buildSwapFeedback(swapResult))) {
                        targets.add(targetPos);
                    }
                }
            }
        }

        return targets;
    }

    @NonNull
    private InteractionFeedback buildSwapFeedback(@NonNull InteractionResult result) {
        Position targetPos = getChosenPosition(result);
        Character target = getCharacterAt(targetPos);

        return createMotionFeedback(CharacterMotionType.Swap,
                new CharacterActionTarget(character, characterPos, targetPos),
                new CharacterActionTarget(target, targetPos, characterPos));
    }
}