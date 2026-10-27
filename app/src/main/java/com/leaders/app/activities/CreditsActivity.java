package com.leaders.app.activities;

import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.leaders.R;

import com.leaders.app.entities.Contributor;
import com.leaders.app.enums.ActivityType;
import com.leaders.app.utilities.JsonUtils;
import com.leaders.app.views.credits.ContributorView;

public class CreditsActivity extends BaseActivity {
    private static final String PRIVACY_POLICY_URL = "https://paulrosselle.github.io/leaders_android_app/privacy_policy/";
    private static final String LEADERS_WEBSITE_URL = "https://www.leadersthegame.com/";
    private static final String LEADERS_INSTAGRAM_URL = "https://www.instagram.com/letsplayleaders/";
    private static final String LEADERS_DISCORD_URL = "https://discord.gg/aJQwvyQRsK";
    private static final String LEADERS_BLUESKY_URL = "https://bsky.app/profile/leadersthegame.bsky.social";
    private static final String LEADERS_REDDIT_URL = "https://www.reddit.com/r/leadersboardgame/";

    //region BASE ACTIVITY OVERRIDEN METHODS

    @Override
    protected void initListeners() {
        super.initListeners();

        findViewById(R.id.btnWebsite_actCredits).setOnClickListener(this::onWebsiteClick);
        findViewById(R.id.btnInstagram_actCredits).setOnClickListener(this::onInstagramClick);
        findViewById(R.id.btnDiscord_actCredits).setOnClickListener(this::onDiscordClick);
        findViewById(R.id.btnBluesky_actCredits).setOnClickListener(this::onBlueskyClick);
        findViewById(R.id.btnReddit_actCredits).setOnClickListener(this::onRedditClick);
        findViewById(R.id.txvPrivacyPolicy_actCredits).setOnClickListener(this::onPrivacyPolicyClick);
    }

    @Override
    protected void initDatas() {
        super.initDatas();

        LinearLayout llyContributors = findViewById(R.id.llyContributors_actCredits);
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

    private void onWebsiteClick(View v) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(LEADERS_WEBSITE_URL)));
    }

    private void onInstagramClick(View v) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(LEADERS_INSTAGRAM_URL)));
    }

    private void onDiscordClick(View v) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(LEADERS_DISCORD_URL)));
    }

    private void onBlueskyClick(View v) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(LEADERS_BLUESKY_URL)));
    }

    private void onRedditClick(View v) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(LEADERS_REDDIT_URL)));
    }

    private void onPrivacyPolicyClick(View v) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(PRIVACY_POLICY_URL)));
    }

    //endregion

    private LinearLayout.LayoutParams getContributorLayoutParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        params.bottomMargin = (int) (4 * getResources().getDisplayMetrics().density);

        return params;
    }
}