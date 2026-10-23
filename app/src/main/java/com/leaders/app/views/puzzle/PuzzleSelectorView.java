package com.leaders.app.views.puzzle;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.leaders.R;
import com.leaders.app.views.selector.SelectorView;
import com.leaders.puzzlelogic.entities.PuzzleSave;

@SuppressLint("ViewConstructor")
public final class PuzzleSelectorView extends SelectorView<PuzzleSave> {
    private ImageView imvSolved;
    private TextView txvName;
    private TextView txvAuthor;
    private PuzzleDifficultyView pdvDifficulty;

    public PuzzleSelectorView(@NonNull Context context, @NonNull PuzzleSave puzzleSave) {
        super(context, puzzleSave);
    }

    @Override
    protected void initViews() {
        super.initViews();

        imvSolved = findViewById(R.id.imvPuzzleSolved_vwPuzzleSelector);
        txvName = findViewById(R.id.txvPuzzleName_vwPuzzleSelector);
        txvAuthor = findViewById(R.id.txvPuzzleAuthor_vwPuzzleSelector);
        pdvDifficulty = findViewById(R.id.pdvDifficulty_vwPuzzleSelector);
        pdvDifficulty.setReadonly(true);
    }


    @Override
    protected void initDatas() {
        super.initDatas();

        PuzzleSave puzzleSave = getItem();

        imvSolved.setImageResource(puzzleSave.isSolved() ?
                R.drawable.icon_highlighted_crown : R.drawable.icon_empty_crown
        );

        pdvDifficulty.setDifficulty(puzzleSave.getDifficulty());

        txvName.setText(puzzleSave.getName());

        ConstraintLayout.LayoutParams txvNameLayoutParams = (ConstraintLayout.LayoutParams) txvName.getLayoutParams();

        if (!puzzleSave.getAuthor().isEmpty()) {
            txvNameLayoutParams.verticalBias = 0.0f;
            txvAuthor.setVisibility(VISIBLE);
            txvAuthor.setText(String.format(getResources().getString(R.string.by_author), puzzleSave.getAuthor()));
        } else {
            txvNameLayoutParams.verticalBias = 0.5f;
            txvAuthor.setVisibility(GONE);
        }

        txvName.setLayoutParams(txvNameLayoutParams);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.view_puzzle_selector;
    }

    @Override
    protected int getBtnMainResId() {
        return R.id.btnMain_vwPuzzleSelector;
    }

    @Override
    protected int getImvCheckedResId() {
        return R.id.imvChecked_vwPuzzleSelector;
    }

    @Override
    protected void updateCheckboxVisibleState() {
        imvChecked.setVisibility(isCheckboxVisible() ? VISIBLE : INVISIBLE);
    }

    @Override
    protected void updateCheckedState() {
        imvChecked.setImageResource(isChecked() ? R.drawable.checked_box : R.drawable.unchecked_box);

        int strokeWidth = getResources().getDimensionPixelSize(R.dimen.default_stroke_width);
        int strokeColorId = R.color.font;
        int textColorId = R.color.font;

        if (isChecked()) {
            strokeWidth = (int) (strokeWidth * 1.5f);
            strokeColorId = R.color.white;
            textColorId = R.color.white;
        }

        int textColor = getResources().getColor(textColorId, getContext().getTheme());

        btnMain.setStrokeWidth(strokeWidth);
        btnMain.setStrokeColor(ContextCompat.getColorStateList(getContext(), strokeColorId));

        txvName.setTextColor(textColor);
        txvAuthor.setTextColor(textColor);
    }

    @NonNull
    public PuzzleSave getPuzzleSave() {
        return getItem();
    }
}