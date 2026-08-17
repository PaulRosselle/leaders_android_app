package com.leaders.app.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.puzzlelogic.entities.PuzzleSave;

import java.util.ArrayList;
import java.util.List;

public final class PuzzleSelectorGroupView extends LinearLayout {
    public interface OnPuzzleSelectionChangeListener {
        void onPuzzleSelectionChange();
    }


    private boolean singleSelection;
    private OnPuzzleSelectionChangeListener selectionChangeListener;

    public PuzzleSelectorGroupView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        // Default behavior is single puzzle selection
        singleSelection = true;
        setOrientation(VERTICAL);
    }

    public void setPuzzles(@NonNull List<? extends PuzzleSave> puzzleSaves) {
        removeAllViews();
        for (PuzzleSave puzzleSave : puzzleSaves) {
            PuzzleSelectorView psvPuzzle = new PuzzleSelectorView(getContext(), puzzleSave);
            psvPuzzle.setOnPuzzleClickListener(this::onPuzzleClick);
            psvPuzzle.setOnPuzzleLongClickListener(this::onPuzzleLongClick);
            addView(psvPuzzle, getPuzzleLayoutParams());
        }

        if (selectionChangeListener != null) {
            selectionChangeListener.onPuzzleSelectionChange();
        }
    }

    @NonNull
    public List<PuzzleSave> getSelectedPuzzles() {
        List<PuzzleSave> selectedPuzzles = new ArrayList<>();
        for (int i = 0; i < getChildCount(); i++) {
            PuzzleSelectorView psvPuzzle = (PuzzleSelectorView) getChildAt(i);
            if (psvPuzzle.isChecked()) {
                selectedPuzzles.add(psvPuzzle.getPuzzleSave());
            }
        }
        return selectedPuzzles;
    }

    public void clearPuzzleSelection() {
        for (int i = 0; i < getChildCount(); i++) {
            ((PuzzleSelectorView) getChildAt(i)).setChecked(false);
        }
        setSingleSelection(true);
    }

    private void setSingleSelection(boolean singleSelection) {
        this.singleSelection = singleSelection;

        for (int i = 0; i < getChildCount(); i++) {
            ((PuzzleSelectorView) getChildAt(i)).setCheckboxVisible(!singleSelection);
        }
    }

    private LinearLayout.LayoutParams getPuzzleLayoutParams() {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        layoutParams.topMargin = (int) (4 * getResources().getDisplayMetrics().density);
        layoutParams.weight = 1;

        return layoutParams;
    }

    private Integer getClosestSelectedPuzzle(int refPuzzleIdx) {
        int closestIdx = Integer.MAX_VALUE;
        for (int i = 0; i < getChildCount(); i++) {
            if (((PuzzleSelectorView) getChildAt(i)).isChecked() &&
                    Math.abs(i - refPuzzleIdx) < Math.abs((closestIdx - refPuzzleIdx))) {
                closestIdx = i;
            }
        }

        if (closestIdx == Integer.MAX_VALUE) {
            return null;
        }

        return closestIdx;
    }

    private void onPuzzleClick(@NonNull PuzzleSelectorView psvSender) {
        // When multiple puzzles can be selected, a simple click add the puzzle to the selection
        if (!singleSelection) {
            psvSender.setChecked(!psvSender.isChecked());
            if (getSelectedPuzzles().isEmpty()) {
                setSingleSelection(true);
            }
            // On single selection mode, a simple click set the puzzle as the selected one
        } else if (!psvSender.isChecked()) {
            clearPuzzleSelection();
            psvSender.setChecked(true);
        }

        if (selectionChangeListener != null) {
            selectionChangeListener.onPuzzleSelectionChange();
        }
    }

    private boolean onPuzzleLongClick(@NonNull PuzzleSelectorView psvSender) {
        // Long click on an already selected puzzle outside of single selection mode does nothing
        if (!singleSelection && psvSender.isChecked()) {
            return false;
        }

        // A long click on a puzzle during single selection mode starts multi selection mode
        if (singleSelection) {
            clearPuzzleSelection();
            setSingleSelection(false);
            psvSender.setChecked(true);
            // A long click on a puzzle during multi select mode select all the
            // puzzles between the one clicked and the closest selected one
        } else {
            int puzzleIdx = indexOfChild(psvSender);
            Integer closestPuzzleIdx = getClosestSelectedPuzzle(puzzleIdx);
            if (closestPuzzleIdx != null) {
                int startIdx;
                int endIdx;
                if (puzzleIdx < closestPuzzleIdx) {
                    startIdx = puzzleIdx;
                    endIdx = closestPuzzleIdx;
                } else {
                    startIdx = closestPuzzleIdx + 1;
                    endIdx = puzzleIdx + 1;
                }

                for (int i = startIdx; i < endIdx; i++) {
                    ((PuzzleSelectorView) getChildAt(i)).setChecked(true);
                }
            }
        }

        if (selectionChangeListener != null) {
            selectionChangeListener.onPuzzleSelectionChange();
        }
        return true;
    }

    public void setSelectionChangeListener(OnPuzzleSelectionChangeListener selectionChangeListener) {
        this.selectionChangeListener = selectionChangeListener;
    }
}
