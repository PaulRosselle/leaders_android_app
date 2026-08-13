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

/**
 * Utility class responsible for determining the next phase of a game.
 *
 * <p>The transition between phases depends on the current game mode, the
 * transition target of the last completed phase, the team concerned by that
 * phase, and the current state of the game.</p>
 */
public final class PhaseTransitionQuery {
    private PhaseTransitionQuery(){
        throw new AssertionError("Cannot instantiate utility class");
    }

    /**
     * Returns the first phase of the game.
     *
     * @param gameHistory the history of the game
     * @return the first phase of the game
     * @throws IllegalStateException if the game mode is not supported
     */
    @NonNull
    private static GamePhase getFirstPhase(@NonNull GameHistory gameHistory) {
        // The first phase depends on the game mode
        if (gameHistory.getConfig().getGameMode() == GameMode.Discovery) {
            return new GamePhase(
                    GamePhaseType.TurnStart,
                    gameHistory.getConfig().getFirstPlayer()
            );
        }

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

    /**
     * Returns the phase that follows the last ended phase of the game.
     *
     * @param history the history of the game
     * @param game the current state of the game
     * @return the next phase of the game
     * @throws IllegalStateException if the last ended phase is not supported
     */
    @NonNull
    public static GamePhase getNextPhase(@NonNull GameHistory history, @NonNull Game game) {
        if (history.getEntries().isEmpty()) {
            return getFirstPhase(history);
        }

        IPhase lastPhase = GameHistoryQuery.findLastEndedPhase(history);
        if (lastPhase == null) {
            throw new IllegalStateException("No next phase without a last phase");
        }

        TransitionTarget lastPhaseTransition = GameHistoryQuery.getPhaseTransitionTarget(lastPhase);
        TeamColor lastPhaseTeam = GameHistoryQuery.getPhaseTeamColor(lastPhase);
        GamePhaseType nextPhaseType = getNextPhaseType(game, history, lastPhaseTransition, lastPhaseTeam);
        TeamColor nextPhaseTeam = getNextPhaseTeam(nextPhaseType, lastPhaseTeam);

        return new GamePhase(
                nextPhaseType,
                GameHistoryQuery.getPlayerFromTeam(history, nextPhaseTeam)
        );
    }

    /**
     * Returns the type of the phase that follows the current phase.
     *
     * @param game the current state of the game
     * @param gameHistory the history of the game
     * @param currentPhaseTransition the transition target of the current phase
     * @param currentPhaseTeam the team of the current phase
     * @return the type of the next phase
     * @throws IllegalStateException if the transition target is not supported
     */
    @NonNull
    private static GamePhaseType getNextPhaseType(@NonNull Game game,
                                                  @NonNull GameHistory gameHistory,
                                                  @NonNull TransitionTarget currentPhaseTransition,
                                                  @NonNull TeamColor currentPhaseTeam) {
        TeamColor oppositeTeam = currentPhaseTeam.getOpposite();

        switch (currentPhaseTransition) {
            case TurnStartPhase: return GamePhaseType.Actions;

            case RecruitmentPhase: return GamePhaseType.TurnEnd;

            // Recruitment is delayed or skipped when it is not possible.
            case ActionsPhase:
                return RecruitmentQuery.canRecruit(game, gameHistory, oppositeTeam) ?
                        GamePhaseType.Recruitment : GamePhaseType.TurnEnd;

            // Banishment is skipped when it is no longer possible.
            case TurnEndPhase:
            case BanishmentPhase:
                return BanishmentQuery.canBanish(game, gameHistory, oppositeTeam) ?
                        GamePhaseType.Banishment : GamePhaseType.TurnStart;

            default: throw new IllegalStateException("\"" + currentPhaseTransition + "\" is not a valid transition");
        }
    }

    /**
     * Returns the team to which the next phase belongs.
     *
     * @param nextPhaseType the type of the next phase
     * @param currentPhaseTeam the team of the current phase
     * @return the team of the next phase
     */
    @NonNull
    private static TeamColor getNextPhaseTeam(@NonNull GamePhaseType nextPhaseType,
                                              @NonNull TeamColor currentPhaseTeam) {
        // A starting a new turn or a banishment phase means it is the opposite team time to play.
        if (nextPhaseType == GamePhaseType.TurnStart
                || nextPhaseType == GamePhaseType.Banishment) {
            return currentPhaseTeam.getOpposite();
        }

        return currentPhaseTeam;
    }
}