package com.leaders.app.views.settings;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.leaders.R;
import com.leaders.app.utilities.JsonUtils;
import com.leaders.puzzlelogic.entities.CustomPuzzleSave;
import com.leaders.puzzlelogic.search.PuzzleSortMode;

import java.util.List;

public class ResetPuzzleProgressionView extends ConstraintLayout {
    public ResetPuzzleProgressionView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        inflate(context, R.layout.view_reset_puzzle_progression, this);

        setBackgroundResource(R.drawable.round_rect);

        Button btnReset = findViewById(R.id.btnReset_vwResetPuzzleProgression);
        btnReset.setOnClickListener(this::onResetClick);
    }

    private void onResetClick(View v) {
        new AlertDialog.Builder(getContext(), R.style.alert_dialog_theme)
                .setTitle(R.string.puzzle_progress_reset_dialog_title)
                .setMessage(R.string.puzzle_progress_reset_dialog_message)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.reset_all, this::resetPuzzleProgression)
                .show();
    }

    private void resetPuzzleProgression(DialogInterface dialogInterface, int id) {
        List<CustomPuzzleSave> customPuzzleSaves;
        try {
            // Loaded first: if it fails, nothing has been modified
            customPuzzleSaves = JsonUtils.loadCustomPuzzles(getContext());
        } catch (JsonUtils.CorruptedFileException e) {
            Toast.makeText(getContext(), R.string.error_during_progress_reset, Toast.LENGTH_LONG).show();
            return;
        }

        for (CustomPuzzleSave customPuzzleSave : customPuzzleSaves) {
            customPuzzleSave.setSolved(false);
        }
        customPuzzleSaves.sort(PuzzleSortMode.DIFFICULTY_ASC.getComparator());
        JsonUtils.saveCustomPuzzles(getContext(), customPuzzleSaves);

        JsonUtils.clearOfficialPuzzlesSolved(getContext());

        Toast.makeText(getContext(), R.string.progress_reset, Toast.LENGTH_SHORT).show();
    }
}
