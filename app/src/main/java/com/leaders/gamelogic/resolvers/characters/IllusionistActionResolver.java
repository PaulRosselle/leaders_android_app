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
        // Rider actions require a single interaction.
        // If an interaction has already been selected, no further interaction can be added to this action.
        if (!builder.getResults().isEmpty()) {
            return null;
        }

        // Normal movement and Illusionist swap targets cannot overlap:
        // a normal move requires an adjacent cell, while a targets cannot be adjacent.
        // For this reason, we can add both destinations without further treatment to avoid duplicates
        List<InteractionTarget> legalTargets = new ArrayList<>();

        for (Position destination : getNormalMovementValidDestinations(builder)) {
            legalTargets.add(new InteractionTarget(TargetCategory.MovementDestination, destination));
        }

        for (Position targetPosition : getIllusionistTargetPositions(builder)) {
            legalTargets.add(new InteractionTarget(
                    TargetCategory.ActiveAbilityTargetPosition, targetPosition));
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
        if (builder.getResults().isEmpty() ||
                !builder.getFeedbacks().isEmpty()) {
            return null;
        }

        InteractionResult result = builder.getResults().get(0);

        if (result.getResultType() == InteractionResultType.CancelAction) {
            return null;
        }

        if (isNormalMovementResult(result)) {
            return super.getNextFeedback(builder);
        }

        if (!isIllusionistTargetResult(result)) {
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
            // The Illusionist cannot target adjacent characters.
            Cell adjacentCell = BoardQuery.findAdjacentCell(game.getBoard(), characterPos, direction);
            if (adjacentCell != null && adjacentCell.getCharacter() == null) {
                Cell targetCell = BoardQuery.findVisibleCharacterCell(game.getBoard(), characterPos,
                        direction, null, null);
                if (targetCell != null) {
                    Position targetPos = targetCell.getPosition();

                    InteractionResult swapResult = new InteractionResult(
                            InteractionResultType.PositionChosen,
                            new InteractionTarget(
                                    TargetCategory.ActiveAbilityTargetPosition,
                                    targetPos
                            )
                    );
                    CharacterActionBuilder targetBuilder = new CharacterActionBuilder(builder);
                    targetBuilder.addResult(swapResult);
                    targetBuilder.addFeedback(buildSwapFeedback(swapResult));

                    if (isActionValid(buildAction(targetBuilder))) {
                        targets.add(targetPos);
                    }
                }
            }
        }

        return targets;
    }

    @NonNull
    private InteractionFeedback buildSwapFeedback(@NonNull InteractionResult result) {
        Position targetPos = Objects.requireNonNull(
                Objects.requireNonNull(
                        result.getChosenTarget(),
                        "Illusionist target interaction result invalid: no data"
                ).getChosenPosition(),
                "Illusionist target interaction result invalid: no target position"
        );

        Character target = Objects.requireNonNull(
                game.getBoard().getCell(targetPos).getCharacter(),
                "Illusionist target position should contain a character"
        );

        return new InteractionFeedback(new CharacterActionMotion(
                CharacterMotionType.Swap,
                List.of(
                        new CharacterActionTarget(character, characterPos, targetPos),
                        new CharacterActionTarget(target, targetPos, characterPos)
                )
        ));
    }

    private boolean isIllusionistTargetResult(@NonNull InteractionResult result) {
        return result.getResultType() == InteractionResultType.PositionChosen &&
                result.getChosenTarget() != null &&
                result.getChosenTarget().getCategory() == TargetCategory.ActiveAbilityTargetPosition;
    }
}