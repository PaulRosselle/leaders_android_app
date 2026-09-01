package com.leaders.gamelogic.resolvers;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.gamelogic.actions.CharacterAction;
import com.leaders.gamelogic.actions.CharacterActionMotion;
import com.leaders.gamelogic.actions.CharacterActionTarget;
import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.entities.CharacterPath;
import com.leaders.gamelogic.entities.Game;
import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.gamelogic.entities.Position;
import com.leaders.gamelogic.enums.CharacterMotionType;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.gamelogic.factories.GameActionHandlerFactory;
import com.leaders.gamelogic.handlers.GameActionHandler;
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
import com.leaders.gamelogic.queries.GameQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Base class responsible for resolving the interaction flow required to build
 * and validate a {@link CharacterAction}.
 *
 * <p>A resolver defines how a character action is progressively constructed
 * through player interactions. Implementations may override
 * {@link #getNextInteraction(CharacterActionBuilder)} to provide custom
 * interaction flows depending on the character abilities and action rules.</p>
 */
public class CharacterActionResolver {
    @NonNull
    protected final Game game;
    @NonNull
    protected final GameHistory gameHistory;

    @NonNull
    protected final Character character;

    @NonNull
    protected final Position characterPos;

    /**
     * Creates a resolver for a specific character in a given game context.
     *
     * @param game current game state used to evaluate and simulate actions
     * @param gameHistory history of previous game events and actions
     * @param character character whose action is being resolved
     */
    public CharacterActionResolver(@NonNull Game game, @NonNull GameHistory gameHistory, @NonNull Character character) {
        this.game = game;
        this.gameHistory = gameHistory;
        this.character = character;
        this.characterPos = BoardQuery.getCellByCharacterId(game.getBoard(), character.getId()).getPosition();
    }

    /**
     * Checks whether an action can legally be performed in the current game state.
     *
     * <p>The action is temporarily applied to the current game projection in
     * order to evaluate the resulting state. The projection is then restored
     * regardless of the validation result.</p>
     *
     * <p>An action is considered invalid if it results in the capture or the
     * surrounding of the acting team's leader.</p>
     *
     * @param action action to validate
     * @return {@code true} if the action respects all game constraints,
     *         {@code false} otherwise
     */
    protected boolean isActionValid(@NonNull CharacterAction action) {
        TeamColor teamColor = character.getTeamColor();
        GameActionHandler actionHandler = GameActionHandlerFactory.create(game, action);
        try {
            // The action is applied temporarily to evaluate its consequences.
            // Validation is performed against the resulting game state.
            actionHandler.doAction();
            return !GameQuery.isLeaderCaptured(game, teamColor)
                    && !GameQuery.isLeaderSurrounded(game, teamColor);
        } finally {
            // The temporary state modification must always be reverted in order
            // to keep the game projection unchanged after validation.
            actionHandler.undoAction();
        }
    }

    /**
     * Computes all destinations reachable through a normal movement action and
     * removes those that would result in an invalid game state.
     *
     * <p>Each possible destination is tested by creating a temporary action
     * containing the corresponding position choice, then validating the action
     * against the resulting game state.</p>
     *
     * @param builder current state of the action being constructed
     * @return list of destinations that can legally be selected
     */
    @NonNull
    protected List<Position> getNormalMovementValidDestinations(@NonNull CharacterActionBuilder builder) {
        List<Position> destPositions = new ArrayList<>();
        for (CharacterPath path : CharacterAbilityQuery.getNormalMovementPaths(game, character)) {
            Position destPos = path.getDestination();
            // Create a temporary action builder containing the tested position choice.
            // This allows the resulting action to be validated before exposing the
            // destination as a legal interaction option.
            CharacterActionBuilder nextMovementBuilder = new CharacterActionBuilder(builder);
            nextMovementBuilder.addResult(new InteractionResult(
                    InteractionResultType.PositionChosen,
                    new InteractionContext(character),
                    new InteractionTarget(TargetCategory.MovementDestination, destPos))
            );
            nextMovementBuilder.addFeedback(InteractionFeedback.createForCharacterAction(
                    List.of(new CharacterActionMotion(
                            CharacterMotionType.Move,
                            List.of(new CharacterActionTarget(character,
                                    characterPos,
                                    destPos)
                            ))
                    )
            ));

            CharacterAction movementAction = buildAction(nextMovementBuilder);
            if (isActionValid(movementAction)) {
                destPositions.add(destPos);
            }
        }
        return destPositions;
    }

    /**
     * Retrieves the next interaction required to continue building a character
     * action.
     *
     * <p>This implementation provides the default movement behavior:
     * the player must select a valid destination position. An ongoing action
     * cannot be extended after this interaction.</p>
     *
     * <p>Subclasses may override this method to implement custom interaction
     * sequences such as ability selection, target selection, chained effects,
     * or conditional interactions.</p>
     *
     * @param builder current state of the action being constructed
     * @return the next interaction request, or {@code null} if no interaction
     *         is required or possible
     */
    @Nullable
    public InteractionRequest getNextInteraction(@NonNull CharacterActionBuilder builder) {
        // The default movement action only requires a single interaction.
        // If an interaction has already been selected, no further interaction can be added to this action.
        if (!builder.getResults().isEmpty()) {
            return null;
        }

        // Build the list of legal movement destinations.
        // Invalid destinations are filtered out before being exposed to the interaction layer.
        List<InteractionTarget> legalTargets = new ArrayList<>();
        for (Position destination : getNormalMovementValidDestinations(builder)) {
            legalTargets.add(new InteractionTarget(TargetCategory.MovementDestination, destination));
        }

        // A movement action requires the player to choose a position.
        // Cancelling remains available while the action is being built.
        List<InteractionResultType> legalResults = new ArrayList<>();
        legalResults.add(InteractionResultType.PositionChosen);
        legalResults.add(InteractionResultType.CancelAction);

        return new InteractionRequest(InteractionType.PositionExpected,
                new InteractionContext(character),
                legalTargets, legalResults);
    }

    /**
     * Returns the next feedback generated from the current interaction state.
     * <p>
     * Each resolver implementation is responsible for translating its own
     * interaction result flow into a concrete {@link InteractionFeedback} instance.</p>
     *
     * @param builder builder containing the current interaction state
     * @return the next feedback, or {@code null} if none is available
     */
    @Nullable
    public InteractionFeedback getNextFeedback(@NonNull CharacterActionBuilder builder) {
        // The default movement action only requires a single interaction.
        // When the result is gotten, a single feedback can be generated containing the movement instructions.
        // No feedback is generated after an action cancellation for the default movement
        if (builder.getResults().size() != 1 ||
                !builder.getFeedbacks().isEmpty() ||
                builder.isBuildCancelled()) {
            return null;
        }

        InteractionResult result = builder.getResults().get(0);

        if (!isNormalMovementResult(result)) {
            throw new IllegalArgumentException("The default action resolver only handles normal movement");
        }

        List<CharacterPath> paths = CharacterAbilityQuery.getNormalMovementPaths(game, character);
        return buildNormalMovementFeedback(getPathMatchingResult(result, paths));
    }

    /**
     * Builds the final action represented by the current interaction state.
     *
     * <p>Each resolver implementation is responsible for translating its own
     * interaction flow into a concrete {@link CharacterAction} instance.</p>
     *
     * @param builder builder containing the selected interaction results
     * @return the action corresponding to the current builder state
     */
    @NonNull
    public CharacterAction buildAction(@NonNull CharacterActionBuilder builder) {
        List<CharacterActionMotion> characterActionMotions = new ArrayList<>();
        for (InteractionFeedback feedback : builder.getFeedbacks()) {
            characterActionMotions.addAll(feedback.getCharacterActionMotions());
        }
        return new CharacterAction(builder.getSourceCharacter(), characterActionMotions);
    }

    @NonNull
    protected final CharacterPath getPathMatchingResult(@NonNull InteractionResult result,
                                                        @NonNull List<CharacterPath> paths) {
        Position destPos = Objects.requireNonNull(
                Objects.requireNonNull(
                        result.getChosenTarget(),
                        "Invalid Path interaction result: no data"
                ).getChosenPosition(),
                "Invalid Path interaction result: no destination position"
        );

        // We search for the shortest path matching the result
        CharacterPath bestMatchingPath = null;
        for (CharacterPath path : paths) {
            if (path.getDestination().equals(destPos) &&
                    (bestMatchingPath == null ||
                            bestMatchingPath.getPositions().size() > path.getPositions().size())) {
                bestMatchingPath = path;
            }
        }

        if (bestMatchingPath == null) {
            throw new IllegalArgumentException("No path found matching result: " + result);
        }

        return bestMatchingPath;
    }


    @NonNull
    private InteractionFeedback buildNormalMovementFeedback(@NonNull CharacterPath path) {
        List<CharacterActionMotion> motions = new ArrayList<>();

        List<Position> pathPositions = path.getPositions();
        if (pathPositions.size() > 3) {
            throw new IllegalArgumentException("Invalid normal movement path: path should not exceed two steps");
        }

        Position previousPos = path.getStart();
        for (Position position : pathPositions) {
            if (!position.equals(path.getStart())) {
                motions.add(new CharacterActionMotion(CharacterMotionType.Move,
                        List.of(new CharacterActionTarget(character, previousPos, position)))
                );
                previousPos = position;
            }
        }

        return InteractionFeedback.createForCharacterAction(motions);
    }

    protected boolean isNormalMovementResult(@NonNull InteractionResult result) {
        return result.getResultType() == InteractionResultType.PositionChosen &&
                result.getChosenTarget() != null &&
                result.getChosenTarget().getCategory() == TargetCategory.MovementDestination;
    }
}
