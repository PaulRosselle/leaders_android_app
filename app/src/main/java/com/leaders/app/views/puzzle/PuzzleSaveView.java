package com.leaders.app.views.puzzle;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.leaders.R;

public class PuzzleSaveView extends ConstraintLayout {
    private String defaultName;

    private final EditText edtName;
    private final EditText edtAuthor;


    public PuzzleSaveView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        inflate(context, R.layout.view_puzzle_save, this);

        defaultName = "";
        edtName = findViewById(R.id.edtName_vwPuzzleSave);
        edtAuthor = findViewById(R.id.edtAuthor_vwPuzzleSave);
    }

    public void setDefaultPuzzleName(@NonNull String puzzleName) {
        defaultName = puzzleName;
        if (!puzzleName.isEmpty()) {
            edtName.setHint(defaultName);
        }
    }

    public void setDefaultPuzzleAuthor(@NonNull String puzzleAuthor) {
        if (!puzzleAuthor.isEmpty() && getFormattedText(edtAuthor).isEmpty()) {
            edtAuthor.setText(puzzleAuthor);
        }
    }

    private String getFormattedText(EditText editText) {
        return getFormattedText(editText.getText().toString());
    }


    private String getFormattedText(@NonNull String text) {
        return text.trim();
    }

    public String getPuzzleName() {
        String name = getFormattedText(edtName);
        return name.isEmpty() ? getFormattedText(defaultName) : name;
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
