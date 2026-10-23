package com.leaders.app.views.puzzle;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.R;
import com.leaders.puzzlelogic.enums.PuzzleDifficulty;

import java.util.Arrays;

public class PuzzleDifficultyView extends LinearLayout {
    private PuzzleDifficulty difficulty;

    public PuzzleDifficultyView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setOrientation(HORIZONTAL);

        Arrays.stream(PuzzleDifficulty.values()).forEach(
                difficulty -> addView(getImvDifficulty(), getImvDifficultyLayoutParams())
        );

        // Medium is the default value
        setDifficulty(PuzzleDifficulty.Medium);
        setReadonly(false);
    }

    private ImageView getImvDifficulty() {
        ImageView imvDifficulty = new ImageView(getContext());

        imvDifficulty.setAdjustViewBounds(true);
        imvDifficulty.setImageResource(getImvDifficultyResId(true));
        imvDifficulty.setOnClickListener(this::onImvClick);

        return imvDifficulty;
    }

    private LinearLayout.LayoutParams getImvDifficultyLayoutParams() {
        return new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT, 1);
    }

    private int getImvDifficultyResId(boolean useEmptyRes) {
        return useEmptyRes ? R.drawable.difficulty_marker_empty : R.drawable.difficulty_marker;
    }

    private void onImvClick(View v) {

        setDifficulty(PuzzleDifficulty.values()[indexOfChild(v)]);
    }

    public void setReadonly(boolean readonly) {
        for (PuzzleDifficulty difficulty : PuzzleDifficulty.values()) {
            getChildAt(difficulty.ordinal()).setClickable(!readonly);
        }
    }

    public void setDifficulty(@NonNull PuzzleDifficulty puzzleDifficulty) {
        this.difficulty = puzzleDifficulty;

        for (PuzzleDifficulty difficulty : PuzzleDifficulty.values()) {
            ((ImageView) getChildAt(difficulty.ordinal())).setImageResource(getImvDifficultyResId(
                    difficulty.ordinal() > puzzleDifficulty.ordinal())
            );
        }
    }

    @NonNull
    public PuzzleDifficulty getDifficulty() {
        if (difficulty == null) {
            throw new IllegalStateException("Difficulty should never be null within a PuzzleDifficultyView");
        }

        return difficulty;
    }
}
