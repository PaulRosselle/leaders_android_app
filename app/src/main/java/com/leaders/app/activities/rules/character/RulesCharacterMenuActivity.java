package com.leaders.app.activities.rules.character;

import android.content.Intent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.R;
import com.leaders.app.activities.BaseActivity;
import com.leaders.app.enums.ActivityTransitionType;
import com.leaders.app.enums.ActivityType;
import com.leaders.app.views.portrait.AbilityPortraitGroupView;

import java.util.List;

public class RulesCharacterMenuActivity extends BaseActivity {

    //region BASE ACTIVITY OVERRIDEN METHODS

    @Override
    protected void initListeners() {
        super.initListeners();

        AbilityPortraitGroupView apgvLeaders = findViewById(R.id.apgvLeaders_actRulesCharacterMenu);
        AbilityPortraitGroupView apgvActive = findViewById(R.id.apgvActive_actRulesCharacterMenu);
        AbilityPortraitGroupView apgvPassive = findViewById(R.id.apgvPassive_actRulesCharacterMenu);
        AbilityPortraitGroupView apgvSpecial = findViewById(R.id.apgvSpecial_actRulesCharacterMenu);

        for (AbilityPortraitGroupView apgvGroup : List.of(apgvLeaders, apgvActive, apgvPassive, apgvSpecial)) {
            apgvGroup.setOnPortraitClickListener(this::onPortraitClick);
        }
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_rules_character_menu;
    }

    @Override
    protected int getRootGuidelineResId() {
        return R.id.gdlRoot_actRulesCharacterMenu;
    }

    @Nullable
    @Override
    protected Integer getBtnBackResId() {
        return R.id.btnBack_actRulesCharacterMenu;
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
        return ActivityType.RulesCharacterMenu;
    }

    @Override
    protected void doOnBackPressed() {
        goToActivity(ActivityType.RulesMenu, ActivityTransitionType.SlideLeft);
    }

    //endregion

    //region VIEWS LISTENER METHODS

    private void onPortraitClick(View v) {
        Intent intent = ActivityType.RulesCharacter.getIntent(this);

        // TODO - send character data

        goToActivity(intent);
    }

    //endregion
}