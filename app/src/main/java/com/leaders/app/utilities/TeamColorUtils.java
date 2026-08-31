package com.leaders.app.utilities;

import android.content.Context;

import androidx.annotation.NonNull;

import com.leaders.R;
import com.leaders.gamelogic.enums.TeamColor;

public final class TeamColorUtils {
    private TeamColorUtils(){
        throw new AssertionError("Cannot instantiate utility class");
    }

    public static String getName(@NonNull Context context, @NonNull TeamColor teamColor) {
        return context.getString(teamColor == TeamColor.Black ? R.string.black_name : R.string.white_name);
    }
}
