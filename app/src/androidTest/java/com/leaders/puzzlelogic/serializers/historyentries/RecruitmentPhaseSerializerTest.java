package com.leaders.puzzlelogic.serializers.historyentries;

import com.leaders.gamelogic.actions.TransitionAction;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.gamelogic.enums.TransitionTarget;
import com.leaders.gamelogic.enums.TransitionType;
import com.leaders.gamelogic.historyentries.segments.RecruitmentPhase;
import com.leaders.puzzlelogic.serializers.SerializerRoundTripTestSupport;

import org.junit.Test;

import java.util.Collections;

public class RecruitmentPhaseSerializerTest {
    @Test
    public void roundTrip_shouldPreserveJson() throws Exception {
        TransitionAction startAction =
                new TransitionAction(TransitionType.Start, TransitionTarget.RecruitmentPhase);
        TransitionAction endAction =
                new TransitionAction(TransitionType.End, TransitionTarget.RecruitmentPhase);
        RecruitmentPhase phase =
                new RecruitmentPhase(
                        startAction,
                        endAction,
                        TeamColor.Black
                );

        SerializerRoundTripTestSupport.assertRoundTrip(
                new RecruitmentPhaseSerializer(),
                phase,
                SerializerRoundTripTestSupport.contextWith()
        );
    }
}
