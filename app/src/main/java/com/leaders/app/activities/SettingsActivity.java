package com.leaders.app.activities;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.R;
import com.leaders.app.entities.LeadersApplication;
import com.leaders.app.entities.Settings;
import com.leaders.app.enums.ActivityType;

public class SettingsActivity extends BaseActivity {
    private Settings settings;


    //region BASE ACTIVITY OVERRIDEN METHODS

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

        settings = ((LeadersApplication) getApplication()).getSettings();

        // TODO
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_settings;
    }

    @Override
    protected int getRootGuidelineResId() {
        return R.id.gdlRoot_actSettings;
    }

    @Nullable
    @Override
    protected Integer getBtnBackResId() {
        return R.id.btnBack_actSettings;
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
        return ActivityType.Settings;
    }

    //endregion
}