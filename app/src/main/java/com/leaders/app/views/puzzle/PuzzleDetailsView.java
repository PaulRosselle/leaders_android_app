package com.leaders.app.views.puzzle;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.leaders.R;
import com.leaders.puzzlelogic.enums.PuzzleDifficulty;

public final class PuzzleDetailsView extends ConstraintLayout {
    private String defaultName;

    private final EditText edtName;
    private final EditText edtAuthor;
    private final PuzzleDifficultyView pdvDifficulty;


    public PuzzleDetailsView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        inflate(context, R.layout.view_puzzle_details, this);

        defaultName = "";
        edtName = findViewById(R.id.edtName_vwPuzzleDetails);
        edtAuthor = findViewById(R.id.edtAuthor_vwPuzzleDetails);
        pdvDifficulty = findViewById(R.id.pdvDifficulty_vwPuzzleDetails);

        try (TypedArray customAttrs = context.obtainStyledAttributes(attrs, R.styleable.PuzzleDetailsView)) {
            TextView txvTitle = findViewById(R.id.txvTitle_vwPuzzleDetails);
            txvTitle.setText(customAttrs.getResourceId(
                    R.styleable.PuzzleDetailsView_titleResId,
                    R.string.save_puzzle
            ));
        }
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

    public void setPuzzleDifficulty(@NonNull PuzzleDifficulty puzzleDifficulty) {
        pdvDifficulty.setDifficulty(puzzleDifficulty);
    }

    private String getFormattedText(EditText editText) {
        return getFormattedText(editText.getText().toString());
    }

    @NonNull
    private String getFormattedText(@NonNull String text) {
        return text.trim();
    }

    @NonNull
    public String getPuzzleName() {
        String name = getFormattedText(edtName);
        return name.isEmpty() ? getFormattedText(defaultName) : name;
    }

    @NonNull
    public String getPuzzleAuthor() {
        return getFormattedText(edtAuthor);
    }

    @NonNull
    public PuzzleDifficulty getPuzzleDifficulty() {
        return pdvDifficulty.getDifficulty();
    }

    public void setOnBtnCancelClick(@NonNull OnClickListener onBtnCancelClick) {
        (findViewById(R.id.txvCancel_vwPuzzleDetails)).setOnClickListener(onBtnCancelClick);
    }

    public void setOnBtnSaveClick(@NonNull OnClickListener onBtnSaveClick) {
        (findViewById(R.id.txvSave_vwPuzzleDetails)).setOnClickListener(onBtnSaveClick);
    }
}
