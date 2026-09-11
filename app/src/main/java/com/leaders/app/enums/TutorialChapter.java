package com.leaders.app.enums;

import com.leaders.R;

public enum TutorialChapter {
    CaptureLeader,
    ActiveAbilities,
    PassiveAbilities,
    Recruitment,
    SurroundLeader,
    GameVsBot;

    public TutorialChapter getNext() {
        TutorialChapter[] rulesChapters = values();
        if (ordinal() < rulesChapters.length - 1) {
            return values()[ordinal() + 1];
        }
        return null;
    }

    public ActivityType getActivityType() {
        // TODO - handle every Chapter activity
        switch (this) {
            case CaptureLeader: return ActivityType.RulesTutorialCapture;
            case ActiveAbilities: return ActivityType.RulesTutorialActiveAbilities;
            case PassiveAbilities: return ActivityType.RulesTutorialPassiveAbilities;
            case SurroundLeader: return ActivityType.RulesTutorialSurround;
            default: throw new IllegalStateException("No activity type found for chapter: " + this);
        }
    }
    
    public int getNameResId() {
        switch (this) {
            case CaptureLeader: return R.string.rules_chapter_capture_leader;
            case ActiveAbilities: return R.string.rules_chapter_active_abilities;
            case PassiveAbilities: return R.string.rules_chapter_passive_abilities;
            case Recruitment: return R.string.rules_chapter_recruitment;
            case SurroundLeader: return R.string.rules_chapter_surround_leader;
            case GameVsBot: return R.string.rules_chapter_game_vs_bot;
            default: throw new IllegalStateException("No name found for chapter: " + this);
        }
    }

    public int getNumberResId() {
        switch (this) {
            case CaptureLeader: return R.string.rules_chapter_number_capture_leader;
            case ActiveAbilities: return R.string.rules_chapter_number_active_abilities;
            case PassiveAbilities: return R.string.rules_chapter_number_passive_abilities;
            case Recruitment: return R.string.rules_chapter_number_recruitment;
            case SurroundLeader: return R.string.rules_chapter_number_surround_leader;
            case GameVsBot: return R.string.rules_chapter_number_game_vs_bot;
            default: throw new IllegalStateException("No number found for chapter: " + this);
        }
    }

    public int getInfoTextResId() {
        switch (this) {
            case CaptureLeader: return R.string.tutorial_capture_info;
            case ActiveAbilities: return R.string.tutorial_active_abilities_info;
            case PassiveAbilities: return R.string.tutorial_passive_abilities_info;
            case Recruitment: return R.string.rules_chapter_recruitment;
            case SurroundLeader: return R.string.tutorial_surround_info;
            case GameVsBot: return R.string.rules_chapter_game_vs_bot;
            default: throw new IllegalStateException("No info text found for chapter: " + this);
        }
    }

    public int getInfoIconResId() {
        switch (this) {
            case ActiveAbilities: return R.drawable.icon_ability_active;
            case PassiveAbilities: return R.drawable.icon_ability_passive;
            default: return R.drawable.icon_leader;
        }
    }

    public int getTextBeforeResId() {
        switch (this) {
            case CaptureLeader: return R.string.tutorial_capture_before;
            case ActiveAbilities: return R.string.tutorial_active_abilities_before;
            case PassiveAbilities: return R.string.tutorial_passive_abilities_before;
            case Recruitment: return R.string.rules_chapter_recruitment;
            case SurroundLeader: return R.string.tutorial_surround_before;
            case GameVsBot: return R.string.rules_chapter_game_vs_bot;
            default: throw new IllegalStateException("No before text found for chapter: " + this);
        }
    }

    public int getTextAfterResId() {
        switch (this) {
            case CaptureLeader: return R.string.tutorial_capture_after;
            case ActiveAbilities: return R.string.tutorial_active_abilities_after;
            case PassiveAbilities: return R.string.tutorial_passive_abilities_after;
            case Recruitment: return R.string.rules_chapter_recruitment;
            case SurroundLeader: return R.string.tutorial_surround_after;
            case GameVsBot: return R.string.rules_chapter_game_vs_bot;
            default: throw new IllegalStateException("No after text found for chapter: " + this);
        }
    }
}
