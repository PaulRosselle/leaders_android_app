package com.leaders.app.enums;

public enum AppSection {
    Puzzles,
    Duel,
    Replay,
    Rules,
    Settings,
    Credits;

    public ActivityType getEntranceActivity() {
        switch (this) {
            case Puzzles: return ActivityType.PuzzleMenu;
            case Duel: return ActivityType.DuelSetup;
            case Replay: return ActivityType.ReplayMenu;
            case Rules: return ActivityType.RulesMenu;
            case Settings: return ActivityType.Settings;
            case Credits: return ActivityType.Credits;
            default: throw new IllegalStateException("No entrance activity found for section: " + this);
        }
    }
}
