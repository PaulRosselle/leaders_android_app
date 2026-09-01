package com.leaders.gamelogic.resolvers.characters;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.gamelogic.actions.CharacterActionMotion;
import com.leaders.gamelogic.actions.CharacterActionTarget;
import com.leaders.gamelogic.entities.Board;
import com.leaders.gamelogic.entities.Cell;
import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.entities.CharacterPath;
import com.leaders.gamelogic.entities.Game;
import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.gamelogic.entities.Position;
import com.leaders.gamelogic.enums.CharacterMotionType;
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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Resolves the Royal Guard's active ability.
 *
 * <p>The Royal Guard can move directly to an empty cell adjacent to its Leader.
 * After this movement, it may move one additional cell.</p>
 *
 * <p>The destination selected by the player represents the final position of
 * the Royal Guard. The intermediate position adjacent to the Leader is
 * determined automatically.</p>
 */
public final class RoyalGuardActionResolver extends CharacterActionResolver {

    public RoyalGuardActionResolver(@NonNull Game game, @NonNull GameHistory gameHistory,
                                    @NonNull Character character) {
        super(game, gameHistory, character);
    }

    @Override
    @Nullable
    public InteractionRequest getNextInteraction(@NonNull CharacterActionBuilder builder) {
        // Royal Guard actions require a single interaction.
        // If an interaction has already been selected, no further interaction can be added.
        if (!builder.getResults().isEmpty()) {
            return null;
        }

        List<InteractionTarget> legalTargets = new ArrayList<>();

        // Normal movement and Royal Guard ability destinations may overlap.
        // Normal movement has priority in that case.
        List<Position> movementDestinations = getNormalMovementValidDestinations(builder);

        for (Position destination : movementDestinations) {
            legalTargets.add(new InteractionTarget(TargetCategory.MovementDestination, destination));
        }


        if (CharacterAbilityQuery.canUseActiveAbility(game, character)) {
            for (CharacterPath path : getRoyalGuardAbilityPaths(builder)) {
                Position pathDestination = path.getDestination();
                if (!movementDestinations.contains(pathDestination)) {
                    legalTargets.add(new InteractionTarget(TargetCategory.ActiveAbilityDestination, pathDestination));
                }
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
                !builder.getFeedbacks().isEmpty() ||
                builder.isBuildCancelled()) {
            return null;
        }

        InteractionResult result = builder.getResults().get(0);

        InteractionFeedback feedback;
        if (isNormalMovementResult(result)) {
            feedback = super.getNextFeedback(builder);
        } else if (isRoyalGuardDestinationResult(result)){
            // We recover all legal paths using an empty builder to find one matching the result
            List<CharacterPath> legalPaths = getRoyalGuardAbilityPaths(
                    new CharacterActionBuilder(character, new ArrayList<>(), new ArrayList<>())
            );
            CharacterPath resultPath = getPathMatchingResult(result, legalPaths);
            feedback = buildRoyalGuardMovementFeedback(resultPath);
        } else {
            throw new IllegalArgumentException("Invalid Royal Guard interaction type " + result.getResultType());
        }
        return feedback;
    }

    /**
     * Returns all valid paths reachable through the Royal Guard's active ability.
     *
     * <p>The first movement must place the Royal Guard on an empty cell adjacent
     * to its Leader. From each such intermediate position, the Guard may then
     * move to an adjacent empty cell. Each resulting action is validated before
     * its final destination is exposed to the player.</p>
     */
    @NonNull
    private List<CharacterPath> getRoyalGuardAbilityPaths(@NonNull CharacterActionBuilder builder) {
        Board board = game.getBoard();
        Cell leaderCell = BoardQuery.findLeaderCell(board, character.getTeamColor());

        if (leaderCell == null) {
            return Collections.emptyList();
        }

        List<CharacterPath> paths = new ArrayList<>();
        Set<Position> destinations = new HashSet<>();

        // First movement: an empty cell adjacent to the Leader.
        for (Cell firstLayerCell : BoardQuery.findEmptyCellsAround(board, leaderCell.getPosition(), 1)) {
            Position firstLayerPos = firstLayerCell.getPosition();

            CharacterPath firstLayerPath = new CharacterPath(List.of(characterPos, firstLayerPos));
            if (isPathValid(builder, firstLayerPath)) {
                paths.add(firstLayerPath);
                destinations.add(firstLayerPath.getDestination());
            }

            // Second movement: any adjacent empty cell.
            for (Cell secondLayerCell : BoardQuery.findEmptyCellsAround(board, firstLayerPos, 1)) {
                Position secondLayerPos = secondLayerCell.getPosition();

                // Keep only the first valid path for each destination.
                if (destinations.contains(secondLayerPos)) {
                    continue;
                }

                CharacterPath secondLayerPath = new CharacterPath(
                        List.of(characterPos, firstLayerPos, secondLayerPos)
                );

                if (isPathValid(builder, secondLayerPath)) {
                    paths.add(secondLayerPath);
                    destinations.add(secondLayerPath.getDestination());
                }
            }
        }

        return paths;
    }

    private boolean isPathValid(@NonNull CharacterActionBuilder builder, @NonNull CharacterPath path) {
        CharacterActionBuilder pathBuilder = new CharacterActionBuilder(builder);

        InteractionResult result = new InteractionResult(
                InteractionResultType.PositionChosen,
                new InteractionContext(character),
                new InteractionTarget(TargetCategory.ActiveAbilityDestination, path.getDestination())
        );

        pathBuilder.addResult(result);
        pathBuilder.addFeedback(buildRoyalGuardMovementFeedback(path));

        return isActionValid(buildAction(pathBuilder));
    }

    @NonNull
    private InteractionFeedback buildRoyalGuardMovementFeedback(@NonNull CharacterPath path) {
        List<CharacterActionMotion> abilityMotions = new ArrayList<>();

        int stepsCount = path.getStepsCount();
        if (stepsCount > 2) {
            throw new IllegalArgumentException("Invalid royal guard path: path should not exceed two steps");
        }

        if (stepsCount > 1) {
            Position previousPos = path.getStart();
            for (Position position : path.getPositions()) {
                if (!position.equals(path.getStart())) {
                    CharacterMotionType motionType = position.equals(path.getDestination()) ?
                            CharacterMotionType.Move : CharacterMotionType.Teleport;
                    abilityMotions.add(new CharacterActionMotion(motionType,
                            List.of(new CharacterActionTarget(character, previousPos, position)))
                    );
                    previousPos = position;
                }
            }

        } else {
            abilityMotions.add(new CharacterActionMotion(CharacterMotionType.Teleport,
                    List.of(new CharacterActionTarget(character, path.getStart(), path.getDestination())))
            );
        }

        return InteractionFeedback.createForCharacterAction(abilityMotions);
    }

    private boolean isRoyalGuardDestinationResult(@NonNull InteractionResult result) {
        return result.getResultType() == InteractionResultType.PositionChosen &&
                result.getChosenTarget() != null &&
                result.getChosenTarget().getCategory() == TargetCategory.ActiveAbilityDestination;
    }
}