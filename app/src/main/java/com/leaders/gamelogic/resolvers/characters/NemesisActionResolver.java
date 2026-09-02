package com.leaders.gamelogic.resolvers.characters;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.entities.CharacterPath;
import com.leaders.gamelogic.entities.Game;
import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.gamelogic.entities.Position;
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
import com.leaders.gamelogic.resolvers.CharacterActionResolver;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Resolves the Nemesis' movement.
 *
 * <p>The Nemesis cannot perform the default movement action. Its movement is
 * replaced by a special movement of two cells. If no two-cell movement is
 * possible, it moves by one cell instead.</p>
 *
 * <p>When moving by two cells, the Nemesis cannot return to its original position.</p>
 */
public final class NemesisActionResolver extends CharacterActionResolver {

    public NemesisActionResolver(@NonNull Game game, @NonNull GameHistory gameHistory, @NonNull Character character) {
        super(game, gameHistory, character);
    }

    @Override
    @Nullable
    public InteractionRequest getNextInteraction(@NonNull CharacterActionBuilder builder) {
        if (!builder.getResults().isEmpty()) {
            return null;
        }

        List<InteractionTarget> legalTargets = new ArrayList<>();

        for (CharacterPath path : getValidNemesisMovementPaths(builder)) {
            legalTargets.add(new InteractionTarget(TargetCategory.MovementDestination, path.getDestination()));
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
                !builder.getFeedbacks().isEmpty() ||
                builder.isBuildCancelled()) {
            return null;
        }

        InteractionResult result = builder.getResults().get(0);

        if (!isMovementResult(result)) {
            throw new IllegalArgumentException(
                    "Invalid Nemesis interaction type " + result.getResultType());
        }

        // We recover all legal paths using an empty builder to find one matching the result
        List<CharacterPath> legalPaths = getValidNemesisMovementPaths(new CharacterActionBuilder(character));
        CharacterPath resultPath = getPathMatchingResult(result, legalPaths);

        return buildNormalMovementFeedback(resultPath);
    }

    /**
     * Returns all valid paths available to the Nemesis.
     *
     * <p>The Nemesis first attempts to move by two cells. A two-cell movement
     * requires both the intermediate and final cells to be empty. If at least
     * one such destination exists, only these destinations are returned.</p>
     *
     * <p>If no two-cell movement is possible, all empty cells adjacent to the
     * Nemesis are returned instead.</p>
     *
     * <p>The original position is excluded from two-cell destinations because
     * the Nemesis must end on a different cell when moving by two cells.</p>
     */
    @NonNull
    private List<CharacterPath> getValidNemesisMovementPaths(@NonNull CharacterActionBuilder builder) {
        List<CharacterPath> allPaths = BoardQuery.getEmptyPathsAround(
                game.getBoard(), characterPos, 2, false
        );

        List<CharacterPath> pathsAtTwo = getValidPathsAtDistance(builder, allPaths, 2);
        if (!pathsAtTwo.isEmpty()) {
            return pathsAtTwo;
        }

        return getValidPathsAtDistance(builder, allPaths, 1);
    }

    private List<CharacterPath> getValidPathsAtDistance(@NonNull CharacterActionBuilder builder,
                                                        @NonNull List<CharacterPath> allPaths,
                                                        int distance) {
        Set<Position> destinations = new HashSet<>();
        List<CharacterPath> paths = new ArrayList<>();

        for (CharacterPath path : allPaths) {
            if (path.getStepsCount() == distance &&
                    !path.getDestination().equals(characterPos) &&
                    destinations.add(path.getDestination())) {
                paths.add(path);
            }
        }

        return filterValidPaths(builder, paths);
    }

    @NonNull
    private List<CharacterPath> filterValidPaths(@NonNull CharacterActionBuilder builder,
                                                 @NonNull List<CharacterPath> paths) {
        List<CharacterPath> validPaths = new ArrayList<>();

        for (CharacterPath path : paths) {
            CharacterActionBuilder destinationBuilder = new CharacterActionBuilder(builder);
            InteractionResult result = new InteractionResult(
                    InteractionResultType.PositionChosen,
                    new InteractionContext(character),
                    new InteractionTarget(TargetCategory.MovementDestination, path.getDestination())
            );

            destinationBuilder.addResult(result);
            destinationBuilder.addFeedback(buildNormalMovementFeedback(path));

            if (isActionValid(buildAction(destinationBuilder))) {
                validPaths.add(path);
            }
        }

        return validPaths;
    }

    private boolean isMovementResult(@NonNull InteractionResult result) {
        return result.getResultType() == InteractionResultType.PositionChosen &&
                result.getChosenTarget() != null &&
                result.getChosenTarget().getCategory() == TargetCategory.MovementDestination;
    }
}