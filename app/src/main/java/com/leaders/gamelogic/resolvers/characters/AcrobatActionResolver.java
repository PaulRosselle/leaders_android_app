package com.leaders.gamelogic.resolvers.characters;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.gamelogic.actions.CharacterActionMotion;
import com.leaders.gamelogic.actions.CharacterActionTarget;
import com.leaders.gamelogic.entities.Cell;
import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.entities.CharacterPath;
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
import com.leaders.gamelogic.queries.CharacterAbilityQuery;
import com.leaders.gamelogic.resolvers.CharacterActionResolver;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class AcrobatActionResolver extends CharacterActionResolver {

    /**
     * Resolves the Acrobat's active ability.
     *
     * <p>The Acrobat can jump over an adjacent character and land on the
     * following empty cell. It can perform up to two consecutive jumps.</p>
     *
     * @param game current game projection
     * @param gameHistory current game history
     * @param character Acrobat being resolved
     */
    public AcrobatActionResolver(@NonNull Game game, @NonNull GameHistory gameHistory, @NonNull Character character) {
        super(game, gameHistory, character);
    }

    @Override
    @Nullable
    public InteractionRequest getNextInteraction(@NonNull CharacterActionBuilder builder) {
        // If the action builder contains already a default movement interaction,
        // we leave the interaction to be handled by the parent default movement resolver
        if (!builder.getResults().isEmpty()) {
            if (isNormalMovementResult(builder.getResults().get(0))) {
                return super.getNextInteraction(builder);
            }
            return null;
        }

        // Normal movement and Acrobat jump destinations cannot overlap:
        // a normal move requires an adjacent cell, while a jump destination is at least two cells away.
        // For this reason, we can add both destinations without further treatment to avoid duplicates
        List<InteractionTarget> legalTargets = new ArrayList<>();
        for (Position movementDest : getNormalMovementValidDestinations(builder)) {
            legalTargets.add(new InteractionTarget(TargetCategory.MovementDestination, movementDest));
        }
        if (CharacterAbilityQuery.canUseActiveAbility(game, character)) {
            for (CharacterPath jumpPaths : getAcrobatJumpPaths(builder)) {
                legalTargets.add(new InteractionTarget(TargetCategory.ActiveAbilityDestination, jumpPaths.getDestination()));
            }
        }

        return new InteractionRequest(InteractionType.PositionExpected,
                new InteractionContext(character), legalTargets,
                List.of(InteractionResultType.PositionChosen, InteractionResultType.CancelAction));
    }

    @Override
    @Nullable
    public InteractionFeedback getNextFeedback(@NonNull CharacterActionBuilder builder) {
        // Acrobat actions only requires a single interaction.
        // When the result is gotten, a single feedback can be generated
        // containing either movement or active ability instructions
        if (builder.getResults().size() != 1 ||
                !builder.getFeedbacks().isEmpty() ||
                builder.isBuildCancelled()) {
            return null;
        }

        InteractionResult result = builder.getResults().get(0);

        InteractionFeedback feedback;
        if (isNormalMovementResult(result)) {
            feedback = super.getNextFeedback(builder);
        } else if (isInteractionResultAcrobatJump(result)){
            // We recover all legal paths using an empty builder to find one matching the result
            List<CharacterPath> legalPaths = getAcrobatJumpPaths(new CharacterActionBuilder(character));
            CharacterPath resultPath = getPathMatchingResult(result, legalPaths);
            feedback = buildAcrobatJumpFeedback(resultPath);
        } else {
            throw new IllegalArgumentException("Invalid Acrobat interaction type " + result.getResultType());
        }
        return feedback;
    }

    private boolean isInteractionResultAcrobatJump(@NonNull InteractionResult result) {
        return result.getResultType() == InteractionResultType.PositionChosen &&
                result.getChosenTarget() != null &&
                result.getChosenTarget().getCategory() == TargetCategory.ActiveAbilityDestination;
    }

    @NonNull
    private List<CharacterPath> getAcrobatJumpPaths(@NonNull CharacterActionBuilder builder) {
        // First we find every jump path
        List<CharacterPath> jumpPaths = new ArrayList<>();
        Set<Position> destinations = new HashSet<>();

        for (Direction firstDirection : Direction.values()) {
            Cell firstJumpDestination = findJumpDestination(characterPos, firstDirection);
            if (firstJumpDestination != null) {
                Position firstJumpPos = firstJumpDestination.getPosition();
                // First jump paths
                if (!destinations.contains(firstJumpPos)) {
                    jumpPaths.add(new CharacterPath(List.of(characterPos, firstJumpPos)));
                    destinations.add(firstJumpPos);
                }

                for (Direction secondDirection : Direction.values()) {
                    if (secondDirection != firstDirection.getOpposite()) {
                        Cell secondJumpDestination = findJumpDestination(firstJumpPos, secondDirection);
                        // Second jump paths
                        if (secondJumpDestination != null) {
                            Position secondJumpPos = secondJumpDestination.getPosition();
                            if (!destinations.contains(secondJumpPos)) {
                                jumpPaths.add(new CharacterPath(List.of(characterPos, firstJumpPos, secondJumpPos)));
                                destinations.add(secondJumpPos);
                            }
                        }
                    }
                }
            }
        }

        // Then we filter out every invalid destination
        for (int i = jumpPaths.size() - 1; i >= 0; i--) {
            CharacterPath jumpPath = jumpPaths.get(i);

            CharacterActionBuilder jumpBuilder = new CharacterActionBuilder(builder);
            InteractionResult jumpResult = new InteractionResult(InteractionResultType.PositionChosen,
                    new InteractionContext(character),
                    new InteractionTarget(TargetCategory.ActiveAbilityDestination, jumpPath.getDestination())
            );
            jumpBuilder.addResult(jumpResult);
            jumpBuilder.addFeedback(buildAcrobatJumpFeedback(jumpPath));

            if (!isActionValid(buildAction(jumpBuilder))) {
                jumpPaths.remove(i);
            }
        }

        return jumpPaths;
    }

    @Nullable
    private Cell findJumpDestination(@NonNull Position originPos, @NonNull Direction direction) {
        // The Acrobat can jump over non-empty cells. The jump destination cell must be empty
        Cell adjacentCell = BoardQuery.findAdjacentCell(game.getBoard(), originPos, direction);
        if (adjacentCell == null || adjacentCell.getCharacter() == null) {
            return null;
        }

        Cell jumpDestination = BoardQuery.findAdjacentCell(game.getBoard(), adjacentCell.getPosition(), direction);
        if (jumpDestination == null || jumpDestination.getCharacter() != null) {
            return null;
        }

        return jumpDestination;
    }

    @NonNull
    private InteractionFeedback buildAcrobatJumpFeedback(@NonNull CharacterPath jumpPath) {
        List<CharacterActionMotion> jumpMotions = new ArrayList<>();

        if (jumpPath.getStepsCount() > 2) {
            throw new IllegalArgumentException("Invalid Acrobat jump path: path should not exceed two steps");
        }

        Position previousPos = jumpPath.getStart();
        for (Position position : jumpPath.getPositions()) {
            if (!position.equals(jumpPath.getStart())) {
                jumpMotions.add(new CharacterActionMotion(CharacterMotionType.Jump,
                        List.of(new CharacterActionTarget(character, previousPos, position)))
                );
                previousPos = position;
            }
        }

        return InteractionFeedback.createForCharacterAction(jumpMotions);
    }
}
