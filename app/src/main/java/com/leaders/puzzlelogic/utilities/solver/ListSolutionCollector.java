package com.leaders.puzzlelogic.utilities.solver;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.actions.CharacterAction;

import java.util.ArrayList;
import java.util.List;

/**
 * Package private solution collector storing solutions locally.
 */
final class ListSolutionCollector implements ISolutionCollector {
    private final List<List<CharacterAction>> solutions = new ArrayList<>();

    @Override
    public void add(@NonNull List<CharacterAction> solution) {
        solutions.add(new ArrayList<>(solution));
    }

    @NonNull
    List<List<CharacterAction>> getSolutions() {
        return solutions;
    }
}
