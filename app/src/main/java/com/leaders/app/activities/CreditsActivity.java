package com.leaders.app.activities;

import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.google.android.material.button.MaterialButton;
import com.leaders.R;

import com.leaders.app.entities.Contributor;
import com.leaders.app.enums.ActivityType;
import com.leaders.app.utilities.JsonUtils;
import com.leaders.app.views.credits.ContributorView;

public class CreditsActivity extends BaseActivity {
    private static final String PRIVACY_POLICY_URL = "https://paulrosselle.github.io/leaders_android_app/privacy_policy/";

    private LinearLayout llyContributors;
    private MaterialButton btnPrivacyPolicy;

    //region BASE ACTIVITY OVERRIDEN METHODS

    @Override
    protected void initViews() {
        super.initViews();

        llyContributors = findViewById(R.id.llyContributors_actCredits);
        btnPrivacyPolicy = findViewById(R.id.btnPrivacyPolicy_actCredits);
    }

    @Override
    protected void initListeners() {
        super.initListeners();

        btnPrivacyPolicy.setOnClickListener(this::onPrivacyPolicyClick);
    }

    @Override
    protected void initDatas() {
        super.initDatas();

        for (Contributor contributor : JsonUtils.loadContributors(this)) {
            ContributorView contributorView = new ContributorView(this, contributor);
            llyContributors.addView(contributorView, getContributorLayoutParams());
        }
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_credits;
    }

    @Override
    protected int getRootGuidelineResId() {
        return R.id.gdlRoot_actCredits;
    }

    @NonNull
    @Override
    protected Integer getBtnBackResId() {
        return R.id.btnBack_actCredits;
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
        return ActivityType.Credits;
    }

    //endregion

    //region ON CLICK LISTENERS

    private void onPrivacyPolicyClick(View v) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(PRIVACY_POLICY_URL)));
    }

    //endregion

    private LinearLayout.LayoutParams getContributorLayoutParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        params.topMargin = (int) (4 * getResources().getDisplayMetrics().density);

        return params;
    }
}