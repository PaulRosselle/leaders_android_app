package com.leaders.gamelogic.enums;

import java.util.NoSuchElementException;

public enum GamePhaseType {
    Banishment,
    TurnStart,
    Actions,
    Recruitment,
    TurnEnd;

    /**
     * Returns the {@link TransitionTarget} corresponding to this game phase type.
     *
     * @return the matching {@link TransitionTarget}
     * @throws NoSuchElementException if no matching transition target is found
     */
    public TransitionTarget getTransitionTarget() {
        switch (this) {
            case Banishment: return TransitionTarget.BanishmentPhase;
            case TurnStart: return TransitionTarget.TurnStartPhase;
            case Actions: return TransitionTarget.ActionsPhase;
            case Recruitment: return TransitionTarget.RecruitmentPhase;
            case TurnEnd: return TransitionTarget.TurnEndPhase;
            default: throw new NoSuchElementException(String.format("No transition target found matching %s", this));
        }
    }

    /**
     * Returns the {@link GamePhaseType} corresponding to the given {@link TransitionTarget}.
     *
     * @param transitionTarget the transition target to match against
     * @return the matching {@link GamePhaseType}
     * @throws NoSuchElementException if no matching phase type is found
     */
    public static GamePhaseType getFromTransitionTarget(TransitionTarget transitionTarget) {
        for (GamePhaseType gamePhaseType : GamePhaseType.values()) {
            if (gamePhaseType.getTransitionTarget() == transitionTarget) {
                return gamePhaseType;
            }
        }
        throw new NoSuchElementException(String.format("No phase type found matching transition target %s", transitionTarget));
    }
}
