package com.leaders.puzzlelogic.serializers.actions;

import com.leaders.gamelogic.actions.TransitionAction;
import com.leaders.gamelogic.enums.TransitionTarget;
import com.leaders.gamelogic.enums.TransitionType;
import com.leaders.puzzlelogic.serializers.SerializerRoundTripTestSupport;

import org.junit.Test;

public class TransitionActionSerializerTest {
    @Test
    public void roundTrip_shouldPreserveJson() throws Exception {
        TransitionAction action =
                new TransitionAction(TransitionType.Start, TransitionTarget.ActionsPhase);

        SerializerRoundTripTestSupport.assertRoundTrip(
                new TransitionActionSerializer(),
                action,
                SerializerRoundTripTestSupport.contextWith()
        );
    }
}
