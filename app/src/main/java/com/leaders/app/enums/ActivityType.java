package com.leaders.app.enums;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.leaders.app.activities.duel.DuelPlayerActivity;
import com.leaders.app.activities.duel.DuelSetupActivity;
import com.leaders.app.activities.MainActivity;
import com.leaders.app.activities.puzzle.PuzzleEditorActivity;
import com.leaders.app.activities.puzzle.PuzzlePlayerActivity;
import com.leaders.app.activities.puzzle.PuzzleMenuActivity;
import com.leaders.app.activities.puzzle.PuzzleSolverActivity;
import com.leaders.app.activities.replay.ReplayMenuActivity;
import com.leaders.app.activities.replay.ReplayViewerActivity;
import com.leaders.app.activities.rules.RulesIntroActivity;

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
    RulesIntro;

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
            case RulesIntro: return new Intent(context, RulesIntroActivity.class);
            default: throw new NoSuchElementException(String.format("No class found matching %s", this));
        }
    }
}
