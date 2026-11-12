package com.leaders.gamelogic.resolvers.characters;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.gamelogic.entities.Board;
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
import com.leaders.gamelogic.resolvers.CharacterActionResolver;
import com.leaders.gamelogic.resolvers.ResolverUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Resolves the Frog's special ability.
 *
 * <p>The Frog can use his normal movement to jump to any empty cell in a range of two cells.</p>
 *
 * <p>The destination is selected directly by the player through a single
 * interaction. The selected destination is then used to build the movement
 * feedback.</p>
 */
public final class FrogActionResolver extends CharacterActionResolver {
    public FrogActionResolver(@NonNull Game game, @NonNull GameHistory gameHistory, @NonNull Character character) {
        super(game, gameHistory, character);
    }

    @Override
    @Nullable
    public InteractionRequest getNextInteraction(@NonNull CharacterActionBuilder builder) {
        if (!builder.getResults().isEmpty()) {
            return null;
        }

        List<InteractionTarget> legalTargets = new ArrayList<>();
        ResolverUtils.addTargets(legalTargets, TargetCategory.MovementDestination, getFrogMovementDestinations(builder));

        return ResolverUtils.createPositionRequest(new InteractionContext(character), legalTargets);
    }

    @Override
    @Nullable
    public InteractionFeedback getNextFeedback(@NonNull CharacterActionBuilder builder) {
        if (!ResolverUtils.isFeedbackExpected(builder)) {
            return null;
        }
        return buildFrogMovementFeedback(builder.getResults().get(0));
    }

    /**
     * Returns all empty cells in a range of two tiles around the frog.
     *
     * <p>Each candidate is validated through the standard action validation
     * mechanism before being exposed to the player.</p>
     */
    @NonNull
    private List<Position> getFrogMovementDestinations(@NonNull CharacterActionBuilder builder) {
        List<Position> destinations = new ArrayList<>();

        Board board = game.getBoard();
        for (Cell cell : board.getCells().values()) {
            Position cellPosition = cell.getPosition();
            if (cell.getCharacter() == null && !characterPos.equals(cellPosition) &&
                    characterPos.distanceTo(cellPosition) <= 2) {

                InteractionResult result = ResolverUtils.createPositionResult(
                        new InteractionContext(character),
                        TargetCategory.MovementDestination, cellPosition
                );
                InteractionFeedback feedback = buildFrogMovementFeedback(result);

                if (isValidWith(builder, result, feedback)) {
                    destinations.add(cellPosition);
                }
            }
        }

        return destinations;
    }

    @NonNull
    private InteractionFeedback buildFrogMovementFeedback(@NonNull InteractionResult result) {
        Position destination = ResolverUtils.getChosenPosition(result);

        return ResolverUtils.createSingleMotionFeedback(CharacterMotionType.Jump, character, characterPos, destination);
    }
}
