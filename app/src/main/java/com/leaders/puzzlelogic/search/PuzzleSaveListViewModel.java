package com.leaders.puzzlelogic.search;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.leaders.puzzlelogic.entities.PuzzleSave;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class PuzzleSaveListViewModel extends ViewModel {
    private final MutableLiveData<List<PuzzleSave>> sourceSaves;
    private final MutableLiveData<PuzzleSaveCriteria> criteria;
    private final MediatorLiveData<List<PuzzleSave>> displayedSaves;

    public PuzzleSaveListViewModel() {
        sourceSaves = new MutableLiveData<>(Collections.emptyList());
        criteria = new MutableLiveData<>(PuzzleSaveCriteria.DEFAULT);
        displayedSaves = new MediatorLiveData<>();

        displayedSaves.addSource(sourceSaves, saves -> refresh());
        displayedSaves.addSource(criteria, c -> refresh());
    }

    // Complete, unfiltered list (used to build filter options)
    @NonNull
    public LiveData<List<PuzzleSave>> getSourceSaves() {
        return sourceSaves;
    }

    // Filtered and sorted list, to be passed to PuzzleSaveGroupView
    @NonNull
    public LiveData<List<PuzzleSave>> getDisplayedSaves() {
        return displayedSaves;
    }

    @NonNull
    public PuzzleSaveCriteria getCriteria() {
        return Objects.requireNonNull(criteria.getValue());
    }

    public void setSaves(@NonNull List<? extends PuzzleSave> saves) {
        sourceSaves.setValue(new ArrayList<>(saves));
    }

    public void setSearchKey(@NonNull String searchKey) {
        updateCriteria(getCriteria().withSearchKey(searchKey));
    }

    public void setSortMode(@NonNull PuzzleSortMode sortMode) {
        updateCriteria(getCriteria().withSortMode(sortMode));
    }

    public void setFilters(@NonNull PuzzleSaveFilters filters) {
        updateCriteria(getCriteria().withFilters(filters));
    }

    private void updateCriteria(@NonNull PuzzleSaveCriteria newCriteria) {
        if (!newCriteria.equals(criteria.getValue())) {
            criteria.setValue(newCriteria);
        }
    }

    private void refresh() {
        displayedSaves.setValue(PuzzleSaveSearchEngine.apply(
                Objects.requireNonNull(sourceSaves.getValue()),
                Objects.requireNonNull(criteria.getValue())));
    }
}