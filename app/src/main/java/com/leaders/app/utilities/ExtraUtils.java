package com.leaders.app.utilities;

import android.content.Intent;

import androidx.annotation.NonNull;

public final class ExtraUtils {
    public static final String EXTRA_PUZZLE_SOURCE = "EXTRA_PUZZLE_SOURCE";
    public static final String PUZZLE_SOURCE_OFFICIAL = "PUZZLE_SOURCE_OFFICIAL";
    public static final String PUZZLE_SOURCE_CUSTOM = "PUZZLE_SOURCE_CUSTOM";
    public static final String EXTRA_PUZZLE_INDEX = "EXTRA_PUZZLE_INDEX";
    public static final String EXTRA_PUZZLE_IMPORTED = "EXTRA_PUZZLE_IMPORTED";

    private ExtraUtils(){
        throw new AssertionError("Cannot instantiate utility class");
    }

    @NonNull
    public static String getStringExtra(@NonNull Intent intent, @NonNull String name) {
        String extraStr = intent.getStringExtra(name);
        if (extraStr == null) {
            return "";
        }
        return extraStr;
    }
}
