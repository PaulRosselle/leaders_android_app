package com.leaders.puzzlelogic.entities;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class PuzzleImportAnalysis {
    @NonNull
    private final List<CustomPuzzleSave> newPuzzles;

    @NonNull
    private final Map<PuzzleIdentity, String> duplicates;

    public PuzzleImportAnalysis(@NonNull List<CustomPuzzleSave> importedPuzzles,
                                @NonNull List<CustomPuzzleSave> existingPuzzles) {
        Map<PuzzleIdentity, String> existingPuzzleMap = new HashMap<>();

        for (CustomPuzzleSave puzzle : existingPuzzles) {
            existingPuzzleMap.put(new PuzzleIdentity(puzzle), puzzle.getName());
        }

        List<CustomPuzzleSave> newPuzzles = new ArrayList<>();
        Map<PuzzleIdentity, String> duplicates = new LinkedHashMap<>();

        for (CustomPuzzleSave importedPuzzle : importedPuzzles) {
            PuzzleIdentity identity = new PuzzleIdentity(importedPuzzle);

            String existingName = existingPuzzleMap.get(identity);

            if (existingName != null) {
                duplicates.put(identity, existingName);
            } else {
                newPuzzles.add(importedPuzzle);
                // Takes into account duplicates within imported puzzles
                existingPuzzleMap.put(identity, importedPuzzle.getName());
            }
        }

        this.newPuzzles = Collections.unmodifiableList(newPuzzles);
        this.duplicates = Collections.unmodifiableMap(duplicates);
    }

    @NonNull
    public List<CustomPuzzleSave> getNewPuzzles() {
        return newPuzzles;
    }

    @NonNull
    public Map<PuzzleIdentity, String> getDuplicates() {
        return duplicates;
    }
}
