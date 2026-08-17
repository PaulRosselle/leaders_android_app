package com.leaders.puzzlelogic.serializers.historyentries;

import com.leaders.gamelogic.actions.TransitionAction;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.gamelogic.enums.TransitionTarget;
import com.leaders.gamelogic.enums.TransitionType;
import com.leaders.gamelogic.historyentries.segments.BanishmentPhase;
import com.leaders.puzzlelogic.serializers.SerializerRoundTripTestSupport;

import org.junit.Test;

public class BanishmentPhaseSerializerTest {
    @Test
    public void roundTrip_shouldPreserveJson() throws Exception {
        TransitionAction startAction =
                new TransitionAction(TransitionType.Start, TransitionTarget.BanishmentPhase);
        TransitionAction endAction =
                new TransitionAction(TransitionType.End, TransitionTarget.BanishmentPhase);
        BanishmentPhase phase =
                new BanishmentPhase(startAction, endAction, TeamColor.Black);

        SerializerRoundTripTestSupport.assertRoundTrip(
                new BanishmentPhaseSerializer(),
                phase,
                SerializerRoundTripTestSupport.contextWith()
        );
    }
}
