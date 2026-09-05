package com.leaders.gamelogic.queries;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.gamelogic.entities.Game;
import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.gamelogic.entities.GamePhase;
import com.leaders.gamelogic.enums.GameMode;
import com.leaders.gamelogic.enums.GamePhaseType;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.gamelogic.enums.TransitionTarget;
import com.leaders.gamelogic.historyentries.IHistoryEntry;
import com.leaders.gamelogic.historyentries.IPhase;
import com.leaders.gamelogic.historyentries.segments.Turn;

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
     * @param game    the current state of the game
     * @param history the history of the game
     * @return the next phase of the game
     * @throws IllegalStateException if the last ended phase is not supported
     */
    @NonNull
    public static GamePhase getNextPhase(@NonNull Game game, @NonNull GameHistory history) {
        if (history.getEntries().isEmpty()) {
            return getFirstPhase(history);
        }

        IPhase lastPhase = GameHistoryQuery.findLastEndedPhase(history);
        if (lastPhase == null) {
            throw new IllegalStateException("No next phase without a last phase");
        }

        TransitionTarget lastPhaseTransition = GameHistoryQuery.getPhaseTransitionTarget(lastPhase);
        GamePhaseType lastPhaseType = GamePhaseType.getFromTransitionTarget(lastPhaseTransition);
        TeamColor lastPhaseTeam = GameHistoryQuery.getPhaseTeamColor(lastPhase);

        GamePhaseType nextPhaseType = getNextPhaseType(game, history, lastPhaseType, lastPhaseTeam);
        TeamColor nextPhaseTeam = getNextPhaseTeam(history, lastPhaseType, lastPhaseTeam, nextPhaseType);

        return new GamePhase(nextPhaseType, GameHistoryQuery.getPlayerFromTeam(history, nextPhaseTeam));
    }

    /**
     * Returns the type of the phase that follows the current phase.
     *
     * @param game the current state of the game
     * @param gameHistory the history of the game
     * @param lastPhaseType the last phase type
     * @param currentPhaseTeam the team of the current phase
     * @return the type of the next phase
     * @throws IllegalStateException if the transition target is not supported
     */
    @NonNull
    private static GamePhaseType getNextPhaseType(@NonNull Game game,
                                                  @NonNull GameHistory gameHistory,
                                                  @NonNull GamePhaseType lastPhaseType,
                                                  @NonNull TeamColor currentPhaseTeam) {
        TeamColor oppositeTeam = currentPhaseTeam.getOpposite();

        switch (lastPhaseType) {
            case TurnStart: return GamePhaseType.Actions;

            case Recruitment: return GamePhaseType.TurnEnd;

            // Recruitment is delayed or skipped when it is not possible.
            case Actions:
                return RecruitmentQuery.canRecruit(game, gameHistory, currentPhaseTeam) ?
                        GamePhaseType.Recruitment : GamePhaseType.TurnEnd;

            // Banishment is skipped when it is no longer possible.
            case TurnEnd:
            case Banishment:
                return BanishmentQuery.canBanish(game, gameHistory, oppositeTeam) ?
                        GamePhaseType.Banishment : GamePhaseType.TurnStart;

            default: throw new IllegalStateException("\"" + lastPhaseType + "\" is not a valid phase type");
        }
    }

    /**
     * Returns the team to which the next phase belongs.
     *
     * @param nextPhaseType the type of the next phase
     * @param lastPhaseTeam the team of the current phase
     * @return the team of the next phase
     */
    @NonNull
    private static TeamColor getNextPhaseTeam(@NonNull GameHistory history,
                                              @NonNull GamePhaseType lastPhaseType,
                                              @NonNull TeamColor lastPhaseTeam,
                                              @NonNull GamePhaseType nextPhaseType) {
        // Banishment phase team order is decorelated from the turn team order
        if (nextPhaseType == GamePhaseType.Banishment) {
            TeamColor firstPlayerTeam = getFirstPlayerTeam(history);
            // The second player always bans first
            if (lastPhaseType != GamePhaseType.Banishment) {
                return firstPlayerTeam.getOpposite();
            }
            return firstPlayerTeam;
        }

        // Each turn start with the opposite player than the previous turn
        if (nextPhaseType == GamePhaseType.TurnStart) {
            // If the last phase was part of a turn we use it directly
            if (lastPhaseType.isTurnPhase()) {
                return lastPhaseTeam.getOpposite();
            }

            TeamColor lastTurnTeam = getLastTurnTeam(history);
            if (lastTurnTeam == null) {
                return getFirstPlayerTeam(history);
            }
            return lastTurnTeam.getOpposite();
        }

        // By default, the next phase team is the same as the last one
        return lastPhaseTeam;
    }

    /**
     * Returns the team color assigned to the first player in the game history config.
     *
     * @param history the game history containing the game configuration
     * @return the first player's team color
     */
    @NonNull
    private static TeamColor getFirstPlayerTeam(@NonNull GameHistory history) {
        return history.getConfig().getFirstPlayer().getTeamColor();
    }

    /**
     * Returns the team color of the last turn recorded in the game history.
     *
     * @param history the game history to inspect
     * @return the team color of the last turn, or {@code null} if no turn is recorded
     */
    @Nullable
    private static TeamColor getLastTurnTeam(@NonNull GameHistory history) {
        for (int i = history.getEntries().size() - 1; i >= 0; i--) {
            IHistoryEntry entry = history.getEntries().get(i);
            if (entry instanceof Turn) {
                return entry.getTeamColor();
            }
        }

        return null;
    }
}