package com.leaders.app.activities.puzzle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.leaders.R;
import com.leaders.app.activities.BaseActivity;
import com.leaders.app.enums.ActivityTransitionType;
import com.leaders.app.enums.ActivityType;

public final class PuzzleEditorActivity extends BaseActivity {

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_puzzle_editor;
    }

    @Override
    protected int getRootGuidelineResId() {
        return R.id.gdlRoot_actPuzzleEditor;
    }

    @NonNull
    @Override
    protected Integer getBtnBackResId() {
        return R.id.btnBack_actPuzzleEditor;
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
        return true;
    }

    @NonNull
    @Override
    public ActivityType getActivityType() {
        return ActivityType.PuzzleEditor;
    }

    @Override
    protected void doOnBackPressed() {
        if (hasPuzzleBeenEdited()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.alert_dialog_theme);
            builder.setTitle(R.string.back);
            builder.setMessage(R.string.go_back_to_puzzle_menu);
            builder.setPositiveButton(R.string.yes, (dialog, which) -> goBackToPuzzlesMenuActivity());
            builder.setNegativeButton(R.string.no, null);
            builder.show();
        } else {
            goBackToPuzzlesMenuActivity();
        }
    }

    private void goBackToPuzzlesMenuActivity() {
        goToActivity(ActivityType.Main, ActivityTransitionType.SlideLeft);
    }

    private boolean hasPuzzleBeenEdited() {
        // TODO - detect when the loaded puzzle has been edited
        return false;
    }
}