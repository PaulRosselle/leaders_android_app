
package com.leaders.puzzlelogic.entities.blocker;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.actions.CharacterAction;

import java.util.BitSet;
import java.util.List;

final class NemesisBlockerCoordinator {
    enum SearchStatus {
        RUNNING, FOUND, COMPLETED_WITHOUT_RESULT, CANCELLED }

    private final List<CharacterAction> actions;
    private final BitSet completed = new BitSet();

    private SearchStatus status = SearchStatus.RUNNING;
    private int nextIndex = 0;
    private int bestIndex = -1; // Smallest blocking action index found

    NemesisBlockerCoordinator(@NonNull List<CharacterAction> actions) {
        this.actions = actions;
        if (actions.isEmpty()) {
            status = SearchStatus.COMPLETED_WITHOUT_RESULT;
        }
    }

    /**
     * Claims the next action index for processing.
     *
     * @return the next available index, or {@code -1} if no index can be claimed
     */
    synchronized int claimNext() {
        if (status != SearchStatus.RUNNING) return -1;
        if (nextIndex >= actions.size()) return -1;
        if (bestIndex != -1 && nextIndex > bestIndex) return -1;
        return nextIndex++;
    }

    /**
     * Checks whether the search for the given action index can be skipped.
     *
     * @param index the action index to check
     * @return {@code true} if the search is no longer running or a better result has been found, otherwise {@code false}
     */
    synchronized boolean isSuperseded(int index) {
        return status != SearchStatus.RUNNING || (bestIndex != -1 && index > bestIndex);
    }

    /**
     * Reports the result of evaluating an action.
     *
     * @param index the index of the evaluated action
     * @param blocks {@code true} if the action blocks the threat, otherwise {@code false}
     */
    synchronized void reportResult(int index, boolean blocks) {
        if (status != SearchStatus.RUNNING) return;

        completed.set(index);
        if (blocks && (bestIndex == -1 || index < bestIndex)) {
            bestIndex = index;
        }

        // Ends when all indexes are resolved or strickly below the limit
        int limit = bestIndex == -1 ? actions.size() : bestIndex;
        if (completed.nextClearBit(0) >= limit) {
            status = bestIndex == -1 ? SearchStatus.COMPLETED_WITHOUT_RESULT : SearchStatus.FOUND;
            notifyAll();
        }
    }

    synchronized void cancel() {
        if (status == SearchStatus.RUNNING) {
            status = SearchStatus.CANCELLED;
            notifyAll();
        }
    }

    synchronized SearchStatus getStatus() { return status; }
    synchronized boolean isFinished() { return status != SearchStatus.RUNNING; }

    synchronized CharacterAction getWinningAction() {
        return bestIndex != -1 ? actions.get(bestIndex) : null;
    }
}
