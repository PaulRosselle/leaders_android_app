package com.leaders.app.views.puzzle;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

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
        imvDifficulty.setImageTintMode(PorterDuff.Mode.MULTIPLY);

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

        ColorStateList colorTint = ContextCompat.getColorStateList(getContext(),  getTintResId(puzzleDifficulty));

        for (PuzzleDifficulty difficulty : PuzzleDifficulty.values()) {
            ImageView imvDifficulty = (ImageView) getChildAt(difficulty.ordinal());
            imvDifficulty.setImageResource(getImvDifficultyResId(
                    difficulty.ordinal() > puzzleDifficulty.ordinal())
            );
            imvDifficulty.setImageTintList(colorTint);
        }
    }

    @ColorRes
    private int getTintResId(@NonNull PuzzleDifficulty puzzleDifficulty) {
        switch (puzzleDifficulty) {
            case Easy: return R.color.difficulty_easy_multiplicator;
            case Hard: return R.color.difficulty_hard_multiplicator;
            case Extreme: return R.color.difficulty_extreme_multiplicator;
            default: return R.color.white;
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
