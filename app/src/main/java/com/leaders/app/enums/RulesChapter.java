package com.leaders.app.enums;

import com.leaders.R;

public enum RulesChapter {
    Introduction,
    WinTheGame,
    Turn,
    Characters,
    GameModes,
    Barrage,
    FAQ;

    public ActivityType getActivityType() {
        // TODO - handle every Chapter activity
        switch (this) {
            case Introduction: return ActivityType.RulesIntro;
            default: throw new IllegalStateException("No name found for chapter: " + this);
        }
    }
    
    public int getNameResId() {
        switch (this) {
            case Introduction: return R.string.rules_chapter_intro;
            case WinTheGame: return R.string.rules_chapter_win_the_game;
            case Turn: return R.string.rules_chapter_turn;
            case Characters: return R.string.rules_chapter_characters;
            case GameModes: return R.string.rules_chapter_game_modes;
            case Barrage: return R.string.rules_chapter_barrage;
            case FAQ: return R.string.rules_chapter_faq;
            default: throw new IllegalStateException("No name found for chapter: " + this);
        }
    }

    public int getNumberResId() {
        switch (this) {
            case Introduction: return R.string.rules_chapter_number_intro;
            case WinTheGame: return R.string.rules_chapter_number_win_the_game;
            case Turn: return R.string.rules_chapter_number_turn;
            case Characters: return R.string.rules_chapter_number_characters;
            case GameModes: return R.string.rules_chapter_number_game_modes;
            case Barrage: return R.string.rules_chapter_number_barrage;
            case FAQ: return R.string.rules_chapter_number_faq;
            default: throw new IllegalStateException("No name found for chapter: " + this);
        }
    }
}
