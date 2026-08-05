package com.leaders.gamelogic.queries;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.gamelogic.historyentries.IHistoryEntry;
import com.leaders.gamelogic.historyentries.IPhase;
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
     * @param phasePredicate condition used to identify the expected phase
     * @param reverseOrder whether sub-phases should be searched from the end
     * @return the matching phase, or {@code null} if no phase matches the condition
     */
    @Nullable
    private static IPhase findPhase(@NonNull GameHistory gameHistory,
                                    @NonNull Predicate<TurnPhase> phasePredicate,
                                    boolean reverseOrder) {
        List<IHistoryEntry> entries = gameHistory.getEntries();
        if (entries.isEmpty()) {
            return null;
        }

        IHistoryEntry lastEntry = entries.get(entries.size() - 1);
        if (lastEntry instanceof IPhase) {
            return (IPhase) lastEntry;
        }

        if (lastEntry instanceof Turn) {
            TurnPhase[] turnPhases = ((Turn) lastEntry).getSubPhasesInOrder();
            if (reverseOrder) {
                for (int i = turnPhases.length - 1; i >= 0; i--) {
                    if (phasePredicate.test(turnPhases[i])) {
                        return turnPhases[i];
                    }
                }
            } else {
                for (TurnPhase turnPhase : turnPhases) {
                    if (phasePredicate.test(turnPhase)) {
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
                turnPhase -> turnPhase.hasStarted() && turnPhase.hasEnded(),
                true
        );
    }
}
