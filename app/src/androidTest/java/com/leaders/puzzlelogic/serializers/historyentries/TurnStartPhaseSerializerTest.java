package com.leaders.puzzlelogic.serializers.historyentries;

import com.leaders.gamelogic.actions.TransitionAction;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.gamelogic.enums.TransitionTarget;
import com.leaders.gamelogic.enums.TransitionType;
import com.leaders.gamelogic.historyentries.segments.TurnStartPhase;
import com.leaders.puzzlelogic.serializers.SerializerRoundTripTestSupport;

import org.junit.Test;

import java.util.Collections;

public class TurnStartPhaseSerializerTest {
    @Test
    public void roundTrip_shouldPreserveJson() throws Exception {
        TransitionAction startAction =
                new TransitionAction(TransitionType.Start, TransitionTarget.TurnStartPhase);
        TransitionAction endAction =
                new TransitionAction(TransitionType.End, TransitionTarget.TurnStartPhase);
        TurnStartPhase phase =
                new TurnStartPhase(
                        startAction,
                        endAction,
                        TeamColor.Black
                );

        SerializerRoundTripTestSupport.assertRoundTrip(
                new TurnStartPhaseSerializer(),
                phase,
                SerializerRoundTripTestSupport.contextWith()
        );
    }
}
