package com.leaders.puzzlelogic.utilities.solver;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.actions.CharacterAction;

import java.util.List;

/**
 * Package private destination for solutions discovered during the search.
 */
interface ISolutionCollector {

    /**
     * Adds a solution to the destination.
     *
     * @param solution winning action sequence
     * @throws InterruptedException if the destination blocks and the thread is interrupted
     */
    void add(@NonNull List<CharacterAction> solution) throws InterruptedException;
}
