
package com.leaders.puzzlelogic.entities.blocker;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.actions.CharacterAction;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;

final class NemesisBlockerCoordinator {
    enum SearchStatus {
        RUNNING,
        FOUND,
        COMPLETED_WITHOUT_RESULT,
        CANCELLED
    }

    private final Deque<ActionCandidate> candidates = new ArrayDeque<>();

    private SearchStatus status = SearchStatus.RUNNING;
    private ActionCandidate winningCandidate;

    NemesisBlockerCoordinator(@NonNull Collection<ActionCandidate> initialCandidates) {
        candidates.addAll(initialCandidates);

        if (candidates.isEmpty()) {
            status = SearchStatus.COMPLETED_WITHOUT_RESULT;
        }
    }

    /**
     * Returns the current candidate without removing it from the queue.
     *
     * @return the current candidate, or {@code null} if the search is no longer running
     * or no candidates remain.
     */
    synchronized ActionCandidate getCurrentCandidate() {
        if (status != SearchStatus.RUNNING) {
            return null;
        }

        removeInvalidatedHead();

        if (candidates.isEmpty()) {
            status = SearchStatus.COMPLETED_WITHOUT_RESULT;
            notifyAll();
            return null;
        }

        return candidates.peekFirst();
    }

    /**
     * Processes a candidate's permutation result.
     *
     * @param candidate the candidate whose result is reported
     * @param candidateStatus the candidate's updated status
     */
    synchronized void reportResult(@NonNull ActionCandidate candidate,
                                   @NonNull ActionCandidate.Status candidateStatus) {
        // No result to report while the search is running
        if (status != SearchStatus.RUNNING) {
            return;
        }

        // Once a candidate is invalidated, it is removed from the list so
        // the workers can continue the search with the next one
        if (candidateStatus == ActionCandidate.Status.INVALIDATED) {
            if (candidates.peekFirst() == candidate) {
                candidates.removeFirst();
            } else {
                candidates.remove(candidate);
            }

            removeInvalidatedHead();

            if (candidates.isEmpty()) {
                status = SearchStatus.COMPLETED_WITHOUT_RESULT;
            }

            notifyAll();
            return;
        }

        // We only take into account the current candidate
        if (candidateStatus == ActionCandidate.Status.FOUND &&
                candidates.peekFirst() == candidate) {
            winningCandidate = candidate;
            status = SearchStatus.FOUND;
            notifyAll();
        }
    }

    /**
     * Removes all invalidated candidates from the head of the queue.
     */
    private void removeInvalidatedHead() {
        if (candidates.isEmpty()) {
            return;
        }

        ActionCandidate firstCandidate = candidates.peekFirst();
        if (firstCandidate != null &&
                firstCandidate.getStatus() == ActionCandidate.Status.INVALIDATED) {
            candidates.removeFirst();
            removeInvalidatedHead();
        }
    }

    /**
     * Interupt global search.
     */
    synchronized void cancel() {
        if (status == SearchStatus.RUNNING) {
            status = SearchStatus.CANCELLED;
            notifyAll();
        }
    }

    synchronized SearchStatus getStatus() {
        return status;
    }

    synchronized CharacterAction getWinningAction() {
        return winningCandidate != null ? winningCandidate.action : null;
    }

    synchronized boolean isFinished() {
        return status != SearchStatus.RUNNING;
    }
}
