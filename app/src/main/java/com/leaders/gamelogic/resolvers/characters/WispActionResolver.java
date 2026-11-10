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
import com.leaders.gamelogic.resolvers.CharacterActionResolver;
import com.leaders.gamelogic.resolvers.ResolverUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Resolves the Wisp's special ability.
 *
 * <p>The Wisp can use his normal movement to fly to any empty cell.</p>
 *
 * <p>The destination is selected directly by the player through a single
 * interaction. The selected destination is then used to build the movement
 * feedback.</p>
 */
public final class WispActionResolver extends CharacterActionResolver {
    public WispActionResolver(@NonNull Game game, @NonNull GameHistory gameHistory, @NonNull Character character) {
        super(game, gameHistory, character);
    }

    @Override
    @Nullable
    public InteractionRequest getNextInteraction(@NonNull CharacterActionBuilder builder) {
        // Wisp actions require a single interaction.
        // If an interaction has already been selected, no further interaction can be added to this action.
        if (!builder.getResults().isEmpty()) {
            return null;
        }

        List<InteractionTarget> legalTargets = getWispFlightDestinations(builder).stream()
                .map(dest -> new InteractionTarget(TargetCategory.MovementDestination, dest))
                .collect(Collectors.toList());

        return ResolverUtils.createPositionRequest(new InteractionContext(character), legalTargets);
    }

    @Override
    @Nullable
    public InteractionFeedback getNextFeedback(@NonNull CharacterActionBuilder builder) {
        if (!ResolverUtils.isFeedbackExpected(builder)) {
            return null;
        }

        return buildWispMovementFeedback(builder.getResults().get(0));
    }

    /**
     * Returns all empty cells.
     *
     * <p>Each candidate is validated through the standard action validation
     * mechanism before being exposed to the player.</p>
     */
    @NonNull
    private List<Position> getWispFlightDestinations(@NonNull CharacterActionBuilder builder) {
        List<Position> destinations = new ArrayList<>();

        for (Cell cell : game.getBoard().getCells().values()) {
            if (cell.getCharacter() == null) {
                Position destPos = cell.getPosition();

                InteractionResult result = ResolverUtils.createPositionResult(
                        new InteractionContext(character),
                        TargetCategory.MovementDestination, destPos
                );
                InteractionFeedback feedback = buildWispMovementFeedback(result);

                if (isValidWith(builder, result, feedback)) {
                    destinations.add(destPos);
                }
            }
        }

        return destinations;
    }


    @NonNull
    private InteractionFeedback buildWispMovementFeedback(@NonNull InteractionResult result) {
        Position destination = ResolverUtils.getChosenPosition(result);

        return ResolverUtils.createSingleMotionFeedback(CharacterMotionType.Fly, character, characterPos, destination);
    }
}
