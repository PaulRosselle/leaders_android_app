package com.leaders.gamelogic.resolvers;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.gamelogic.actions.RecruitmentAction;
import com.leaders.gamelogic.actions.RecruitmentActionMotion;
import com.leaders.gamelogic.entities.Cell;
import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.entities.Game;
import com.leaders.gamelogic.entities.Position;
import com.leaders.gamelogic.enums.CharacterCard;
import com.leaders.gamelogic.enums.CharacterType;
import com.leaders.gamelogic.enums.RecruitmentMotionType;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.gamelogic.factories.GameActionHandlerFactory;
import com.leaders.gamelogic.handlers.GameActionHandler;
import com.leaders.gamelogic.interactions.InteractionContext;
import com.leaders.gamelogic.interactions.InteractionFeedback;
import com.leaders.gamelogic.interactions.InteractionRequest;
import com.leaders.gamelogic.interactions.InteractionResult;
import com.leaders.gamelogic.interactions.InteractionResultType;
import com.leaders.gamelogic.interactions.InteractionTarget;
import com.leaders.gamelogic.interactions.InteractionType;
import com.leaders.gamelogic.interactions.RecruitmentActionBuilder;
import com.leaders.gamelogic.interactions.TargetCategory;
import com.leaders.gamelogic.queries.BoardQuery;
import com.leaders.gamelogic.queries.GameQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class RecruitmentActionResolver {

    @NonNull
    private final Game game;


    @NonNull
    private final CharacterCard recruitedCard;

    @NonNull
    private final TeamColor teamColor;

    public RecruitmentActionResolver(@NonNull Game game,
                                     @NonNull CharacterCard recruitedCard,
                                     @NonNull TeamColor teamColor) {
        this.game = game;
        this.recruitedCard = recruitedCard;
        this.teamColor = teamColor;
    }

    /**
     * Returns the next interaction required to complete the recruitment.
     *
     * <p>Each character associated with the recruited card must be assigned a
     * valid recruitment position. The interaction allows the user to choose
     * among the positions where the current character can legally be recruited,
     * or to cancel the recruitment.</p>
     *
     * <p>No further interaction is returned once all characters associated with
     * the card have been processed or the recruitment has been cancelled.</p>
     *
     * @param builder the builder containing the results and feedbacks already
     *                produced for this recruitment
     * @return the next interaction required to continue the recruitment, or
     *         {@code null} if no further interaction is required
     */
    @Nullable
    public InteractionRequest getNextInteraction(@NonNull RecruitmentActionBuilder builder) {
        List<InteractionResult> results = builder.getResults();
        if (!results.isEmpty()) {
            // If the recruitment was canceled, there is no next interaction
            for (InteractionResult result : results) {
                if (result.getResultType() == InteractionResultType.CancelAction) {
                    return null;
                }
            }
        }

        // If every character matching the card has been recruited, there is no next interaction
        List<CharacterType> cardCharacters = CharacterType.getCharacterTypesMatchingCard(recruitedCard);
        if (results.size() >= cardCharacters.size()) {
            return null;
        }
        Character recruitedCharacter = Character.create(cardCharacters.get(results.size()), teamColor);

        // We get every valid recruitment destination for the user to choose from
        List<InteractionTarget> legalTargets = new ArrayList<>();
        for (Position recruitmentPos : getValidRecruitmentPositions(builder, recruitedCharacter)) {
            legalTargets.add(new InteractionTarget(TargetCategory.RecruitmentDestination, recruitmentPos));
        }

        List<InteractionResultType> legalResults = new ArrayList<>();
        legalResults.add(InteractionResultType.PositionChosen);
        legalResults.add(InteractionResultType.CancelAction);

        return new InteractionRequest(
                InteractionType.PositionExpected,
                new InteractionContext(recruitedCharacter),
                legalTargets,
                legalResults
        );
    }

    @Nullable
    public InteractionFeedback getNextFeedback(@NonNull RecruitmentActionBuilder builder) {
        List<InteractionResult> results = builder.getResults();
        List<InteractionFeedback> feedbacks = builder.getFeedbacks();

        // There must be one feedback per result in a valid recruitment sequence.
        int diff = results.size() - feedbacks.size();
        if (diff < 0) {
            throw new IllegalStateException("There should never be more feedbacks than results");
        }
        if (diff > 1) {
            throw new IllegalStateException("Feedbacks must be generated immediately after their matching result. " +
                    "There should never be a difference of 2 or more between results feedbacks count");
        }

        if (feedbacks.size() == results.size()) {
            return null;
        }

        InteractionResult result = results.get(results.size() - 1);
        if (result.getResultType() == InteractionResultType.CancelAction) {
            return buildCancellationFeedback(builder);
        }

        return InteractionFeedback.createForRecruitmentAction(
                List.of(new RecruitmentActionMotion(
                        RecruitmentMotionType.Add,
                        result.getContext().getCharacter(),
                        getRecruitmentPosition(result)
                ))
        );
    }

    /**
     * Extracts and validates the recruitment position selected by an interaction
     * result.
     *
     * <p>The result must represent a position selection targeting a recruitment
     * destination and must contain a valid position.</p>
     *
     * @param result the interaction result containing the selected position
     * @return the selected recruitment position
     * @throws IllegalArgumentException if the result does not represent a valid
     *                                  recruitment destination
     * @throws NullPointerException if the result contains no position
     */
    @NonNull
    private static Position getRecruitmentPosition(@NonNull InteractionResult result) {
        if (result.getResultType() != InteractionResultType.PositionChosen) {
            throw new IllegalArgumentException("Invalid recruitment result type");
        }

        InteractionTarget target = result.getChosenTarget();
        if (target == null || target.getCategory() != TargetCategory.RecruitmentDestination) {
            throw new IllegalArgumentException("Recruitment result has no valid destination");
        }

        return Objects.requireNonNull(target.getChosenPosition(),
                "Recruitment interaction result invalid: no destination position");
    }

    /**
     * Builds the recruitment action represented by the feedbacks accumulated
     * during the recruitment.
     *
     * <p>Each recruitment feedback may contain one or more motions. All motions
     * are collected in their original order to form the resulting action.</p>
     *
     * @param builder the builder containing the feedbacks produced during the
     *                recruitment
     * @return the recruitment action represented by the builder's feedbacks
     */
    @NonNull
    public RecruitmentAction buildAction(@NonNull RecruitmentActionBuilder builder) {
        List<RecruitmentActionMotion> motions = new ArrayList<>();

        for (InteractionFeedback feedback : builder.getFeedbacks()) {
            motions.addAll(feedback.getRecruitmentActionMotions());
        }

        return new RecruitmentAction(motions);
    }

    @NonNull
    private List<Position> getValidRecruitmentPositions(@NonNull RecruitmentActionBuilder builder,
                                                        @NonNull Character recruitedCharacter) {
        List<Position> validPositions = new ArrayList<>();

        for (Cell recruitmentCell : BoardQuery.getRecruitmentCells(game.getBoard(), teamColor)) {
            Position recruitmentPos = recruitmentCell.getPosition();

            RecruitmentActionBuilder testBuilder = new RecruitmentActionBuilder(builder);

            testBuilder.addResult(new InteractionResult(
                    InteractionResultType.PositionChosen,
                    new InteractionContext(recruitedCharacter),
                    new InteractionTarget(TargetCategory.RecruitmentDestination, recruitmentPos)
            ));
            testBuilder.addFeedback(InteractionFeedback.createForRecruitmentAction(
                    List.of(new RecruitmentActionMotion(
                            RecruitmentMotionType.Add,
                            recruitedCharacter,
                            recruitmentPos
                    ))
            ));

            if (isActionValid(buildAction(testBuilder))) {
                validPositions.add(recruitmentPos);
            }
        }

        return validPositions;
    }

    @NonNull
    private InteractionFeedback buildCancellationFeedback(@NonNull RecruitmentActionBuilder builder) {
        List<RecruitmentActionMotion> cancellationMotions = new ArrayList<>();
        // We go through every feedback motion in reverse order and add them as remove motions
        for (int i = builder.getFeedbacks().size() - 1; i >= 0; i--) {
            InteractionFeedback feedback = builder.getFeedbacks().get(i);
            for (int j = feedback.getRecruitmentActionMotions().size() - 1; j >= 0; j--) {
                cancellationMotions.add(getCancellationMotion(
                        feedback.getRecruitmentActionMotions().get(j)
                ));
            }
        }
        return InteractionFeedback.createForRecruitmentAction(cancellationMotions);
    }

    @NonNull
    private static RecruitmentActionMotion getCancellationMotion(@NonNull RecruitmentActionMotion refMotion) {
        if (refMotion.getMotionType() != RecruitmentMotionType.Add) {
            throw new IllegalStateException("No recruitment removal motion is allowed outside of action cancellation");
        }
        return new RecruitmentActionMotion(
                RecruitmentMotionType.Remove,
                refMotion.getCharacter(),
                refMotion.getPosition()
        );
    }

    /**
     * Determines whether a recruitment action can be applied without leaving the
     * recruiting team in an invalid game state.
     *
     * <p>The action is temporarily applied to the game so that its consequences
     * can be evaluated. The game state is restored afterwards regardless of
     * whether validation succeeds or an exception is thrown.</p>
     *
     * <p>A recruitment is considered invalid if it results in the team's leader
     * being captured or surrounded.</p>
     *
     * @param action the recruitment action to validate
     * @return {@code true} if the action produces a valid resulting game state,
     *         {@code false} otherwise
     */
    private boolean isActionValid(@NonNull RecruitmentAction action) {
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
}