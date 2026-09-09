package com.leaders.app.activities.rules.character;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.R;
import com.leaders.app.activities.BaseActivity;
import com.leaders.app.enums.ActivityTransitionType;
import com.leaders.app.enums.ActivityType;

public class RulesCharacterActivity extends BaseActivity {

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

        // TODO
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_rules_character;
    }

    @Override
    protected int getRootGuidelineResId() {
        return R.id.gdlRoot_actRulesCharacter;
    }

    @Nullable
    @Override
    protected Integer getBtnBackResId() {
        return R.id.btnBack_actRulesCharacter;
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
        return ActivityType.RulesCharacter;
    }

    @Override
    protected void doOnBackPressed() {
        goToActivity(ActivityType.RulesCharacterMenu, ActivityTransitionType.SlideLeft);
    }

    //endregion

    //region VIEWS LISTENER METHODS

    // TODO

    //endregion
}