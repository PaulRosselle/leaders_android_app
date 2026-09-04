package com.leaders.app.views.puzzle;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.app.views.selector.SelectorGroupView;
import com.leaders.puzzlelogic.entities.PuzzleSave;

import java.util.List;

public final class PuzzleSelectorGroupView extends SelectorGroupView<PuzzleSave, PuzzleSelectorView> {
    public PuzzleSelectorGroupView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @NonNull
    @Override
    protected PuzzleSelectorView createSelectorView(@NonNull PuzzleSave puzzleSave) {
        return new PuzzleSelectorView(getContext(), puzzleSave);
    }

    public void setPuzzles(@NonNull List<? extends PuzzleSave> puzzleSaves) {
        setItems(puzzleSaves);
    }

    @NonNull
    public List<PuzzleSave> getSelectedPuzzles() {
        return getSelectedItems();
    }

    public void selectAllPuzzles() {
        selectAll();
    }

    public void clearPuzzleSelection() {
        clearSelection();
    }
}