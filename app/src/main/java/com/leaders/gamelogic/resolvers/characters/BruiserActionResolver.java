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
import java.util.List;
import java.util.Objects;

/**
 * Resolves the Bruiser's active ability.
 *
 * <p>The Bruiser moves onto the cell occupied by an adjacent enemy and pushes
 * that enemy onto one of the three cells opposite to the Bruiser's origin.</p>
 *
 * <p>The ability requires the player to first select an adjacent enemy, then
 * select a valid destination for the pushed enemy.</p>
 */
public final class BruiserActionResolver extends CharacterActionResolver {

    public BruiserActionResolver(@NonNull Game game, @NonNull GameHistory gameHistory, @NonNull Character character) {
        super(game, gameHistory, character);
    }

    @Override
    @Nullable
    public InteractionRequest getNextInteraction(@NonNull CharacterActionBuilder builder) {
        // For the first interaction, both normal movement and ability activation are possible
        if (builder.getResults().isEmpty()) {
            return buildInitialInteraction(builder);
        }

        InteractionResult firstResult = builder.getResults().get(0);

        if (firstResult.getResultType() == InteractionResultType.CancelAction) {
            return null;
        }

        if (isNormalMovementResult(firstResult)) {
            return super.getNextInteraction(builder);
        }

        if (isBruiserTargetResult(firstResult)) {
            if (builder.getResults().size() > 1) {
                if (!isBruiserPushDestinationResult(builder.getResults().get(1))) {
                    throw new IllegalArgumentException("Invalid result type for a Bruiser ability activation");
                }
                return null;
            } else {
                return buildPushDestinationInteraction(builder, firstResult);
            }
        }

        throw new IllegalArgumentException("Invalid Bruiser action builder");
    }

    @Override
    @Nullable
    public InteractionFeedback getNextFeedback(@NonNull CharacterActionBuilder builder) {
        if (!builder.getFeedbacks().isEmpty()) {
            return null;
        }

        if (builder.getResults().isEmpty()) {
            return null;
        }

        InteractionResult firstResult = builder.getResults().get(0);

        if (firstResult.getResultType() == InteractionResultType.CancelAction) {
            return null;
        }

        if (isNormalMovementResult(firstResult)) {
            return super.getNextFeedback(builder);
        }

        if (builder.getResults().size() < 2) {
            return null;
        }

        InteractionResult destinationResult = builder.getResults().get(1);

        if (!isBruiserTargetResult(firstResult) ||
                !isBruiserPushDestinationResult(destinationResult)) {
            throw new IllegalArgumentException("Invalid result types for a Bruiser ability activation");
        }

        Position targetOrigin = Objects.requireNonNull(
                Objects.requireNonNull(firstResult.getChosenTarget(),
                                "Bruiser target interaction result invalid: no data")
                        .getChosenPosition(),
                "Bruiser target interaction result invalid: no target position");

        Position destination = Objects.requireNonNull(
                Objects.requireNonNull(destinationResult.getChosenTarget(),
                                "Bruiser destination interaction result invalid: no data")
                        .getChosenPosition(),
                "Bruiser destination interaction result invalid: no destination");

        Character target = Objects.requireNonNull(
                game.getBoard().getCell(targetOrigin).getCharacter(),
                "Bruiser target position should contain a character");

        return InteractionFeedback.createForCharacterAction(List.of(new CharacterActionMotion(
                CharacterMotionType.Push,
                List.of(
                        new CharacterActionTarget(character, characterPos, targetOrigin),
                        new CharacterActionTarget(target, targetOrigin, destination)
                )
        )));
    }

    @NonNull
    private InteractionRequest buildInitialInteraction(@NonNull CharacterActionBuilder builder) {
        List<InteractionTarget> legalTargets = new ArrayList<>();

        for (Position destination : getNormalMovementValidDestinations(builder)) {
            legalTargets.add(new InteractionTarget(TargetCategory.MovementDestination, destination));
        }

        for (Position enemyPosition : getAdjacentEnemyPositions()) {
            if (!getValidPushDestinations(builder, enemyPosition, true).isEmpty()) {
                legalTargets.add(new InteractionTarget(TargetCategory.ActiveAbilityTargetPosition, enemyPosition));
            }
        }

        return new InteractionRequest(
                InteractionType.PositionExpected,
                new InteractionContext(character),
                legalTargets,
                List.of(InteractionResultType.PositionChosen, InteractionResultType.CancelAction)
        );
    }

    @NonNull
    private InteractionRequest buildPushDestinationInteraction(@NonNull CharacterActionBuilder builder,
                                                               @NonNull InteractionResult result) {
        if (!isBruiserTargetResult(result)) {
            throw new IllegalArgumentException("A Bruiser ability target must be selected through a PositionChosen result");
        }

        Position targetPos = Objects.requireNonNull(Objects.requireNonNull(result.getChosenTarget(),
                        "Bruiser target interaction invalid : no data")
                        .getChosenPosition(),
                "Bruiser target interaction invalid : no target position");

        List<InteractionTarget> legalTargets = new ArrayList<>();

        for (Position destination : getValidPushDestinations(builder, targetPos, false)) {
            legalTargets.add(new InteractionTarget(TargetCategory.ActiveAbilityDestination, destination));
        }

        Character targetedEnemy = Objects.requireNonNull(game.getBoard().getCell(targetPos).getCharacter(),
                "A Bruiser target position must contain an enemy");
        return new InteractionRequest(
                InteractionType.PositionExpected,
                new InteractionContext(targetedEnemy), // Here the request has for subjet the target
                legalTargets,
                List.of(InteractionResultType.PositionChosen, InteractionResultType.CancelAction)
        );
    }

    /**
     * Returns the positions of all enemies adjacent to the Bruiser.
     *
     * <p>Only enemy characters are considered as valid targets for the active
     * ability.</p>
     */
    @NonNull
    private List<Position> getAdjacentEnemyPositions() {
        List<Position> enemies = new ArrayList<>();

        for (Direction direction : Direction.values()) {
            Cell adjacentCell = BoardQuery.findAdjacentCell(game.getBoard(), characterPos, direction);
            if (adjacentCell != null) {
                Character target = adjacentCell.getCharacter();
                if (target != null && target.getTeamColor() != character.getTeamColor()) {
                    enemies.add(adjacentCell.getPosition());
                }
            }
        }

        return enemies;
    }

    /**
     * Returns the valid destinations to which the selected enemy can be pushed.
     *
     * <p>The Bruiser can push the target onto any of the three cells opposite
     * to the Bruiser relative to the target's current position. The cell
     * corresponding to the Bruiser's current position is therefore excluded.</p>
     *
     * <p>Each candidate destination is validated by building the corresponding
     * temporary action and passing it through the standard action validation.</p>
     *
     * @param builder current action builder
     * @param targetPos current position of the enemy targeted by the ability
     * @param exitAfterFirstValidDest whether the search can stop after finding
     *                                the first valid destination
     */
    @NonNull
    private List<Position> getValidPushDestinations(@NonNull CharacterActionBuilder builder,
                                                    @NonNull Position targetPos,
                                                    boolean exitAfterFirstValidDest) {
        Character target = Objects.requireNonNull(game.getBoard().getCell(targetPos).getCharacter(),
                "A cell targeted by the Bruiser's ability should always contain a character");

        Direction straightPushDirection = findStraightPushDirection(targetPos);

        List<Direction> pushDirections = List.of(
                straightPushDirection.getNext(false),
                straightPushDirection,
                straightPushDirection.getNext(true)
        );

        // If the target result is missing, we add it before entering the destination loop
        CharacterActionBuilder targetBuilder = new CharacterActionBuilder(builder);
        if (builder.getResults().isEmpty()) {
            targetBuilder.addResult(new InteractionResult(
                    InteractionResultType.PositionChosen,
                    new InteractionContext(character),
                    new InteractionTarget(TargetCategory.ActiveAbilityTargetPosition, targetPos)
            ));
        }


        List<Position> destinations = new ArrayList<>();
        for (Direction direction : pushDirections) {
            Cell pushCell = BoardQuery.findAdjacentCell(game.getBoard(), targetPos, direction);
            if (pushCell != null && pushCell.getCharacter() == null) {
                Position pushDestPos = pushCell.getPosition();

                CharacterActionBuilder destinationBuilder = new CharacterActionBuilder(targetBuilder);
                destinationBuilder.addResult(new InteractionResult(
                        InteractionResultType.PositionChosen,
                        new InteractionContext(target), // Here the result subject is the target
                        new InteractionTarget(TargetCategory.ActiveAbilityDestination, pushDestPos)
                ));

                destinationBuilder.addFeedback(InteractionFeedback.createForCharacterAction(
                        List.of(new CharacterActionMotion(
                                CharacterMotionType.Push,
                                List.of(
                                        new CharacterActionTarget(character, characterPos, targetPos),
                                        new CharacterActionTarget(target, targetPos, pushDestPos)
                                ))
                        ))
                );

                if (isActionValid(buildAction(destinationBuilder))) {
                    destinations.add(pushDestPos);

                    if (exitAfterFirstValidDest) {
                        return destinations;
                    }
                }
            }
        }

        return destinations;
    }

    @NonNull
    private Direction findStraightPushDirection(@NonNull Position targetPos) {
        for (Direction direction : Direction.values()) {
            if (targetPos.equals(characterPos.adjacent(direction))) {
                return direction;
            }
        }

        throw new IllegalArgumentException("Target position is not adjacent to the Bruiser");
    }

    private boolean isBruiserTargetResult(@NonNull InteractionResult result) {
        return result.getResultType() == InteractionResultType.PositionChosen &&
                result.getChosenTarget() != null &&
                result.getChosenTarget().getCategory() == TargetCategory.ActiveAbilityTargetPosition;
    }

    private boolean isBruiserPushDestinationResult(@NonNull InteractionResult result) {
        return result.getResultType() == InteractionResultType.PositionChosen &&
                result.getChosenTarget() != null &&
                result.getChosenTarget().getCategory() == TargetCategory.ActiveAbilityDestination;
    }
}
