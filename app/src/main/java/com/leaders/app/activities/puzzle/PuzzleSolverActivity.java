package com.leaders.app.activities.puzzle;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.R;
import com.leaders.app.activities.BaseActivity;
import com.leaders.app.enums.ActivityTransitionType;
import com.leaders.app.enums.ActivityType;

public class PuzzleSolverActivity extends BaseActivity {

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_puzzle_solver;
    }

    @Override
    protected int getRootGuidelineResId() {
        return R.id.gdlRoot_actPuzzleSolver;
    }

    @Nullable
    @Override
    protected Integer getBtnBackResId() {
        return R.id.btnBack_actPuzzleSolver;
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
        return ActivityType.PuzzleSolver;
    }

    @Override
    protected void doOnBackPressed() {
        Intent intent = ActivityType.PuzzleEditor.getIntent(this);
        // TODO - puzzle index
        // TODO - puzzle datas
        goToActivity(intent, ActivityTransitionType.SlideLeft);
    }
}