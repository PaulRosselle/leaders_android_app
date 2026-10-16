package com.leaders.puzzlelogic.entities.blocker;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.actions.CharacterAction;

final class ActionCandidate {
    enum Status {
        ACTIVE,
        INVALIDATED,
        FOUND
    }

    @NonNull
    final CharacterAction action;

    // Action position (generally in a coordinator)
    final int actionIndex;

    // Total permutation count to test for this action
    final long permutationCount;

    // Synchronized fields
    private long nextPermutation = 0;
    private long completedCount = 0;

    // Allow workers to access quickly the action state
    private volatile Status status = Status.ACTIVE;

    ActionCandidate(@NonNull CharacterAction action, int actionIndex, long permutationCount) {
        if (actionIndex < 0) {
            throw new IllegalArgumentException("actionIndex must be >= 0");
        }

        if (permutationCount <= 0) {
            throw new IllegalArgumentException("permutationCount must be > 0");
        }

        this.action = action;
        this.actionIndex = actionIndex;
        this.permutationCount = permutationCount;
    }

    /**
     * Atomically reserves the next available permutation index.
     *
     * @return the reserved index, or {@code -1} if the candidate is not active or no permutations remain.
     */
    synchronized long reservePermutation() {
        if (status != Status.ACTIVE) {
            return -1;
        }

        if (nextPermutation >= permutationCount) {
            return -1;
        }

        return nextPermutation++;
    }

    /**
     * Reports the result of a completed permutation.
     *
     * @param canBlock {@code true} if the permutation confirms that the candidate
     * can block the opponent, {@code false} otherwise.
     * @return the updated status, or the current status if the candidate is not active
     */
    synchronized Status reportResult(boolean canBlock) {
        if (status != Status.ACTIVE) {
            return status;
        }

        if (!canBlock) {
            status = Status.INVALIDATED;
            notifyAll();
            return status;
        }

        completedCount++;

        if (completedCount == permutationCount) {
            status = Status.FOUND;
        }

        notifyAll();
        return status;
    }

    /**
     * Checks whether the computation should continue.
     *
     * @return {@code true} if the candidate is active, {@code false} otherwise
     */
    boolean isActive() {
        return status == Status.ACTIVE;
    }

    Status getStatus() {
        return status;
    }

    synchronized long getCompletedCount() {
        return completedCount;
    }

    synchronized long getNextPermutationIndex() {
        return nextPermutation;
    }

    synchronized boolean allPermutationsAssigned() {
        return nextPermutation >= permutationCount;
    }

    /**
     * Waits for progress when all permutations have been assigned but some remain incomplete.
     *
     * @throws InterruptedException if the current thread is interrupted while waiting
     */
    synchronized void awaitProgress() throws InterruptedException {
        while (status == Status.ACTIVE
                && allPermutationsAssigned()
                && completedCount < permutationCount) {
            wait();
        }
    }
}
