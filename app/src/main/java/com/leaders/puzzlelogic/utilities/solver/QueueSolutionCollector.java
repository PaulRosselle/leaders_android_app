package com.leaders.puzzlelogic.utilities.solver;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.actions.CharacterAction;

import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * Package private solution collector publishing solutions to a BlockingQueue.
 */
final class QueueSolutionCollector implements ISolutionCollector {
    private final BlockingQueue<List<CharacterAction>> solutions;

    QueueSolutionCollector(@NonNull BlockingQueue<List<CharacterAction>> solutions) {
        this.solutions = solutions;
    }

    @Override
    public void add(@NonNull List<CharacterAction> solution) throws InterruptedException {
        solutions.put(solution);
    }
}