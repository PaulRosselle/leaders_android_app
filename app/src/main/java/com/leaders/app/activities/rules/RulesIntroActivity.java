package com.leaders.app.activities.rules;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.R;
import com.leaders.app.enums.ActivityType;
import com.leaders.app.enums.RulesChapter;

public class RulesIntroActivity extends RulesActivity {

    @Override
    protected int getRnvNavigationResId() {
        return R.id.rnvNavigation_actRulesIntro;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_rules_intro;
    }

    @Override
    protected int getRootGuidelineResId() {
        return R.id.gdlRoot_actRulesIntro;
    }

    @Nullable
    @Override
    protected Integer getBtnBackResId() {
        return R.id.btnBack_actRulesIntro;
    }

    @NonNull
    @Override
    public ActivityType getActivityType() {
        return ActivityType.RulesIntro;
    }

    @Override
    public RulesChapter getChapter() {
        return RulesChapter.Introduction;
    }
}