package com.leaders.app.activities.replay;

import android.content.Intent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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
        // TODO - show amvActions
    }

    private void onDialogBgClick(View v) {
        // TODO - hide vwDialogBg and amvActions
    }

    //endregion

    //region ACTIONS METHODS

    private void onEditPuzzleClick(View v) {
        // TODO
    }

    private void onRemoveClick(View v) {
        // TODO
    }

    private void onImportClick(View v) {
        // TODO
    }

    private void onExportClick(View v) {
        // TODO
    }

    private void onSelectAllClick(View v) {
        // TODO
    }

    private void onUnselectAllClick(View v) {
        // TODO
    }

    //endregion
}