package com.leaders.app.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.leaders.R;
import com.leaders.puzzlelogic.entities.OfficialPuzzleSave;
import com.leaders.puzzlelogic.entities.PuzzleSave;

import org.json.JSONObject;

public class PuzzleSelectorView extends ConstraintLayout {
    private MaterialButton btnMain;
    private ImageView imvSolved;
    private ImageView imvChecked;
    private boolean isSelectedPuzzle;
    private boolean isChecked;
    private TextView txvName;
    private TextView txvAuthor;
    private final PuzzleSave puzzleSave;


    public PuzzleSelectorView(@NonNull Context context, @Nullable AttributeSet attrs) {
        // TODO - remove
        this(context, new OfficialPuzzleSave(context, 1, false, new JSONObject()));
    }

    public PuzzleSelectorView(@NonNull Context context, @NonNull PuzzleSave puzzleSave) {
        super(context, null);
        this.puzzleSave = puzzleSave;

        inflate(context, R.layout.view_puzzle_selector, this);
        initViews();
        loadPuzzleSave();

        setSelectedPuzzle(false);
        setChecked(false);
    }

    private void initViews() {
        imvSolved = findViewById(R.id.imvPuzzleSolved_vwPuzzleSelector);
        imvChecked = findViewById(R.id.imvChecked_vwPuzzleSelector);
        btnMain = findViewById(R.id.btnMain_vwPuzzleSelector);
        txvName = findViewById(R.id.txvPuzzleName_vwPuzzleSelector);
        txvAuthor = findViewById(R.id.txvPuzzleAuthor_vwPuzzleSelector);

        btnMain.setOnClickListener(v -> setSelectedPuzzle(!isSelectedPuzzle));
        imvSolved.setOnLongClickListener(v -> {
            Toast.makeText(getContext(), "Puzzle non complété", Toast.LENGTH_SHORT).show();
            btnMain.performLongClick();
            return false;
        });
    }

    private void loadPuzzleSave() {
        // SOLVED
        imvSolved.setImageResource(puzzleSave.isSolved() ? R.drawable.icon_highlighted_crown : R.drawable.icon_empty_crown);
        // NAME
        txvName.setText(puzzleSave.getName());
        // AUTHOR
        LayoutParams txvNameLayoutParams = (LayoutParams) txvName.getLayoutParams();
        if (!puzzleSave.getAuthor().isEmpty()) {
            txvNameLayoutParams.verticalBias = 0.0f;
            txvAuthor.setVisibility(VISIBLE);
            txvAuthor.setText(String.format(getResources().getString(R.string.by_author), puzzleSave.getAuthor()));
        } else {
            txvNameLayoutParams.verticalBias = 0.5f;
            txvAuthor.setVisibility(GONE);
        }
    }

    public void setSelectedPuzzle(boolean isSelectedPuzzle) {
        this.isSelectedPuzzle = isSelectedPuzzle;

        int strokeWidth = getResources().getDimensionPixelSize(R.dimen.default_stroke_width);
        int bgColorId = R.color.darker_background;
        int strokeColorId = R.color.font;
        int nameColorId = R.color.font;
        int authorColorId = R.color.font;
        if (isSelectedPuzzle) {
            strokeWidth *= 2;
            bgColorId = R.color.black;
            strokeColorId = R.color.white;
            nameColorId = R.color.white;
            authorColorId = R.color.white;
        }

        btnMain.setStrokeWidth(strokeWidth);
        btnMain.setBackgroundTintList(ContextCompat.getColorStateList(getContext(), bgColorId));
        btnMain.setStrokeColor(ContextCompat.getColorStateList(getContext(), strokeColorId));
        txvName.setTextColor(getResources().getColor(nameColorId, getContext().getTheme()));
        txvAuthor.setTextColor(getResources().getColor(authorColorId, getContext().getTheme()));
    }

    public boolean isSelectedPuzzle() {
        return isSelectedPuzzle;
    }

    public void setChecked(boolean isChecked) {
        this.isChecked = isChecked;
        imvChecked.setImageResource(isChecked ? R.drawable.checked_box : R.drawable.unchecked_box);
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setOnPuzzleClickListener(@NonNull OnClickListener onBtnClickListener) {
        btnMain.setOnClickListener(onBtnClickListener);
    }

    public void setOnPuzzleLongClickListener(@NonNull OnLongClickListener onBtnLongClickListener) {
        btnMain.setOnLongClickListener(onBtnLongClickListener);
    }
}
