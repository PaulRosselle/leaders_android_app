package com.leaders.app.activities.puzzle;

import androidx.annotation.NonNull;

import com.leaders.R;
import com.leaders.app.activities.BaseActivity;
import com.leaders.app.enums.ActivityType;

public final class PuzzleSelectionActivity extends BaseActivity {

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_puzzle_selection;
    }

    @Override
    protected int getRootGuidelineResId() {
        return R.id.gdlRoot_actPuzzleSelection;
    }

    @Override
    protected Integer getBtnBackResId() {
        return R.id.btnBack_actPuzzleSelection;
    }

    @Override
    protected boolean isImmersiveActivity() {
        return true;
    }

    @Override
    protected boolean overrideOnBackPressed() {
        return true;
    }

    @Override
    protected boolean askForConfirmationBeforeFinish() {
        return false;
    }

    @NonNull
    @Override
    public ActivityType getActivityType() {
        return ActivityType.PuzzleSelection;
    }
}