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
import java.util.Objects;
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

        for (Position destination : getValidNemesisMovementDestinations(builder)) {
            legalTargets.add(new InteractionTarget(TargetCategory.MovementDestination, destination));
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
                !builder.getFeedbacks().isEmpty()) {
            return null;
        }

        InteractionResult result = builder.getResults().get(0);

        if (result.getResultType() == InteractionResultType.CancelAction) {
            return null;
        }

        if (!isMovementResult(result)) {
            throw new IllegalArgumentException(
                    "Invalid Nemesis interaction type " + result.getResultType());
        }

        return buildMovementFeedback(result);
    }

    /**
     * Returns all valid destinations available to the Nemesis.
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
    private List<Position> getValidNemesisMovementDestinations(@NonNull CharacterActionBuilder builder) {
        Set<Position> secondStepDestinations = new HashSet<>();
        List<Position> firstStepDestinations = new ArrayList<>();

        for (Direction firstDirection : Direction.values()) {

            Cell firstStepCell = BoardQuery.findAdjacentCell(game.getBoard(), characterPos, firstDirection);
            if (firstStepCell != null && firstStepCell.getCharacter() == null) {

                firstStepDestinations.add(firstStepCell.getPosition());
                for (Direction secondDirection : Direction.values()) {
                    Cell secondStepCell = BoardQuery.findAdjacentCell(
                            game.getBoard(), firstStepCell.getPosition(), secondDirection);

                    if (secondStepCell != null && secondStepCell.getCharacter() == null) {
                        secondStepDestinations.add(secondStepCell.getPosition());
                    }
                }
            }
        }

        secondStepDestinations.remove(characterPos);

        if (!secondStepDestinations.isEmpty()) {
            return filterValidDestinations(builder, secondStepDestinations);
        }

        return filterValidDestinations(builder, firstStepDestinations);
    }

    @NonNull
    private List<Position> filterValidDestinations(@NonNull CharacterActionBuilder builder,
                                                   @NonNull Iterable<Position> destinations) {
        List<Position> validDestinations = new ArrayList<>();

        for (Position destPos : destinations) {
            CharacterActionBuilder destinationBuilder = new CharacterActionBuilder(builder);
            InteractionResult result = new InteractionResult(
                    InteractionResultType.PositionChosen,
                    new InteractionContext(character),
                    new InteractionTarget(TargetCategory.MovementDestination, destPos)
            );

            destinationBuilder.addResult(result);
            destinationBuilder.addFeedback(buildMovementFeedback(result));

            if (isActionValid(buildAction(destinationBuilder))) {
                validDestinations.add(destPos);
            }
        }

        return validDestinations;
    }

    @NonNull
    private InteractionFeedback buildMovementFeedback(@NonNull InteractionResult result) {
        Position destination = Objects.requireNonNull(
                Objects.requireNonNull(
                        result.getChosenTarget(),
                        "Nemesis movement interaction result invalid: no data"
                ).getChosenPosition(),
                "Nemesis movement interaction result invalid: no destination position"
        );

        return InteractionFeedback.createForCharacterAction(List.of(new CharacterActionMotion(
                CharacterMotionType.Move,
                List.of(new CharacterActionTarget(character, characterPos, destination))
        )));
    }

    private boolean isMovementResult(@NonNull InteractionResult result) {
        return result.getResultType() == InteractionResultType.PositionChosen &&
                result.getChosenTarget() != null &&
                result.getChosenTarget().getCategory() == TargetCategory.MovementDestination;
    }
}