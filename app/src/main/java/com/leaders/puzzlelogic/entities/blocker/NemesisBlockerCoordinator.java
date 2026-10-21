
package com.leaders.puzzlelogic.entities.blocker;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.actions.CharacterAction;

import java.util.ArrayDeque;
import java.util.BitSet;
import java.util.Collection;
import java.util.Deque;
import java.util.List;

final class NemesisBlockerCoordinator {
    enum SearchStatus { RUNNING, FOUND, COMPLETED_WITHOUT_RESULT, CANCELLED }

    private final List<CharacterAction> actions;
    private final BitSet completed = new BitSet();

    private SearchStatus status = SearchStatus.RUNNING;
    private int nextIndex = 0;
    private int bestIndex = -1; // plus petit index bloquant trouvé

    NemesisBlockerCoordinator(@NonNull List<CharacterAction> actions) {
        this.actions = actions;
        if (actions.isEmpty()) {
            status = SearchStatus.COMPLETED_WITHOUT_RESULT;
        }
    }

    /** Réserve l'index suivant à évaluer, ou -1 s'il n'y a plus rien d'utile à faire. */
    synchronized int claimNext() {
        if (status != SearchStatus.RUNNING) return -1;
        if (nextIndex >= actions.size()) return -1;
        if (bestIndex != -1 && nextIndex > bestIndex) return -1;
        return nextIndex++;
    }

    /** Vrai si le résultat du candidat n'a plus d'intérêt (recherche finie ou meilleur déjà trouvé). */
    synchronized boolean isSuperseded(int index) {
        return status != SearchStatus.RUNNING || (bestIndex != -1 && index > bestIndex);
    }

    synchronized void reportResult(int index, boolean blocks) {
        if (status != SearchStatus.RUNNING) return;

        completed.set(index);
        if (blocks && (bestIndex == -1 || index < bestIndex)) {
            bestIndex = index;
        }

        // Fini quand tous les index strictement inférieurs à la limite sont résolus.
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
