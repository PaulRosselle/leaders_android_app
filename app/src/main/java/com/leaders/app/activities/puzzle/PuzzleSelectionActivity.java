package com.leaders.app.activities.puzzle;

import android.view.View;

import androidx.annotation.NonNull;

import com.google.android.material.button.MaterialButton;
import com.leaders.R;
import com.leaders.app.activities.BaseActivity;
import com.leaders.app.enums.ActivityType;
import com.leaders.app.views.ActionsMenuView;

public final class PuzzleSelectionActivity extends BaseActivity {
    private enum PuzzleSelectionAction {
        Add,
        Edit,
        Remove,
        Import,
        Export,
        SelectAll,
        UnselectAll;

        private int getIconResId() {
            switch (this) {
                case Add: return R.drawable.icon_add;
                case Edit: return R.drawable.icon_edit;
                case Remove: return R.drawable.icon_remove;
                case Import: return R.drawable.icon_import;
                case Export: return R.drawable.icon_export;
                case SelectAll: return R.drawable.icon_select_all;
                case UnselectAll: return R.drawable.icon_unselect_all;
                default: throw new IllegalStateException("No icon found for puzzle action: " + this);
            }
        }

        private int getTextResId() {
            switch (this) {
                case Add: return R.string.new_puzzle;
                case Edit: return R.string.edit_puzzle;
                case Remove: return R.string.remove;
                case Import: return R.string.import_puzzle;
                case Export: return R.string.export_puzzle;
                case SelectAll: return R.string.select_all;
                case UnselectAll: return R.string.unselect_all;
                default: throw new IllegalStateException("No text found for puzzle action: " + this);
            }
        }

        private View.OnClickListener getOnClickListener(PuzzleSelectionActivity activity) {
            switch (this) {
                case Add: return activity::newPuzzleClick;
                case Edit: return activity::editPuzzleClick;
                case Remove: return activity::removeClick;
                case Import: return activity::importClick;
                case Export: return activity::exportClick;
                case SelectAll: return activity::selectAllClick;
                case UnselectAll: return activity::unselectAllClick;
                default: throw new IllegalStateException("No click listener found for puzzle action: " + this);
            }
        }
    }


    private View vwDialogBg;
    private MaterialButton btnPuzzleActions;
    private ActionsMenuView amvPuzzleActions;

    @Override
    protected void initViews() {
        super.initViews();
        vwDialogBg = findViewById(R.id.vwDialogBg_actPuzzleSelection);
        btnPuzzleActions = findViewById(R.id.btnPuzzleActions_actPuzzleSelection);
        amvPuzzleActions = findViewById(R.id.amvPuzzleActions_actPuzzleSelection);
        for (PuzzleSelectionAction action : PuzzleSelectionAction.values()) {
            amvPuzzleActions.addActionButton(
                    action.getIconResId(),
                    action.getTextResId(),
                    action.ordinal(),
                    action.getOnClickListener(this)
            );
        }
    }

    @Override
    protected void initListeners() {
        super.initListeners();
        vwDialogBg.setOnClickListener(this::hidePuzzleActions);
        btnPuzzleActions.setOnClickListener(this::btnPuzzleActionsClick);
    }

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

    //region PUZZLE ACTIONS METHODS

    private void hidePuzzleActions(View v) {
        amvPuzzleActions.setVisibility(View.GONE);
        vwDialogBg.setVisibility(View.GONE);
    }

    private void btnPuzzleActionsClick(View v) {
        // TODO
        int puzzlesCount = 0;
        boolean displayOfficialPuzzles = true;
        int selectedPuzzlesCount = 0;

        // Before showing the actions menu, we must update the available actions based on the puzzle selection
        amvPuzzleActions.setButtonEnabled(PuzzleSelectionAction.Edit.ordinal(), selectedPuzzlesCount == 1 && !displayOfficialPuzzles);
        amvPuzzleActions.setButtonEnabled(PuzzleSelectionAction.Remove.ordinal(), selectedPuzzlesCount > 0 && !displayOfficialPuzzles);
        amvPuzzleActions.setButtonEnabled(PuzzleSelectionAction.Export.ordinal(), selectedPuzzlesCount > 0);
        amvPuzzleActions.setButtonEnabled(PuzzleSelectionAction.SelectAll.ordinal(), selectedPuzzlesCount < puzzlesCount);
        amvPuzzleActions.setButtonEnabled(PuzzleSelectionAction.UnselectAll.ordinal(), selectedPuzzlesCount > 0);
        amvPuzzleActions.setVisibility(View.VISIBLE);
        vwDialogBg.setVisibility(View.VISIBLE);
    }

    public void newPuzzleClick(View v) {
        // TODO
    }

    public void editPuzzleClick(View v) {
        // TODO
    }

    public void removeClick(View v) {
        // TODO
    }

    public void importClick(View v) {
        // TODO
    }

    public void exportClick(View v) {
        // TODO
    }

    public void selectAllClick(View v) {
        // TODO
    }

    public void unselectAllClick(View v) {
        // TODO
    }

    //endregion
}