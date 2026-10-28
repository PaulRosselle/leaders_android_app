package com.leaders.app.utilities;

import android.content.Context;

import androidx.annotation.NonNull;

import com.leaders.app.entities.ReplaySave;
import com.leaders.puzzlelogic.entities.CustomPuzzleSave;

import java.util.List;

public final class DefaultResourcesUtils {
    private DefaultResourcesUtils(){
        throw new AssertionError("Cannot instantiate utility class");
    }

    public static void loadDefaultResources(@NonNull Context context) {
        loadDefaultCustomPuzzles(context);
        loadDefaultReplays(context);
    }

    private static void loadDefaultReplays(@NonNull Context context) {
        List<ReplaySave> defaultReplays = JsonUtils.loadDefaultReplays(context);
        JsonUtils.saveReplays(context, defaultReplays);
    }

    private static void loadDefaultCustomPuzzles(@NonNull Context context) {
        List<CustomPuzzleSave> defaultCustomPuzzles = JsonUtils.loadDefaultCustomPuzzles(context);
        JsonUtils.saveCustomPuzzles(context, defaultCustomPuzzles);
    }
}
