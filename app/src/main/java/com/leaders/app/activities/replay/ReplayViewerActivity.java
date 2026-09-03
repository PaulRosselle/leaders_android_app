package com.leaders.app.activities.replay;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.R;
import com.leaders.app.activities.BaseActivity;
import com.leaders.app.enums.ActivityType;

public class ReplayViewerActivity extends BaseActivity {

    @Override
    protected void initViews() {
        super.initViews();
        // TODO
    }


    @Override
    protected void initListeners() {
        super.initListeners();
        // TODO
    }


    @Override
    protected void initDatas() {
        super.initDatas();
        // TODO
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_replay_viewer;
    }

    @Override
    protected int getRootGuidelineResId() {
        return R.id.gdlRoot_actReplayViewer;
    }

    @Nullable
    @Override
    protected Integer getBtnBackResId() {
        return R.id.btnBack_actReplayViewer;
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
        return ActivityType.ReplayViewer;
    }
}