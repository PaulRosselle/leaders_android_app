package com.leaders.gamelogic.interactions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameActionBuilderTest {

    @Test
    public void isBuildCancelledReturnsFalseWhenNoCancellationResultExists() {
        GameActionBuilder builder = createBuilder();

        assertFalse(builder.isBuildCancelled());
    }

    @Test
    public void isBuildCancelledReturnsTrueWhenCancellationResultExists() {
        GameActionBuilder builder = createBuilder();

        builder.addResult(createCancelActionResult());

        assertTrue(builder.isBuildCancelled());
    }

    @Test
    public void isBuildCancelledReturnsTrueWhenCancellationResultIsNotLastResult() {
        GameActionBuilder builder = createBuilder();

        builder.addResult(createCancelActionResult());
        builder.addResult(createNonCancellationResult());

        assertTrue(builder.isBuildCancelled());
    }

    @Test
    public void getResultsReturnsAddedResults() {
        GameActionBuilder builder = createBuilder();
        InteractionResult result = createNonCancellationResult();

        builder.addResult(result);

        assertEquals(Collections.singletonList(result), builder.getResults());
    }

    @Test
    public void getFeedbacksReturnsAddedFeedbacks() {
        GameActionBuilder builder = createBuilder();
        InteractionFeedback feedback = createFeedback();

        builder.addFeedback(feedback);

        assertEquals(Collections.singletonList(feedback), builder.getFeedbacks());
    }

    private GameActionBuilder createBuilder() {
        return new GameActionBuilder(new ArrayList<>(), new ArrayList<>()) {
        };
    }

    private InteractionResult createCancelActionResult() {
        return new InteractionResult(InteractionResultType.CancelAction, new InteractionContext(), null);
    }

    private InteractionResult createNonCancellationResult() {
        return new InteractionResult(InteractionResultType.NoChoice, new InteractionContext(), null);
    }

    private InteractionFeedback createFeedback() {
        return InteractionFeedback.createForCharacterAction(List.of());
    }
}