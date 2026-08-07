package com.leaders.gamelogic.enums;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class GamePhaseTypeTest {

    @Test
    public void getTransitionTarget_shouldReturnMatchingTarget() {
        assertEquals(
                TransitionTarget.BanishmentPhase,
                GamePhaseType.Banishment.getTransitionTarget()
        );

        assertEquals(
                TransitionTarget.TurnStartPhase,
                GamePhaseType.TurnStart.getTransitionTarget()
        );

        assertEquals(
                TransitionTarget.ActionsPhase,
                GamePhaseType.Actions.getTransitionTarget()
        );

        assertEquals(
                TransitionTarget.RecruitmentPhase,
                GamePhaseType.Recruitment.getTransitionTarget()
        );

        assertEquals(
                TransitionTarget.TurnEndPhase,
                GamePhaseType.TurnEnd.getTransitionTarget()
        );
    }

    @Test
    public void getFromTransitionTarget_shouldReturnMatchingPhaseType() {
        assertEquals(
                GamePhaseType.Banishment,
                GamePhaseType.getFromTransitionTarget(
                        TransitionTarget.BanishmentPhase
                )
        );

        assertEquals(
                GamePhaseType.TurnStart,
                GamePhaseType.getFromTransitionTarget(
                        TransitionTarget.TurnStartPhase
                )
        );

        assertEquals(
                GamePhaseType.Actions,
                GamePhaseType.getFromTransitionTarget(
                        TransitionTarget.ActionsPhase
                )
        );

        assertEquals(
                GamePhaseType.Recruitment,
                GamePhaseType.getFromTransitionTarget(
                        TransitionTarget.RecruitmentPhase
                )
        );

        assertEquals(
                GamePhaseType.TurnEnd,
                GamePhaseType.getFromTransitionTarget(
                        TransitionTarget.TurnEndPhase
                )
        );
    }

    @Test
    public void shouldContainExactlyFiveGamePhaseTypes() {
        assertEquals(5, GamePhaseType.values().length);
    }
}