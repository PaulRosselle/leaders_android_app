package com.leaders.app.activities.rules.tutorial;

import androidx.annotation.NonNull;

import com.leaders.app.enums.ActivityType;
import com.leaders.app.enums.TutorialChapter;

public class RulesTutorialActiveAbilitiesActivity extends RulesTutorialActivity {
    @NonNull
    @Override
    public ActivityType getActivityType() {
        return ActivityType.RulesTutorialActiveAbilities;
    }

    @Override
    public TutorialChapter getChapter() {
        return TutorialChapter.ActiveAbilities;
    }
}
