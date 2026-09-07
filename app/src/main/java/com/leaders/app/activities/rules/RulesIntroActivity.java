package com.leaders.app.activities.rules;

import android.view.View;

import androidx.annotation.NonNull;

import com.leaders.R;
import com.leaders.app.enums.ActivityType;
import com.leaders.app.enums.RulesChapter;
import com.leaders.app.views.rules.RulesPlayButton;
import com.leaders.app.views.rules.RulesQuestionButton;

public final class RulesIntroActivity extends RulesActivity {
    private RulesPlayButton btnPlay;
    private RulesQuestionButton btnNextStep;


    //region BASE ACTIVITY OVERRIDEN METHODS

    @Override
    protected void initViews() {
        super.initViews();

        btnPlay = findViewById(R.id.btnPlay_actRulesIntro);
        btnNextStep = findViewById(R.id.btnNextStep_actRUlesIntro);
    }

    @Override
    protected void initListeners() {
        super.initListeners();

        btnPlay.setOnClickListener(this::onPlayClick);
        btnNextStep.setOnClickListener(this::onNextStepClick);
    }

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

    @NonNull
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

    //endregion

    //region VIEW LISTENER METHODS

    private void onPlayClick(View v) {
        // TODO
    }

    private void onNextStepClick(View v) {
        // TODO
    }

    //endregion
}