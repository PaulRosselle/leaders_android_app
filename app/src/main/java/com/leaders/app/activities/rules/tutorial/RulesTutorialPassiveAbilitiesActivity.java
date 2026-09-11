package com.leaders.app.activities.rules.tutorial;

import androidx.annotation.NonNull;

import com.leaders.app.enums.ActivityType;
import com.leaders.app.enums.TutorialChapter;

public class RulesTutorialPassiveAbilitiesActivity extends RulesTutorialActivity {
    @NonNull
    @Override
    public ActivityType getActivityType() {
        return ActivityType.RulesTutorialPassiveAbilities;
    }

    @Override
    public TutorialChapter getChapter() {
        return TutorialChapter.PassiveAbilities;
    }
}
