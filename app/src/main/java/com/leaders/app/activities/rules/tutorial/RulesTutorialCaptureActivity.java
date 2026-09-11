package com.leaders.app.activities.rules.tutorial;

import androidx.annotation.NonNull;

import com.leaders.app.enums.ActivityType;
import com.leaders.app.enums.TutorialChapter;

public final class RulesTutorialCaptureActivity extends RulesTutorialActivity {
    @Override
    public TutorialChapter getChapter() {
        return TutorialChapter.CaptureLeader;
    }

    @NonNull
    @Override
    public ActivityType getActivityType() {
        return ActivityType.RulesTutorialCapture;
    }
}
