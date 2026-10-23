package com.leaders.puzzlelogic.search;

import androidx.annotation.NonNull;

import java.util.Objects;

public final class PuzzleSaveCriteria {
    public static final PuzzleSaveCriteria DEFAULT = new PuzzleSaveCriteria(
            "",
            PuzzleSortMode.NAME_ASC,
            PuzzleSaveFilters.NONE
    );

    @NonNull
    private final String searchKey;
    @NonNull
    private final PuzzleSortMode sortMode;
    @NonNull
    private final PuzzleSaveFilters filters;

    public PuzzleSaveCriteria(@NonNull String searchKey,
                              @NonNull PuzzleSortMode sortMode,
                              @NonNull PuzzleSaveFilters filters) {
        this.searchKey = searchKey;
        this.sortMode = sortMode;
        this.filters = filters;
    }

    @NonNull
    public PuzzleSaveCriteria withSearchKey(@NonNull String searchKey) {
        return new PuzzleSaveCriteria(searchKey, sortMode, filters);
    }

    @NonNull
    public PuzzleSaveCriteria withSortMode(@NonNull PuzzleSortMode sortMode) {
        return new PuzzleSaveCriteria(searchKey, sortMode, filters);
    }

    @NonNull
    public PuzzleSaveCriteria withFilters(@NonNull PuzzleSaveFilters filters) {
        return new PuzzleSaveCriteria(searchKey, sortMode, filters);
    }

    @NonNull
    public String getSearchKey() {
        return searchKey;
    }

    @NonNull
    public PuzzleSortMode getSortMode() {
        return sortMode;
    }

    @NonNull
    public PuzzleSaveFilters getFilters() {
        return filters;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof PuzzleSaveCriteria)) {
            return false;
        }

        PuzzleSaveCriteria other = (PuzzleSaveCriteria) obj;

        return searchKey.equals(other.searchKey) &&
                sortMode == other.sortMode &&
                filters.equals(other.filters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(searchKey, sortMode, filters);
    }
}