package com.leaders.app.utilities;

import android.content.Context;

import androidx.annotation.NonNull;

import com.leaders.R;
import com.leaders.gamelogic.enums.GameMode;

public class GameModeUtils {
    private GameModeUtils(){
        throw new AssertionError("Cannot instantiate utility class");
    }

    public static String getName(@NonNull Context context, @NonNull GameMode gameMode) {
        switch (gameMode) {
            case Discovery: return context.getString(R.string.discovery_mode_name);
            case Strategist: return context.getString(R.string.strategist_mode_name);
            default: throw new IllegalStateException("No name found for game mode: " + gameMode);
        }
    }
}
