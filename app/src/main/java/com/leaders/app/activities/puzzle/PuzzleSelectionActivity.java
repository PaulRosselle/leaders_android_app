package com.leaders.app.activities.puzzle;

import android.app.AlertDialog;
import android.content.Intent;
import android.view.View;

import androidx.annotation.NonNull;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.leaders.R;
import com.leaders.app.activities.BaseActivity;
import com.leaders.app.enums.ActivityType;
import com.leaders.app.utilities.ButtonUtils;
import com.leaders.app.utilities.ExtraUtils;
import com.leaders.app.utilities.JsonUtils;
import com.leaders.app.utilities.PuzzleExportUtils;
import com.leaders.app.utilities.PuzzleImportUtils;
import com.leaders.app.views.ActionsMenuView;
import com.leaders.app.views.puzzle.PuzzleSelectorGroupView;
import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.puzzlelogic.entities.CustomPuzzleSave;
import com.leaders.puzzlelogic.entities.OfficialPuzzleSave;
import com.leaders.puzzlelogic.entities.PuzzleSave;
import com.leaders.puzzlelogic.enums.PuzzleCategory;

import java.util.List;

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
                case Add: return activity::onNewPuzzleClick;
                case Edit: return activity::onEditPuzzleClick;
                case Remove: return activity::onRemoveClick;
                case Import: return activity::onImportClick;
                case Export: return activity::onExportClick;
                case SelectAll: return activity::onSelectAllClick;
                case UnselectAll: return activity::onUnselectAllClick;
                default: throw new IllegalStateException("No click listener found for puzzle action: " + this);
            }
        }
    }

    private View vwDialogBg;
    private MaterialButton btnPuzzleActions;
    private MaterialButton btnPlay;
    private ActionsMenuView amvPuzzleActions;
    private MaterialButtonToggleGroup mbtgPuzzlesCategory;
    private PuzzleSelectorGroupView psgvPuzzles;
    private PuzzleCategory puzzlesCategory;

    private List<OfficialPuzzleSave> officialPuzzleSaves;
    private List<CustomPuzzleSave> customPuzzleSaves;

    @Override
    protected void initViews() {
        super.initViews();

        vwDialogBg = findViewById(R.id.vwDialogBg_actPuzzleSelection);
        btnPlay = findViewById(R.id.btnPlay_actPuzzleSelection);
        btnPuzzleActions = findViewById(R.id.btnPuzzleActions_actPuzzleSelection);
        amvPuzzleActions = findViewById(R.id.amvPuzzleActions_actPuzzleSelection);
        mbtgPuzzlesCategory = findViewById(R.id.mbtgPuzzlesCategory_actPuzzleSelection);
        psgvPuzzles = findViewById(R.id.psgvPuzzles_actPuzzleSelection);

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
        psgvPuzzles.setSelectionChangeListener(this::onPuzzleSelectionChange);
        mbtgPuzzlesCategory.addOnButtonCheckedListener(this::onPuzzleCategoryChange);
    }

    @Override
    protected void initDatas() {
        super.initDatas();

        officialPuzzleSaves = JsonUtils.loadOfficialPuzzles(this);
        customPuzzleSaves = JsonUtils.loadCustomPuzzles(this);

        // This will cause "onPuzzleSelectionChange" to be called
        ((MaterialButton) (findViewById(R.id.btnOfficial_actPuzzleSelection))).setChecked(true);
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

    private List<? extends PuzzleSave> getPuzzlesFromCategory() {
        return puzzlesCategory == PuzzleCategory.Official ? officialPuzzleSaves : customPuzzleSaves;
    }

    private void onPuzzleCategoryChange(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
        if (!isChecked) {
            return;
        }

        puzzlesCategory = checkedId == R.id.btnOfficial_actPuzzleSelection ?
                PuzzleCategory.Official : PuzzleCategory.Custom;
        psgvPuzzles.setPuzzles(getPuzzlesFromCategory());
    }

    private void onPuzzleSelectionChange() {
        // A single puzzle must be selected
        ButtonUtils.setButtonEnabled(btnPlay, psgvPuzzles.getSelectedPuzzles().size() == 1);
    }

    //region PUZZLE ACTIONS METHODS

    private void hidePuzzleActions(View v) {
        amvPuzzleActions.setVisibility(View.GONE);
        vwDialogBg.setVisibility(View.GONE);
    }

    private void btnPuzzleActionsClick(View v) {
        int puzzlesCount = getPuzzlesFromCategory().size();
        int selectedPuzzlesCount = psgvPuzzles.getSelectedPuzzles().size();

        // Before showing the actions menu, we must update the available actions based on the puzzle selection
        amvPuzzleActions.setButtonEnabled(PuzzleSelectionAction.Edit.ordinal(),
                selectedPuzzlesCount == 1 && puzzlesCategory == PuzzleCategory.Custom);
        amvPuzzleActions.setButtonEnabled(PuzzleSelectionAction.Remove.ordinal(),
                selectedPuzzlesCount > 0 && puzzlesCategory == PuzzleCategory.Custom);
        amvPuzzleActions.setButtonEnabled(PuzzleSelectionAction.Export.ordinal(),
                selectedPuzzlesCount > 0);
        amvPuzzleActions.setButtonEnabled(PuzzleSelectionAction.SelectAll.ordinal(),
                selectedPuzzlesCount < puzzlesCount);
        amvPuzzleActions.setButtonEnabled(PuzzleSelectionAction.UnselectAll.ordinal(),
                selectedPuzzlesCount > 0);

        amvPuzzleActions.setVisibility(View.VISIBLE);
        vwDialogBg.setVisibility(View.VISIBLE);
    }

    public void onNewPuzzleClick(View v) {
        goToActivity(ActivityType.PuzzleEditor);
    }

    public void onEditPuzzleClick(View v) {
        Intent intent = ActivityType.PuzzleEditor.getIntent(this);
        int puzzleIdx = customPuzzleSaves.indexOf((CustomPuzzleSave) psgvPuzzles.getSelectedPuzzles().get(0));
        intent.putExtra(ExtraUtils.EXTRA_PUZZLE_INDEX, puzzleIdx);
        goToActivity(intent);
    }

    public void onRemoveClick(View v) {
        if (puzzlesCategory == PuzzleCategory.Official) {
            throw new IllegalStateException("Official puzzle removal is forbidden");
        }

        List<PuzzleSave> selectedPuzzles = psgvPuzzles.getSelectedPuzzles();

        String dialogTitle;
        if (selectedPuzzles.size() == 1) {
            String puzzleName = selectedPuzzles.get(0).getName();
            dialogTitle = String.format(getString(R.string.remove_selected_puzzle), puzzleName);
        } else {
            dialogTitle = getString(R.string.remove_selected_puzzles);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.alert_dialog_theme);
        builder.setTitle(dialogTitle);
        builder.setMessage(getString(R.string.warning_removal_cannot_be_undone));
        builder.setPositiveButton(R.string.confirm, (dialog, which) -> {
            for (PuzzleSave puzzleSave : selectedPuzzles) {
                customPuzzleSaves.remove((CustomPuzzleSave) puzzleSave);
                JsonUtils.saveCustomPuzzles(this, customPuzzleSaves);
            }
            psgvPuzzles.setPuzzles(customPuzzleSaves);
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.show();

        hidePuzzleActions(null);
    }

    public void onImportClick(View v) {
        // TODO - import from file ?
        GameHistory gameHistory = PuzzleImportUtils.importPuzzleFromClipboard(this);
        if (gameHistory != null) {
            CustomPuzzleSave importedPuzzleSave = CustomPuzzleSave.getDefault(gameHistory);
            customPuzzleSaves.add(importedPuzzleSave);
            JsonUtils.saveCustomPuzzles(this, customPuzzleSaves);

            Intent intent = ActivityType.PuzzleEditor.getIntent(this);
            int puzzleIdx = customPuzzleSaves.indexOf(importedPuzzleSave);
            intent.putExtra(ExtraUtils.EXTRA_PUZZLE_INDEX, puzzleIdx);
            intent.putExtra(ExtraUtils.EXTRA_PUZZLE_IMPORTED, true);
            goToActivity(intent);

        } else {
            hidePuzzleActions(null);
        }
    }

    public void onExportClick(View v) {
        // TODO - export to file ?
        StringBuilder builder = new StringBuilder();
        for (PuzzleSave puzzleSave : psgvPuzzles.getSelectedPuzzles()) {
            builder.append(puzzleSave.getName());
            if (!puzzleSave.getAuthor().isEmpty()) {
                builder.append(String.format(getString(R.string.by_author), puzzleSave.getAuthor()));
            }
            builder.append("\n");
            builder.append(PuzzleExportUtils.getLbeUrl(puzzleSave));
            builder.append("\n\n");
        }

        PuzzleExportUtils.exportAsTextIntent(this, builder.toString().trim());

        hidePuzzleActions(null);
    }

    public void onSelectAllClick(View v) {
        // TODO
    }

    public void onUnselectAllClick(View v) {
        // TODO
    }

    //endregion
}