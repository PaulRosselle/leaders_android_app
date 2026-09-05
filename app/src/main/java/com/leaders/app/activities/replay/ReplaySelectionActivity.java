package com.leaders.app.activities.replay;

import android.content.Intent;
import android.net.Uri;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.documentfile.provider.DocumentFile;

import com.google.android.material.button.MaterialButton;
import com.leaders.R;
import com.leaders.app.activities.BaseActivity;
import com.leaders.app.entities.ReplaySave;
import com.leaders.app.enums.ActivityType;
import com.leaders.app.utilities.ExtraUtils;
import com.leaders.app.utilities.JsonUtils;
import com.leaders.app.views.ActionsMenuView;
import com.leaders.app.views.replay.ReplaySelectorGroupView;

import java.util.List;

public class ReplaySelectionActivity extends BaseActivity {
    private enum ReplaySelectionAction {
        Edit,
        Remove,
        Import,
        Export,
        SelectAll,
        UnselectAll;

        private int getIconResId() {
            switch (this) {
                case Edit: return R.drawable.icon_edit;
                case Remove: return R.drawable.icon_remove;
                case Import: return R.drawable.icon_import;
                case Export: return R.drawable.icon_export;
                case SelectAll: return R.drawable.icon_select_all;
                case UnselectAll: return R.drawable.icon_unselect_all;
                default: throw new IllegalStateException("No icon found for replay selection action: " + this);
            }
        }

        private int getTextResId() {
            switch (this) {
                case Edit: return R.string.edit_puzzle;
                case Remove: return R.string.remove;
                case Import: return R.string.import_puzzle;
                case Export: return R.string.export_puzzle;
                case SelectAll: return R.string.select_all;
                case UnselectAll: return R.string.unselect_all;
                default: throw new IllegalStateException("No text found for replay selection action: " + this);
            }
        }

        private View.OnClickListener getOnClickListener(ReplaySelectionActivity activity) {
            switch (this) {
                case Edit: return activity::onEditPuzzleClick;
                case Remove: return activity::onRemoveClick;
                case Import: return activity::onImportClick;
                case Export: return activity::onExportClick;
                case SelectAll: return activity::onSelectAllClick;
                case UnselectAll: return activity::onUnselectAllClick;
                default: throw new IllegalStateException("No click listener found for replay selection action: " + this);
            }
        }
    }

    private ReplaySelectorGroupView rsgvReplays;

    private MaterialButton btnActions;
    private ActionsMenuView amvActions;
    private View vwDialogBg;

    private List<ReplaySave> replaySaves;

    private ActivityResultLauncher<Uri> exportPuzzleDirectorySelector;


    //region BASE ACTIVITY OVERRIDEN METHODS

    @Override
    protected void initViews() {
        super.initViews();

        rsgvReplays = findViewById(R.id.rsgvReplays_actReplaySelection);

        btnActions = findViewById(R.id.btnActions_actReplaySelection);
        amvActions = findViewById(R.id.amvActions_actReplaySelection);
        for (ReplaySelectionAction action : ReplaySelectionAction.values()) {
            amvActions.addActionButton(
                    action.getIconResId(),
                    action.getTextResId(),
                    action.ordinal(),
                    action.getOnClickListener(this)
            );
        }
        vwDialogBg = findViewById(R.id.vwDialogBg_actReplaySelection);
    }

    @Override
    protected void initListeners() {
        super.initListeners();

        rsgvReplays.setSelectionChangeListener(this::onReplaySelectionChange);

        btnActions.setOnClickListener(this::onActionsClick);
        vwDialogBg.setOnClickListener(this::onDialogBgClick);
    }

    @Override
    protected void initDatas() {
        super.initDatas();

        exportPuzzleDirectorySelector = registerForActivityResult(
                new ActivityResultContracts.OpenDocumentTree(),
                uri -> {
                    if (uri != null) {
                        exportToFile(uri);
                    }
                });

        replaySaves = JsonUtils.loadReplays(this);
        rsgvReplays.setReplays(replaySaves);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_replay_selection;
    }

    @Override
    protected int getRootGuidelineResId() {
        return R.id.gdlRoot_actReplaySelection;
    }

    @Nullable
    @Override
    protected Integer getBtnBackResId() {
        return R.id.btnBack_actReplaySelection;
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
        return ActivityType.ReplaySelection;
    }

    //endregion

    //region VIEWS LISTENER METHODS

    private void onReplaySelectionChange() {
        // A click on a replay outside of selection mode starts the ReplayViewer
        List<ReplaySave> selectedReplays = rsgvReplays.getSelectedReplays();
        if (!rsgvReplays.isSingleSelection() || selectedReplays.size() != 1) {
            return;
        }

        Intent intent = ActivityType.ReplayViewer.getIntent(this);

        ReplaySave replaySave = selectedReplays.get(0);
        intent.putExtra(ExtraUtils.EXTRA_REPLAY_INDEX, replaySaves.indexOf(replaySave));

        goToActivity(intent);
    }

    private void onActionsClick(View v) {
        int replaysCount = replaySaves.size();
        int selectedReplaysCount = rsgvReplays.getSelectedReplays().size();

        // Before showing the actions menu, we must update the available actions based on the replays selection
        amvActions.setButtonEnabled(ReplaySelectionAction.Edit.ordinal(),
                selectedReplaysCount == 1);
        amvActions.setButtonEnabled(ReplaySelectionAction.Remove.ordinal(),
                selectedReplaysCount > 0);
        amvActions.setButtonEnabled(ReplaySelectionAction.Export.ordinal(),
                selectedReplaysCount > 0);
        amvActions.setButtonEnabled(ReplaySelectionAction.SelectAll.ordinal(),
                selectedReplaysCount < replaysCount);
        amvActions.setButtonEnabled(ReplaySelectionAction.UnselectAll.ordinal(),
                selectedReplaysCount > 0);

        setActionsVisible(true);
    }

    private void onDialogBgClick(View v) {
        setActionsVisible(false);
    }

    //endregion

    //region ACTIONS METHODS

    private void onEditPuzzleClick(View v) {
        // TODO
    }

    private void onRemoveClick(View v) {
        List<ReplaySave> selectedReplays = rsgvReplays.getSelectedReplays();

        String dialogTitle;
        if (selectedReplays.size() == 1) {
            dialogTitle = String.format(getString(R.string.remove_selected_puzzle), selectedReplays.get(0).getName());
        } else {
            dialogTitle = getString(R.string.remove_selected_replays);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.alert_dialog_theme);
        builder.setTitle(dialogTitle);
        builder.setMessage(getString(R.string.warning_removal_cannot_be_undone));
        builder.setPositiveButton(R.string.confirm, (dialog, which) -> {
            for (ReplaySave replaySave : selectedReplays) {
                replaySaves.remove(replaySave);
            }
            JsonUtils.saveReplays(this, replaySaves);
            rsgvReplays.setReplays(replaySaves);
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.show();

        setActionsVisible(false);
    }

    private void onImportClick(View v) {
        // TODO
    }

    private void onExportClick(View v) {
        selectExportFile();

        setActionsVisible(false);
    }

    private void onSelectAllClick(View v) {
        rsgvReplays.selectAllReplays();
        setActionsVisible(false);
    }

    private void onUnselectAllClick(View v) {
        rsgvReplays.clearReplaySelection();
        setActionsVisible(false);
    }


    private void setActionsVisible(boolean visible) {
        amvActions.setVisibility(visible ? View.VISIBLE : View.GONE);
        vwDialogBg.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    //endregion

    //region IMPORT/EXPORT METHODS

    private void selectExportFile() {
        exportPuzzleDirectorySelector.launch(null);
    }

    private void exportToFile(@NonNull Uri directoryUri) {
        // Now that the user has chosen a directory to export the replays we can set up
        // an alertDialog with an editText so the user can input the file name
        DocumentFile fileDirectory = DocumentFile.fromTreeUri(this, directoryUri);

        if (fileDirectory == null || !fileDirectory.canWrite()) {
            Toast.makeText(this, R.string.error_export_file_writing, Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.alert_dialog_theme);

        builder.setTitle(R.string.choose_file_name);

        final EditText edtFileName = new EditText(this);
        edtFileName.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
        builder.setView(edtFileName);

        builder.setPositiveButton(R.string.confirm, (dialog, which) -> {
            // If the file already exists, it will be renamed with an incremented value at the end
            String fileName = edtFileName.getText().toString();

            DocumentFile file = fileDirectory.createFile("application/json", fileName);

            if (file == null) {
                Toast.makeText(this, R.string.error_export_file_creation, Toast.LENGTH_SHORT).show();
                return;
            }

            JsonUtils.saveReplaysToFile(this, rsgvReplays.getSelectedReplays(), file.getUri());

            Toast.makeText(this, R.string.export_successful, Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());
        builder.show();
    }

    //endregion
}