package com.leaders.puzzlelogic.search;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.puzzlelogic.entities.PuzzleSave;
import com.leaders.puzzlelogic.enums.PuzzleDifficulty;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;


/**
 * Immutable filters. Semantics: OR within a dimension, AND across dimensions.
 * An empty set (or solved == null) means "no filter on this dimension".
 */
public final class PuzzleSaveFilters {
    public static final PuzzleSaveFilters NONE = new PuzzleSaveFilters(
            Collections.emptySet(), Collections.emptySet(), null
    );

    @NonNull
    private final Set<PuzzleDifficulty> difficulties;
    @NonNull
    private final Set<String> authors;
    @Nullable
    private final Boolean solved;

    public PuzzleSaveFilters(@NonNull Set<PuzzleDifficulty> difficulties,
                             @NonNull Set<String> authors,
                             @Nullable Boolean solved) {
        this.difficulties = Set.copyOf(difficulties);
        this.authors = Set.copyOf(authors);
        this.solved = solved;
    }

    public boolean matches(@NonNull PuzzleSave save) {
        return (difficulties.isEmpty() || difficulties.contains(save.getDifficulty())) &&
                (authors.isEmpty() || authors.contains(save.getAuthor())) &&
                (solved == null || solved == save.isSolved());
    }

    public boolean isEmpty() {
        return difficulties.isEmpty() && authors.isEmpty() && solved == null;
    }

    @NonNull
    public Set<PuzzleDifficulty> getDifficulties() {
        return difficulties;
    }

    @NonNull
    public Set<String> getAuthors() {
        return authors;
    }

    @Nullable
    public Boolean getSolved() {
        return solved;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PuzzleSaveFilters)) return false;
        PuzzleSaveFilters other = (PuzzleSaveFilters) o;
        return difficulties.equals(other.difficulties) &&
                authors.equals(other.authors) &&
                Objects.equals(solved, other.solved);
    }

    @Override
    public int hashCode() {
        return Objects.hash(difficulties, authors, solved);
    }
}