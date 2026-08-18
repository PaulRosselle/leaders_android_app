package com.leaders.app.enums;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.leaders.app.activities.MainActivity;
import com.leaders.app.activities.PuzzleEditorActivity;
import com.leaders.app.activities.puzzle.PuzzleSelectionActivity;

import java.util.NoSuchElementException;

public enum ActivityType {
    Main,
    // PUZZLES
    PuzzleSelection,
    PuzzleEditor;

    @NonNull
    public Intent getIntent(@NonNull Context context) {
        switch (this) {
            case Main: return new Intent(context, MainActivity.class);
            case PuzzleSelection: return new Intent(context, PuzzleSelectionActivity.class);
            case PuzzleEditor: return new Intent(context, PuzzleEditorActivity.class);
            default: throw new NoSuchElementException(String.format("No class found matching %s", this));
        }
    }
}
