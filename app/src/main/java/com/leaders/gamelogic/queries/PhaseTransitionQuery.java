package com.leaders.gamelogic.queries;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.entities.Game;
import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.gamelogic.entities.GamePhase;
import com.leaders.gamelogic.enums.GameMode;
import com.leaders.gamelogic.enums.GamePhaseType;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.gamelogic.enums.TransitionTarget;
import com.leaders.gamelogic.historyentries.IPhase;
import com.leaders.gamelogic.historyentries.segments.BanishmentPhase;
import com.leaders.gamelogic.historyentries.segments.TurnPhase;

public final class PhaseTransitionQuery {
    private PhaseTransitionQuery(){
        throw new AssertionError("Cannot instantiate utility class");
    }

    // TODO - javadoc
    private static GamePhase getFirstPhase(@NonNull GameHistory gameHistory) {
        // TODO - comment
        if (gameHistory.getConfig().getGameMode() == GameMode.Discovery) {
            return new GamePhase(
                    GamePhaseType.TurnStart,
                    gameHistory.getConfig().getFirstPlayer()
            );
        }

        // TODO - comment
        if (gameHistory.getConfig().getGameMode() == GameMode.Strategist) {
            TeamColor oppositeTeam = gameHistory.getConfig().getFirstPlayer().getTeamColor().getOpposite();

            return new GamePhase(
                    GamePhaseType.Banishment,
                    GameHistoryQuery.getPlayerFromTeam(gameHistory, oppositeTeam)
            );
        }

        // If this change, this algorithm would need to be updated to take into account the new cases
        throw new IllegalStateException("Game modes are limited to Discovery and Strategist");
    }

    // TODO - javadoc
    @NonNull
    public static GamePhase getNextPhase(@NonNull GameHistory history, @NonNull Game game) {
        if (history.getEntries().isEmpty()) {
            return getFirstPhase(history);
        }

        TransitionTarget lastPhaseTransition;
        TeamColor lastPhaseTeam;
        IPhase lastPhase = GameHistoryQuery.findLastEndedPhase(history);
        if (lastPhase instanceof TurnPhase) {
            TurnPhase turnPhase = (TurnPhase) lastPhase;
            lastPhaseTransition = turnPhase.getTransitionTarget();
            lastPhaseTeam = turnPhase.getTurnTeamColor();
        } else if (lastPhase instanceof BanishmentPhase) {
            BanishmentPhase banishmentPhase = (BanishmentPhase) lastPhase;
            lastPhaseTransition = banishmentPhase.getTransitionTarget();
            lastPhaseTeam = banishmentPhase.getTeamColor();
        } else {
            throw new IllegalStateException("A phase must belong to a turn or be a banishment phase");
        }

        GamePhaseType nextPhaseType = getNextPhaseType(game, history, lastPhaseTransition, lastPhaseTeam);
        TeamColor nextPhaseTeam = getNextPhaseTeam(nextPhaseType, lastPhaseTeam);

        return new GamePhase(
                nextPhaseType,
                GameHistoryQuery.getPlayerFromTeam(history, nextPhaseTeam)
        );
    }

    // TODO - javadoc
    @NonNull
    private static GamePhaseType getNextPhaseType(@NonNull Game game,
                                                  @NonNull GameHistory gameHistory,
                                                  @NonNull TransitionTarget currentPhaseTransition,
                                                  @NonNull TeamColor currentPhaseTeam) {
        TeamColor oppositeTeam = currentPhaseTeam.getOpposite();

        switch (currentPhaseTransition) {
            case TurnStartPhase: return GamePhaseType.Actions;

            case RecruitmentPhase: return GamePhaseType.TurnEnd;

            // TODO - comment : la phase de recrutement peut être reportée si aucun recrutement n'est possible et n'est plus proposée passé un certain stade de jeu
            case ActionsPhase:
                return RecruitmentQuery.canRecruit(game, gameHistory, oppositeTeam) ?
                        GamePhaseType.Recruitment : GamePhaseType.TurnEnd;

            // TODO - comment : la phase de bannissement n'est pas présente dans tous les modes de jeu et n'est plus proposée passé un certain stade de jeu
            case TurnEndPhase:
            case BanishmentPhase:
                return BanishmentQuery.canBanish(game, gameHistory, oppositeTeam) ?
                        GamePhaseType.Banishment : GamePhaseType.TurnStart;

            default: throw new IllegalStateException("\"" + currentPhaseTransition + "\" is not a valid transition");
        }
    }

    // TODO - javadoc
    @NonNull
    private static TeamColor getNextPhaseTeam(@NonNull GamePhaseType nextPhaseType,
                                              @NonNull TeamColor currentPhaseTeam) {

        // TODO - comment : début de tour ou phase de bannissement = changement de joueur
        if (nextPhaseType == GamePhaseType.TurnStart
                || nextPhaseType == GamePhaseType.Banishment) {
            return currentPhaseTeam.getOpposite();
        }

        return currentPhaseTeam;
    }
}