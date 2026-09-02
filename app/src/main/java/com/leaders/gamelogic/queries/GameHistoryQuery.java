package com.leaders.gamelogic.queries;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.gamelogic.entities.Player;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.gamelogic.enums.TransitionTarget;
import com.leaders.gamelogic.historyentries.IHistoryEntry;
import com.leaders.gamelogic.historyentries.IPhase;
import com.leaders.gamelogic.historyentries.Segment;
import com.leaders.gamelogic.historyentries.segments.BanishmentPhase;
import com.leaders.gamelogic.historyentries.segments.Turn;
import com.leaders.gamelogic.historyentries.segments.TurnPhase;

import java.util.List;
import java.util.function.Predicate;

public final class GameHistoryQuery {
    private GameHistoryQuery(){
        throw new AssertionError("Cannot instantiate utility class");
    }

    /**
     * Finds the turn currently active in the game history.
     *
     * <p>A turn is considered current only if it is the last entry of the history.
     *
     * @param gameHistory the game history to inspect
     * @return the current turn, or {@code null} if no turn is currently active
     */
    @Nullable
    public static Turn findCurrentTurn(@NonNull GameHistory gameHistory) {
        List<IHistoryEntry> entries = gameHistory.getEntries();
        if (!entries.isEmpty()) {
            IHistoryEntry lastEntry = entries.get(entries.size() - 1);
            if (lastEntry instanceof Turn) {
                return (Turn) lastEntry;
            }
        }
        return null;
    }

    /**
     * Finds a phase matching the given condition in the last game history entry.
     *
     * <p>If the latest history entry is a phase, that phase is returned directly.
     * If it is a turn, its sub-phases are searched according to the requested order.
     *
     * @param gameHistory the game history to inspect
     * @param predicate condition used to identify the expected phase
     * @param reverseOrder whether sub-phases should be searched from the end
     * @return the matching phase, or {@code null} if no phase matches the condition
     */
    @Nullable
    private static IPhase findPhase(@NonNull GameHistory gameHistory,
                                    @NonNull Predicate<Segment> predicate,
                                    boolean reverseOrder) {
        List<IHistoryEntry> entries = gameHistory.getEntries();
        if (entries.isEmpty()) {
            return null;
        }

        IHistoryEntry lastEntry = entries.get(entries.size() - 1);
        if (lastEntry instanceof IPhase) {
            IPhase lastPhase = (IPhase) lastEntry;
            if (!(lastPhase instanceof Segment)) {
                throw new IllegalStateException("A game phase should always be an history segment as well");
            }
            return predicate.test((Segment) lastPhase) ? lastPhase : null;
        }

        if (lastEntry instanceof Turn) {
            TurnPhase[] turnPhases = ((Turn) lastEntry).getSubPhasesInOrder();
            if (reverseOrder) {
                for (int i = turnPhases.length - 1; i >= 0; i--) {
                    if (predicate.test(turnPhases[i])) {
                        return turnPhases[i];
                    }
                }
            } else {
                for (TurnPhase turnPhase : turnPhases) {
                    if (predicate.test(turnPhase)) {
                        return turnPhase;
                    }
                }
            }

            return null;
        }

        throw new AssertionError("History entries should only be turns or phases");
    }

    /**
     * Finds the phase currently active in the game history.
     *
     * @param gameHistory the game history to inspect
     * @return the current phase, or {@code null} if no phase is currently active
     */
    @Nullable
    public static IPhase findCurrentPhase(@NonNull GameHistory gameHistory) {
        return findPhase(gameHistory,
                turnPhase -> turnPhase.hasStarted() && !turnPhase.hasEnded(),
                false
        );
    }

    /**
     * Finds the most recently completed phase in the game history.
     *
     * @param gameHistory the game history to inspect
     * @return the last ended phase, or {@code null} if no completed phase exists
     */
    @Nullable
    public static IPhase findLastEndedPhase(@NonNull GameHistory gameHistory) {
        return findPhase(gameHistory,
                phase -> phase.hasStarted() && phase.hasEnded(),
                true
        );
    }

    /**
     * Returns the team color associated with the given phase.
     *
     * @param phase phase whose team should be resolved
     * @return the team associated with the phase
     * @throws IllegalStateException if the phase does not belong to a turn or banishment phase
     */
    @NonNull
    public static TeamColor getPhaseTeamColor(@NonNull IPhase phase) {
        if (phase instanceof TurnPhase) {
            return ((TurnPhase) phase).getTurnTeamColor();
        }
        if (phase instanceof BanishmentPhase) {
           return ((BanishmentPhase) phase).getTeamColor();
        }
        throw new IllegalStateException("A phase must belong to a turn or be a banishment phase");
    }

    /**
     * Returns the transition target associated with the given phase.
     *
     * @param phase phase whose transition target should be resolved
     * @return the transition target associated with the phase
     * @throws IllegalStateException if the phase does not belong to a turn or banishment phase
     */
    @NonNull
    public static TransitionTarget getPhaseTransitionTarget(@NonNull IPhase phase) {
        if (phase instanceof TurnPhase) {
            return ((TurnPhase) phase).getTransitionTarget();
        }
        if (phase instanceof BanishmentPhase) {
            return ((BanishmentPhase) phase).getTransitionTarget();
        }
        throw new IllegalStateException("A phase must belong to a turn or be a banishment phase");
    }

    /**
     * Returns the player with the matching team color.
     *
     * @param history the game history containing the players
     * @param teamColor the team color of the player to find
     * @return the player whose team matches {@code teamColor}
     * @throws IllegalStateException if no player with the given team color can be found
     */
    @NonNull
    public static Player getPlayerFromTeam(@NonNull GameHistory history, @NonNull TeamColor teamColor) {
        for (Player player : history.getConfig().getPlayers()) {
            if (player.getTeamColor() == teamColor) {
                return player;
            }
        }

        throw new IllegalStateException("No player found for team " + teamColor);
    }
}
