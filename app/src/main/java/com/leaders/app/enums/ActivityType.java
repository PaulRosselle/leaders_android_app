package com.leaders.app.enums;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.leaders.app.activities.SettingsActivity;
import com.leaders.app.activities.duel.DuelPlayerActivity;
import com.leaders.app.activities.duel.DuelSetupActivity;
import com.leaders.app.activities.MainActivity;
import com.leaders.app.activities.puzzle.PuzzleEditorActivity;
import com.leaders.app.activities.puzzle.PuzzlePlayerActivity;
import com.leaders.app.activities.puzzle.PuzzleMenuActivity;
import com.leaders.app.activities.puzzle.PuzzleSolverActivity;
import com.leaders.app.activities.replay.ReplayMenuActivity;
import com.leaders.app.activities.replay.ReplayViewerActivity;
import com.leaders.app.activities.rules.RulesMenuActivity;
import com.leaders.app.activities.rules.character.RulesCharacterDemoActivity;
import com.leaders.app.activities.rules.character.RulesCharacterMenuActivity;
import com.leaders.app.activities.rules.tutorial.RulesTutorialActiveAbilitiesActivity;
import com.leaders.app.activities.rules.tutorial.RulesTutorialActivity;
import com.leaders.app.activities.rules.tutorial.RulesTutorialCaptureActivity;
import com.leaders.app.activities.rules.tutorial.RulesTutorialGameActivity;
import com.leaders.app.activities.rules.tutorial.RulesTutorialPassiveAbilitiesActivity;
import com.leaders.app.activities.rules.tutorial.RulesTutorialRecruitmentActivity;
import com.leaders.app.activities.rules.tutorial.RulesTutorialSurroundActivity;

import java.util.NoSuchElementException;

public enum ActivityType {
    Main,
    // PUZZLES
    PuzzleMenu,
    PuzzleEditor,
    PuzzleSolver,
    PuzzlePlayer,
    // DUEL
    DuelSetup,
    DuelPlayer,
    // REPLAY
    ReplayMenu,
    ReplayViewer,
    // RULES
    RulesMenu,
    RulesCharacterMenu,
    RulesCharacterDemo,
    RulesTutorialCapture,
    RulesTutorialActiveAbilities,
    RulesTutorialPassiveAbilities,
    RulesTutorialRecruitment,
    RulesTutorialSurround,
    RulesTutorialGame,
    // SETTINGS
    Settings;

    @NonNull
    public Intent getIntent(@NonNull Context context) {
        switch (this) {
            case Main: return new Intent(context, MainActivity.class);
            case PuzzleMenu: return new Intent(context, PuzzleMenuActivity.class);
            case PuzzleEditor: return new Intent(context, PuzzleEditorActivity.class);
            case PuzzleSolver: return new Intent(context, PuzzleSolverActivity.class);
            case PuzzlePlayer: return new Intent(context, PuzzlePlayerActivity.class);
            case DuelSetup: return new Intent(context, DuelSetupActivity.class);
            case DuelPlayer: return new Intent(context, DuelPlayerActivity.class);
            case ReplayMenu: return new Intent(context, ReplayMenuActivity.class);
            case ReplayViewer: return new Intent(context, ReplayViewerActivity.class);
            case RulesMenu: return new Intent(context, RulesMenuActivity.class);
            case RulesCharacterMenu: return new Intent(context, RulesCharacterMenuActivity.class);
            case RulesCharacterDemo: return new Intent(context, RulesCharacterDemoActivity.class);
            case RulesTutorialCapture: return new Intent(context, RulesTutorialCaptureActivity.class);
            case RulesTutorialActiveAbilities: return new Intent(context, RulesTutorialActiveAbilitiesActivity.class);
            case RulesTutorialPassiveAbilities: return new Intent(context, RulesTutorialPassiveAbilitiesActivity.class);
            case RulesTutorialRecruitment: return new Intent(context, RulesTutorialRecruitmentActivity.class);
            case RulesTutorialSurround: return new Intent(context, RulesTutorialSurroundActivity.class);
            case RulesTutorialGame: return new Intent(context, RulesTutorialGameActivity.class);
            case Settings: return new Intent(context, SettingsActivity.class);
            default: throw new NoSuchElementException(String.format("No class found matching %s", this));
        }
    }
}
