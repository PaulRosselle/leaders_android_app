package com.leaders.puzzlelogic.serializers.historyentries;

import com.leaders.gamelogic.actions.TransitionAction;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.gamelogic.enums.TransitionTarget;
import com.leaders.gamelogic.enums.TransitionType;
import com.leaders.gamelogic.historyentries.segments.ActionsPhase;
import com.leaders.gamelogic.historyentries.segments.RecruitmentPhase;
import com.leaders.gamelogic.historyentries.segments.Turn;
import com.leaders.gamelogic.historyentries.segments.TurnEndPhase;
import com.leaders.gamelogic.historyentries.segments.TurnStartPhase;
import com.leaders.puzzlelogic.serializers.SerializerRoundTripTestSupport;

import org.junit.Test;

public class TurnSerializerTest {
    @Test
    public void roundTrip_shouldPreserveJson() throws Exception {
        TransitionAction startAction =
                new TransitionAction(TransitionType.Start, TransitionTarget.Turn);
        TransitionAction endAction =
                new TransitionAction(TransitionType.End, TransitionTarget.Turn);

        TurnStartPhase turnStartPhase =
                new TurnStartPhase(
                        new TransitionAction(TransitionType.Start, TransitionTarget.TurnStartPhase),
                        new TransitionAction(TransitionType.End, TransitionTarget.TurnStartPhase),
                        TeamColor.Black
                );
        ActionsPhase actionsPhase =
                new ActionsPhase(
                        new TransitionAction(TransitionType.Start, TransitionTarget.ActionsPhase),
                        new TransitionAction(TransitionType.End, TransitionTarget.ActionsPhase),
                        TeamColor.Black
                );
        RecruitmentPhase recruitmentPhase =
                new RecruitmentPhase(
                        new TransitionAction(TransitionType.Start, TransitionTarget.RecruitmentPhase),
                        new TransitionAction(TransitionType.End, TransitionTarget.RecruitmentPhase),
                        TeamColor.Black
                );
        TurnEndPhase turnEndPhase =
                new TurnEndPhase(
                        new TransitionAction(TransitionType.Start, TransitionTarget.TurnEndPhase),
                        new TransitionAction(TransitionType.End, TransitionTarget.TurnEndPhase),
                        TeamColor.Black
                );

        Turn turn =
                new Turn(
                        startAction,
                        endAction,
                        TeamColor.Black,
                        turnStartPhase,
                        actionsPhase,
                        recruitmentPhase,
                        turnEndPhase
                );

        SerializerRoundTripTestSupport.assertRoundTrip(
                new TurnSerializer(),
                turn,
                SerializerRoundTripTestSupport.contextWith()
        );
    }
}
