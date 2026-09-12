package com.leaders.app.activities;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.R;
import com.leaders.app.entities.LeadersApplication;
import com.leaders.app.entities.Settings;
import com.leaders.app.enums.ActivityType;
import com.leaders.app.enums.AnimationSpeed;
import com.leaders.app.enums.HighlightColor;
import com.leaders.app.utilities.JsonUtils;
import com.leaders.app.views.settings.AnimationSpeedView;
import com.leaders.app.views.settings.HighlightColorView;

public class SettingsActivity extends BaseActivity {
    private AnimationSpeedView asvAnimationSpeed;
    private HighlightColorView hcvHighlightColor;

    private Settings settings;


    //region BASE ACTIVITY OVERRIDEN METHODS

    @Override
    protected void initViews() {
        super.initViews();

        asvAnimationSpeed = findViewById(R.id.asvAnimationSpeed_actSettings);
        hcvHighlightColor = findViewById(R.id.hcvHighlightColor_actSettings);
        // TODO
    }

    @Override
    protected void initListeners() {
        super.initListeners();

        asvAnimationSpeed.setChangeListener(this::onAnimationSpeedChange);
        hcvHighlightColor.setChangeListener(this::onColorChange);
        // TODO
    }

    @Override
    protected void initDatas() {
        super.initDatas();

        settings = ((LeadersApplication) getApplication()).getSettings();

        asvAnimationSpeed.setSpeed(settings.getAnimationSpeed());
        hcvHighlightColor.setColor(settings.getHighlightColor());
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

    //region CHANGE LISTENERS

    private void onAnimationSpeedChange(@NonNull AnimationSpeed speed) {
        settings.setAnimationSpeed(speed);
        JsonUtils.saveSettings(this, settings);
    }

    private void onColorChange(@NonNull HighlightColor color) {
        settings.setHighlightColor(color);
        JsonUtils.saveSettings(this, settings);
    }

    //endregion
}