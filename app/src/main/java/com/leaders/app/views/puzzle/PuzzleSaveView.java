package com.leaders.app.views.puzzle;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.leaders.R;

public class PuzzleSaveView extends ConstraintLayout {
    private final EditText edtName;
    private final EditText edtAuthor;


    public PuzzleSaveView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        inflate(context, R.layout.view_puzzle_save, this);

        edtName = findViewById(R.id.edtName_vwPuzzleSave);
        edtAuthor = findViewById(R.id.edtAuthor_vwPuzzleSave);
    }

    public void setDefaultPuzzleName(String puzzleName) {
        // Since the name cannot be empty, we can set the default value in the hint
        if (puzzleName != null && !puzzleName.isEmpty()) {
            edtName.setHint(puzzleName);
        }
    }

    public void setDefaultPuzzleAuthor(String puzzleAuthor) {
        if (puzzleAuthor != null && !puzzleAuthor.isEmpty()) {
            edtAuthor.setText(puzzleAuthor);
        }
    }

    private String getFormattedText(EditText edtText) {
        return edtText.getText().toString().trim();
    }

    public String getPuzzleName() {
        return getFormattedText(edtName);
    }

    public String getPuzzleAuthor() {
        return getFormattedText(edtAuthor);
    }

    public void setOnBtnCancelClick(@NonNull OnClickListener onBtnCancelClick) {
        (findViewById(R.id.btnCancel_vwPuzzleSave)).setOnClickListener(onBtnCancelClick);
    }

    public void setOnBtnSaveClick(@NonNull OnClickListener onBtnSaveClick) {
        (findViewById(R.id.btnSave_vwPuzzleSave)).setOnClickListener(onBtnSaveClick);
    }
}
