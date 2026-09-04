package com.leaders.app.activities.replay;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.button.MaterialButton;
import com.leaders.R;
import com.leaders.app.activities.BaseActivity;
import com.leaders.app.entities.ReplaySave;
import com.leaders.app.enums.ActivityType;
import com.leaders.app.utilities.JsonUtils;
import com.leaders.app.views.ActionsMenuView;
import com.leaders.app.views.replay.ReplaySelectorGroupView;

import java.util.List;

public class ReplaySelectionActivity extends BaseActivity {
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
        // TODO - actions menu buttons
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
        // TODO
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
        // TODO - start a replay (if single selection)
    }

    private void onActionsClick(View v) {
        // TODO - show amvActions
    }

    private void onDialogBgClick(View v) {
        // TODO - hide vwDialogBg and amvActions
    }

    //endregion

    //region ACTIONS METHODS

    // TODO - add methods for subelements of amvActions

    //endregion
}