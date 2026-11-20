
package com.leaders.gamelogic.resolvers;

import com.leaders.gamelogic.actions.CharacterActionMotion;
import com.leaders.gamelogic.actions.CharacterActionTarget;
import com.leaders.gamelogic.entities.Cell;
import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.entities.CharacterPath;
import com.leaders.gamelogic.entities.Position;
import com.leaders.gamelogic.enums.CharacterMotionType;
import com.leaders.gamelogic.enums.CharacterType;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.gamelogic.interactions.CharacterActionBuilder;
import com.leaders.gamelogic.interactions.InteractionContext;
import com.leaders.gamelogic.interactions.InteractionFeedback;
import com.leaders.gamelogic.interactions.InteractionRequest;
import com.leaders.gamelogic.interactions.InteractionResult;
import com.leaders.gamelogic.interactions.InteractionResultType;
import com.leaders.gamelogic.interactions.InteractionTarget;
import com.leaders.gamelogic.interactions.InteractionType;
import com.leaders.gamelogic.interactions.TargetCategory;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.junit.Assert.*;

public class ResolverUtilsTest {

    private Character character;
    private InteractionContext context;

    private Position start;
    private Position middle;
    private Position destination;
    private Position fourthPosition;

    private TargetCategory category;
    private CharacterMotionType motionType;

    @Before
    public void setUp() {
        character = Character.create(
                firstEnum(CharacterType.class),
                firstEnum(TeamColor.class)
        );

        context = new InteractionContext(character);

        start = new Position(3, 3);
        middle = new Position(4, 3);
        destination = new Position(5, 3);
        fourthPosition = new Position(6, 3);

        category = firstEnum(TargetCategory.class);
        motionType = firstEnum(CharacterMotionType.class);
    }

    /**
     * Returns the first available enum constant.
     * This avoids coupling the tests to particular enum names.
     */
    private static <T extends Enum<T>> T firstEnum(Class<T> enumClass) {
        T[] values = enumClass.getEnumConstants();
        if (values == null || values.length == 0) {
            throw new IllegalStateException(
                    "Enum has no constants: " + enumClass.getName()
            );
        }
        return values[0];
    }

    private InteractionResult createPositionResult(Position position) {
        return new InteractionResult(
                InteractionResultType.PositionChosen,
                context,
                new InteractionTarget(category, position)
        );
    }

    private InteractionFeedback createFeedback() {
        return ResolverUtils.createSingleMotionFeedback(
                motionType,
                character,
                start,
                destination
        );
    }

    // ---------------------------------------------------------
    // isPositionResult()
    // ---------------------------------------------------------

    @Test
    public void isPositionResult_shouldReturnTrueWhenPositionAndCategoryMatch() {
        InteractionResult result = createPositionResult(start);

        assertTrue(
                ResolverUtils.isPositionResult(result, category)
        );
    }

    @Test
    public void isPositionResult_shouldReturnFalseWhenResultIsCancelled() {
        InteractionResult result = new InteractionResult(
                InteractionResultType.CancelAction,
                context,
                null
        );

        assertFalse(
                ResolverUtils.isPositionResult(result, category)
        );
    }

    @Test
    public void isPositionResult_shouldReturnFalseWhenTargetIsNull() {
        InteractionResult result = new InteractionResult(
                InteractionResultType.PositionChosen,
                context,
                null
        );

        assertFalse(
                ResolverUtils.isPositionResult(result, category)
        );
    }

    @Test
    public void isPositionResult_shouldReturnFalseWhenCategoryDoesNotMatch() {
        TargetCategory[] categories = TargetCategory.values();

        if (categories.length < 2) {
            return;
        }

        TargetCategory otherCategory = categories[1];

        InteractionResult result = createPositionResult(start);

        assertFalse(
                ResolverUtils.isPositionResult(result, otherCategory)
        );
    }

    // ---------------------------------------------------------
    // getChosenPosition()
    // ---------------------------------------------------------

    @Test
    public void getChosenPosition_shouldReturnChosenPosition() {
        InteractionResult result = createPositionResult(start);

        Position actual = ResolverUtils.getChosenPosition(result);

        assertSame(start, actual);
    }

    @Test
    public void getChosenPosition_shouldThrowWhenTargetIsNull() {
        InteractionResult result = new InteractionResult(
                InteractionResultType.CancelAction,
                context,
                null
        );

        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> ResolverUtils.getChosenPosition(result)
        );

        assertTrue(Objects.requireNonNull(exception.getMessage()).contains("No target"));
    }

    // ---------------------------------------------------------
    // createPositionResult()
    // ---------------------------------------------------------

    @Test
    public void createPositionResult_shouldCreateCorrectResult() {
        InteractionResult result = ResolverUtils.createPositionResult(
                context,
                category,
                start
        );

        assertEquals(
                InteractionResultType.PositionChosen,
                result.getResultType()
        );

        assertSame(context, result.getContext());

        assertNotNull(result.getChosenTarget());
        assertEquals(category, result.getChosenTarget().getCategory());
        assertSame(start, result.getChosenTarget().getChosenPosition());
    }

    // ---------------------------------------------------------
    // addTargets()
    // ---------------------------------------------------------

    @Test
    public void addTargets_shouldAddTargetsForEveryPosition() {
        List<Position> positions = Arrays.asList(
                start,
                middle,
                destination
        );

        List<InteractionTarget> targets = new ArrayList<>();

        ResolverUtils.addTargets(targets, category, positions);

        assertEquals(3, targets.size());

        for (int i = 0; i < positions.size(); i++) {
            InteractionTarget target = targets.get(i);

            assertEquals(category, target.getCategory());
            assertEquals(positions.get(i), target.getChosenPosition());
        }
    }

    @Test
    public void addTargets_shouldPreserveExistingTargets() {
        List<InteractionTarget> targets = new ArrayList<>();

        InteractionTarget existingTarget =
                new InteractionTarget(category, start);

        targets.add(existingTarget);

        ResolverUtils.addTargets(
                targets,
                category,
                Collections.singletonList(middle)
        );

        assertEquals(2, targets.size());
        assertSame(existingTarget, targets.get(0));
        assertEquals(middle, targets.get(1).getChosenPosition());
    }

    @Test
    public void addTargets_shouldDoNothingWhenPositionsAreEmpty() {
        List<InteractionTarget> targets = new ArrayList<>();

        ResolverUtils.addTargets(
                targets,
                category,
                Collections.emptyList()
        );

        assertTrue(targets.isEmpty());
    }

    // ---------------------------------------------------------
    // getPositions()
    // ---------------------------------------------------------

    @Test
    public void getPositions_shouldReturnPositionsOfCells() {
        List<Cell> cells = Arrays.asList(
                new Cell(start),
                new Cell(middle),
                new Cell(destination)
        );

        List<Position> positions = ResolverUtils.getPositions(cells);

        assertEquals(3, positions.size());
        assertSame(start, positions.get(0));
        assertSame(middle, positions.get(1));
        assertSame(destination, positions.get(2));
    }

    @Test
    public void getPositions_shouldReturnEmptyListWhenCellsAreEmpty() {
        List<Position> positions = ResolverUtils.getPositions(
                Collections.emptyList()
        );

        assertNotNull(positions);
        assertTrue(positions.isEmpty());
    }

    // ---------------------------------------------------------
    // createPositionRequest()
    // ---------------------------------------------------------

    @Test
    public void createPositionRequest_shouldCreateCorrectRequest() {
        List<InteractionTarget> targets = Arrays.asList(
                new InteractionTarget(category, start),
                new InteractionTarget(category, middle)
        );

        InteractionRequest request = ResolverUtils.createPositionRequest(
                context,
                targets
        );

        assertEquals(
                InteractionType.PositionExpected,
                request.getRequestType()
        );

        assertSame(context, request.getContext());

        assertEquals(targets, request.getLegalTargets());

        assertEquals(
                Arrays.asList(
                        InteractionResultType.PositionChosen,
                        InteractionResultType.CancelAction
                ),
                request.getLegalResults()
        );
    }

    @Test
    public void createPositionRequest_shouldAcceptEmptyTargets() {
        InteractionRequest request = ResolverUtils.createPositionRequest(
                context,
                Collections.emptyList()
        );

        assertNotNull(request);
        assertTrue(request.getLegalTargets().isEmpty());
    }

    // ---------------------------------------------------------
    // isFeedbackExpected()
    // ---------------------------------------------------------

    @Test
    public void isFeedbackExpected_shouldReturnTrueWhenResultsExistWithoutFeedbackOrCancellation() {
        CharacterActionBuilder builder = new CharacterActionBuilder(character);

        builder.addResult(createPositionResult(start));

        assertTrue(ResolverUtils.isFeedbackExpected(builder));
    }

    @Test
    public void isFeedbackExpected_shouldReturnFalseWhenResultsAreEmpty() {
        CharacterActionBuilder builder = new CharacterActionBuilder(character);

        assertFalse(ResolverUtils.isFeedbackExpected(builder));
    }

    @Test
    public void isFeedbackExpected_shouldReturnFalseWhenFeedbacksExist() {
        CharacterActionBuilder builder = new CharacterActionBuilder(character);

        builder.addResult(createPositionResult(start));
        builder.addFeedback(createFeedback());

        assertFalse(ResolverUtils.isFeedbackExpected(builder));
    }

    @Test
    public void isFeedbackExpected_shouldReturnFalseWhenActionIsCancelled() {
        CharacterActionBuilder builder = new CharacterActionBuilder(character);

        builder.addResult(
                new InteractionResult(
                        InteractionResultType.CancelAction,
                        context,
                        null
                )
        );

        assertFalse(ResolverUtils.isFeedbackExpected(builder));
    }

    @Test
    public void isFeedbackExpected_shouldReturnFalseWhenFeedbackExistsAndActionIsCancelled() {
        CharacterActionBuilder builder = new CharacterActionBuilder(character);

        builder.addResult(
                new InteractionResult(
                        InteractionResultType.CancelAction,
                        context,
                        null
                )
        );

        builder.addFeedback(createFeedback());

        assertFalse(ResolverUtils.isFeedbackExpected(builder));
    }

    // ---------------------------------------------------------
    // createMotionFeedback()
    // ---------------------------------------------------------

    @Test
    public void createMotionFeedback_shouldCreateFeedbackWithOneTarget() {
        CharacterActionTarget target = new CharacterActionTarget(
                character,
                start,
                destination
        );

        InteractionFeedback feedback = ResolverUtils.createMotionFeedback(
                motionType,
                target
        );

        assertEquals(
                com.leaders.gamelogic.enums.FeedbackType.CharacterAction,
                feedback.getFeedbackType()
        );

        List<CharacterActionMotion> motions =
                feedback.getCharacterActionMotions();

        assertEquals(1, motions.size());
        assertEquals(motionType, motions.get(0).getMotionType());
        assertEquals(1, motions.get(0).getTargets().size());

        assertSame(target, motions.get(0).getTargets().get(0));
    }

    @Test
    public void createMotionFeedback_shouldCreateFeedbackWithMultipleTargets() {
        CharacterActionTarget firstTarget = new CharacterActionTarget(
                character,
                start,
                middle
        );

        CharacterActionTarget secondTarget = new CharacterActionTarget(
                character,
                middle,
                destination
        );

        InteractionFeedback feedback = ResolverUtils.createMotionFeedback(
                motionType,
                firstTarget,
                secondTarget
        );

        List<CharacterActionMotion> motions =
                feedback.getCharacterActionMotions();

        assertEquals(1, motions.size());
        assertEquals(2, motions.get(0).getTargets().size());

        assertSame(firstTarget, motions.get(0).getTargets().get(0));
        assertSame(secondTarget, motions.get(0).getTargets().get(1));
    }

    @Test
    public void createMotionFeedback_shouldAcceptNoTargets() {
        InteractionFeedback feedback = ResolverUtils.createMotionFeedback(
                motionType
        );

        assertNotNull(feedback);

        List<CharacterActionMotion> motions =
                feedback.getCharacterActionMotions();

        assertEquals(1, motions.size());
        assertTrue(motions.get(0).getTargets().isEmpty());
    }

    // ---------------------------------------------------------
    // createSingleMotionFeedback()
    // ---------------------------------------------------------

    @Test
    public void createSingleMotionFeedback_shouldCreateCorrectMotion() {
        InteractionFeedback feedback =
                ResolverUtils.createSingleMotionFeedback(
                        motionType,
                        character,
                        start,
                        destination
                );

        List<CharacterActionMotion> motions =
                feedback.getCharacterActionMotions();

        assertEquals(1, motions.size());

        CharacterActionMotion motion = motions.get(0);

        assertEquals(motionType, motion.getMotionType());
        assertEquals(1, motion.getTargets().size());

        CharacterActionTarget target = motion.getTargets().get(0);

        assertSame(character, target.getCharacter());
        assertSame(start, target.getOriginPos());
        assertSame(destination, target.getDestPos());
    }

    // ---------------------------------------------------------
    // createPathFeedback()
    // ---------------------------------------------------------

    @Test
    public void createPathFeedback_shouldCreateOneMotionForOneStep() {
        CharacterPath path = new CharacterPath(
                Arrays.asList(start, destination)
        );

        InteractionFeedback feedback = ResolverUtils.createPathFeedback(
                character,
                path,
                motionType
        );

        List<CharacterActionMotion> motions =
                feedback.getCharacterActionMotions();

        assertEquals(1, motions.size());

        CharacterActionMotion motion = motions.get(0);

        assertEquals(motionType, motion.getMotionType());
        assertEquals(1, motion.getTargets().size());

        CharacterActionTarget target = motion.getTargets().get(0);

        assertSame(character, target.getCharacter());
        assertEquals(start, target.getOriginPos());
        assertEquals(destination, target.getDestPos());
    }

    @Test
    public void createPathFeedback_shouldCreateTwoMotionsForTwoSteps() {
        CharacterPath path = new CharacterPath(
                Arrays.asList(start, middle, destination)
        );

        InteractionFeedback feedback = ResolverUtils.createPathFeedback(
                character,
                path,
                motionType
        );

        List<CharacterActionMotion> motions =
                feedback.getCharacterActionMotions();

        assertEquals(2, motions.size());

        CharacterActionTarget firstTarget =
                motions.get(0).getTargets().get(0);

        CharacterActionTarget secondTarget =
                motions.get(1).getTargets().get(0);

        assertEquals(start, firstTarget.getOriginPos());
        assertEquals(middle, firstTarget.getDestPos());

        assertEquals(middle, secondTarget.getOriginPos());
        assertEquals(destination, secondTarget.getDestPos());
    }

    @Test
    public void createPathFeedback_shouldIgnorePositionsEqualToStart() {
        CharacterPath path = new CharacterPath(
                Arrays.asList(start, start, destination)
        );

        InteractionFeedback feedback = ResolverUtils.createPathFeedback(
                character,
                path,
                motionType
        );

        List<CharacterActionMotion> motions =
                feedback.getCharacterActionMotions();

        assertEquals(1, motions.size());

        CharacterActionTarget target =
                motions.get(0).getTargets().get(0);

        assertEquals(start, target.getOriginPos());
        assertEquals(destination, target.getDestPos());
    }

    @Test
    public void createPathFeedback_shouldReturnEmptyMotionsWhenAllPositionsEqualStart() {
        CharacterPath path = new CharacterPath(
                Arrays.asList(start, start)
        );

        InteractionFeedback feedback = ResolverUtils.createPathFeedback(
                character,
                path,
                motionType
        );

        assertTrue(feedback.getCharacterActionMotions().isEmpty());
    }

    @Test
    public void createPathFeedback_shouldThrowWhenPathExceedsTwoSteps() {
        CharacterPath path = new CharacterPath(
                Arrays.asList(
                        start,
                        middle,
                        destination,
                        fourthPosition
                )
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ResolverUtils.createPathFeedback(
                        character,
                        path,
                        motionType
                )
        );

        assertTrue(Objects.requireNonNull(exception.getMessage()).contains("should not exceed two steps"));
    }
}