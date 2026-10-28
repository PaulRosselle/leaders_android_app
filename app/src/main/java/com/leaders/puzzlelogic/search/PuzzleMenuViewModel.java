package com.leaders.puzzlelogic.search;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.leaders.app.utilities.JsonUtils;
import com.leaders.puzzlelogic.entities.CustomPuzzleSave;
import com.leaders.puzzlelogic.entities.OfficialPuzzleSave;
import com.leaders.puzzlelogic.enums.PuzzleCategory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Loads and persists the puzzles displayed in a menu.
 * <p>
 * The puzzles are loaded only once (they survive configuration changes) and
 * all disk accesses are done off the UI thread, on a single worker thread so
 * that reads and writes are always executed in order.
 */
public final class PuzzleMenuViewModel extends AndroidViewModel {
    public enum LoadState {
        LOADING,
        LOADED,
        ERROR
    }

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final MutableLiveData<LoadState> loadState = new MutableLiveData<>(LoadState.LOADING);

    // These lists are only filled by the worker thread BEFORE the state switches to LOADED,
    // then they are only accessed from the main thread
    private final List<OfficialPuzzleSave> officialPuzzleSaves = new ArrayList<>();
    private final List<CustomPuzzleSave> customPuzzleSaves = new ArrayList<>();

    private boolean loadStarted = false;
    private PuzzleCategory category = PuzzleCategory.Official;

    public PuzzleMenuViewModel(@NonNull Application application) {
        super(application);
    }

    @NonNull
    public LiveData<LoadState> getLoadState() {
        return loadState;
    }

    @NonNull
    public List<OfficialPuzzleSave> getOfficialPuzzleSaves() {
        return officialPuzzleSaves;
    }

    @NonNull
    public List<CustomPuzzleSave> getCustomPuzzleSaves() {
        return customPuzzleSaves;
    }

    @NonNull
    public PuzzleCategory getCategory() {
        return category;
    }

    public void setCategory(@NonNull PuzzleCategory category) {
        this.category = category;
    }

    /**
     * Starts the loading if it has not been done yet (or if the previous attempt failed).
     * Must be called from the main thread.
     */
    public void loadIfNeeded() {
        if (loadStarted) {
            return;
        }
        loadStarted = true;
        loadState.setValue(LoadState.LOADING);

        executor.execute(() -> {
            try {
                List<OfficialPuzzleSave> official = JsonUtils.loadOfficialPuzzles(getApplication());
                List<CustomPuzzleSave> custom = JsonUtils.loadCustomPuzzles(getApplication());

                officialPuzzleSaves.clear();
                officialPuzzleSaves.addAll(official);
                customPuzzleSaves.clear();
                customPuzzleSaves.addAll(custom);

                loadState.postValue(LoadState.LOADED);
            } catch (RuntimeException e) {
                // Allows a new attempt the next time loadIfNeeded() is called
                loadStarted = false;
                loadState.postValue(LoadState.ERROR);
            }
        });
    }

    /**
     * Saves the custom puzzles off the UI thread.
     * A snapshot of the list is taken now, so later modifications cannot interfere with the writing.
     * Must be called from the main thread.
     */
    public void saveCustomPuzzles() {
        customPuzzleSaves.sort(PuzzleSortMode.DIFFICULTY_ASC.getComparator());
        List<CustomPuzzleSave> snapshot = new ArrayList<>(customPuzzleSaves);
        executor.execute(() -> JsonUtils.saveCustomPuzzles(getApplication(), snapshot));
    }

    @Override
    protected void onCleared() {
        // shutdown() and not shutdownNow(): a pending save must be completed
        executor.shutdown();
    }
}