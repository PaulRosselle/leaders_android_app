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
import com.leaders.gamelogic.interactions.InteractionTarget;
import com.leaders.gamelogic.interactions.TargetCategory;
import com.leaders.gamelogic.queries.BoardQuery;
import com.leaders.gamelogic.resolvers.CharacterActionResolver;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.leaders.gamelogic.resolvers.ResolverUtils.createPositionRequest;
import static com.leaders.gamelogic.resolvers.ResolverUtils.createPositionResult;
import static com.leaders.gamelogic.resolvers.ResolverUtils.isFeedbackExpected;

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

        return createPositionRequest(new InteractionContext(character), legalTargets);
    }

    @Override
    @Nullable
    public InteractionFeedback getNextFeedback(@NonNull CharacterActionBuilder builder) {
        if (!isFeedbackExpected(builder)) {
            return null;
        }

        InteractionResult result = builder.getResults().get(0);

        if (!isNormalMovementResult(result)) {
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

        List<CharacterPath> filteredPaths = filterValidPaths(builder, paths);
        // Prefer paths that avoid surrounding the Nemesis's leader.
        if (!filteredPaths.isEmpty()) {
            return filteredPaths;
        }

        // If none are available, she is forced to choose a path that will surround her leader.
        return paths;
    }

    @NonNull
    private List<CharacterPath> filterValidPaths(@NonNull CharacterActionBuilder builder,
                                                 @NonNull List<CharacterPath> paths) {
        List<CharacterPath> validPaths = new ArrayList<>();

        for (CharacterPath path : paths) {
            InteractionResult result = createPositionResult(new InteractionContext(character),
                    TargetCategory.MovementDestination, path.getDestination());

            if (isValidWith(builder, result, buildNormalMovementFeedback(path))) {
                validPaths.add(path);
            }
        }

        return validPaths;
    }
}