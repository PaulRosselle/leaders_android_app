package com.leaders.app.activities.rules;

import android.content.Intent;
import android.net.Uri;
import android.view.View;

import androidx.annotation.NonNull;

import com.google.android.material.button.MaterialButton;
import com.leaders.R;
import com.leaders.app.activities.BaseActivity;
import com.leaders.app.enums.ActivityType;
import com.leaders.app.enums.TutorialChapter;
import com.leaders.app.utilities.ExtraUtils;

public final class RulesMenuActivity extends BaseActivity {
    private static final String OFFICIAL_GUIDE_URL = "https://www.leadersthegame.com/rules";

    private MaterialButton btnTutorial;
    private MaterialButton btnCharacters;
    private MaterialButton btnOnlineGuide;


    //region BASE ACTIVITY OVERRIDEN METHODS

    @Override
    protected void initViews() {
        super.initViews();

        btnTutorial = findViewById(R.id.btnTutorial_actRulesMenu);
        btnCharacters = findViewById(R.id.btnCharacters_actRulesMenu);
        btnOnlineGuide = findViewById(R.id.btnOnlineGuide_actRulesMenu);
    }

    @Override
    protected void initListeners() {
        super.initListeners();

        btnTutorial.setOnClickListener(this::onTutorialClick);
        btnCharacters.setOnClickListener(this::onCharactersClick);
        btnOnlineGuide.setOnClickListener(this::onOnlineGuideClick);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_rules_menu;
    }

    @Override
    protected int getRootGuidelineResId() {
        return R.id.gdlRoot_actRulesMenu;
    }

    @NonNull
    @Override
    protected Integer getBtnBackResId() {
        return R.id.btnBack_actRulesMenu;
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
        return ActivityType.RulesMenu;
    }

    //endregion

    //region VIEWS LISTENER METHODS

    public void onTutorialClick(View v) {
        Intent intent = ActivityType.RulesTutorial.getIntent(this);
        intent.putExtra(ExtraUtils.EXTRA_TUTORIAL_CHAPTER, TutorialChapter.CaptureLeader.name());
        goToActivity(intent);
    }

    public void onCharactersClick(View v) {
        goToActivity(ActivityType.RulesCharacterMenu);
    }

    public void onOnlineGuideClick(View v) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(OFFICIAL_GUIDE_URL)));
    }

    //endregion
}