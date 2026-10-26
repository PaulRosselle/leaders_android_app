package com.leaders.app.views.puzzle;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.leaders.R;
import com.leaders.app.views.selector.SelectorView;
import com.leaders.puzzlelogic.entities.OfficialPuzzleSave;
import com.leaders.puzzlelogic.entities.PuzzleSave;
import com.leaders.puzzlelogic.enums.PuzzleDifficulty;

@SuppressLint("ViewConstructor")
public final class PuzzleSelectorView extends SelectorView<PuzzleSave> {
    private ImageView imvSolved;
    private TextView txvId;
    private TextView txvName;
    private TextView txvAuthor;
    private PuzzleDifficultyView pdvDifficulty;

    public PuzzleSelectorView(@NonNull Context context, @NonNull PuzzleSave puzzleSave) {
        super(context, puzzleSave);
    }

    @Override
    protected void initViews() {
        super.initViews();

        imvSolved = findViewById(R.id.imvSolved_vwPuzzleSelector);
        txvId = findViewById(R.id.txvId_vwPuzzleSelector);
        txvName = findViewById(R.id.txvName_vwPuzzleSelector);
        txvAuthor = findViewById(R.id.txvAuthor_vwPuzzleSelector);
        pdvDifficulty = findViewById(R.id.pdvDifficulty_vwPuzzleSelector);
        pdvDifficulty.setReadonly(true);
    }


    @Override
    protected void initDatas() {
        super.initDatas();

        PuzzleSave puzzleSave = getItem();

        applySolvedState(puzzleSave);

        pdvDifficulty.setDifficulty(puzzleSave.getDifficulty());

        applyIdentity(puzzleSave);
    }

    private void applySolvedState(@NonNull PuzzleSave puzzleSave) {
        boolean solved = puzzleSave.isSolved();

        imvSolved.setImageTintList(ContextCompat.getColorStateList(
                getContext(), getTintResId(puzzleSave.getDifficulty()))
        );
        imvSolved.setAlpha(solved ? 0.8f : 0.5f);
        btnMain.setBackgroundTintList(ContextCompat.getColorStateList(getContext(),
                solved ? R.color.transparent_golden : R.color.darker_background
        ));
    }

    @ColorRes
    private int getTintResId(@NonNull PuzzleDifficulty puzzleDifficulty) {
        switch (puzzleDifficulty) {
            case Easy: return R.color.difficulty_easy;
            case Hard: return R.color.difficulty_hard;
            case Extreme: return R.color.difficulty_extreme;
            default: return R.color.app_golden;
        }
    }

    private void applyIdentity(@NonNull PuzzleSave puzzleSave) {
        if (puzzleSave instanceof OfficialPuzzleSave) {
            applyOfficialIdentity(puzzleSave);
        } else {
            applyCustomIdentity(puzzleSave);
        }
    }

    private void applyOfficialIdentity(@NonNull PuzzleSave puzzleSave) {
        txvId.setVisibility(VISIBLE);
        txvId.setText(String.valueOf(((OfficialPuzzleSave) puzzleSave).getId()));

        txvName.setVisibility(GONE);
        txvAuthor.setVisibility(GONE);
    }

    private void applyCustomIdentity(@NonNull PuzzleSave puzzleSave) {
        txvId.setVisibility(GONE);
        txvName.setVisibility(VISIBLE);
        txvName.setText(puzzleSave.getName());

        String author = puzzleSave.getAuthor();

        if (author.isEmpty()) {
            txvAuthor.setVisibility(GONE);
            return;
        }

        txvAuthor.setVisibility(VISIBLE);
        txvAuthor.setText(
                getResources().getString(R.string.by_author, author)
        );
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