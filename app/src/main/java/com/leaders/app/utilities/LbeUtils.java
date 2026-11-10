package com.leaders.app.utilities;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.R;
import com.leaders.gamelogic.entities.Board;
import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.entities.Position;
import com.leaders.gamelogic.enums.CharacterType;

public final class LbeUtils {
    public static final String DEFAULT_URL = "https://mthbrt.github.io/Leaders-editor/#";
    public static final String HEADER_WHITE = "white=";
    public static final String HEADER_BLACK = "black=";
    public static final String COLOR_DATA_SEPARATOR = "&";
    public static final String CHARACTER_DATA_SEPARATOR = ":";
    public static final String CELL_DATA_SEPARATOR = ",";

    private final static String POSITION_X_REF_STR = "ABCDEFG";


    private LbeUtils(){
        throw new AssertionError("Cannot instantiate utility class");
    }

    public static String getCharacterExportStr(@Nullable Character character) {
        if (character == null) {
            throw new IllegalArgumentException("Cannot export an empty character");
        }
        return String.valueOf(CharacterTypeUtils.getCharacterExportId(character.getCharacterType()));
    }

    public static CharacterType getCharacterTypeFromExportStr(@Nullable Context context, @NonNull String exportStr) {
        final String errorMsg = String.format(context != null ?
                context.getString(R.string.invalid_lbe_url_character) : "Invalid character in LBE URL: %s",
                exportStr
        );
        try {
            int characterExportId = Integer.parseInt(exportStr);
            for (CharacterType characterType : CharacterType.values()) {
                if (CharacterTypeUtils.getCharacterExportId(characterType) == characterExportId) {
                    return characterType;
                }
            }
            throw new IllegalArgumentException(errorMsg);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(errorMsg);
        }
    }

    public static String getPositionExportStr(@NonNull Position position) {
        return POSITION_X_REF_STR.charAt(position.getX()) +
                String.valueOf(Board.getRowCount(position.getX()) - position.getY());
    }

    public static Position getPositionFromExportStr(@Nullable Context context, @NonNull String exportStr) {
        final String errorMsg = String.format(context != null ?
                context.getString(R.string.invalid_lbe_url_position) : "Invalid position in LBE URL: %s",
                exportStr
        );
        if (exportStr.length() != 2) {
            throw new IllegalArgumentException(errorMsg);
        }

        int x = POSITION_X_REF_STR.indexOf(exportStr.charAt(0));
        if (x == -1) {
            throw new IllegalArgumentException(errorMsg);
        }

        int rowCount = Board.getRowCount(x);
        int y = rowCount - Integer.parseInt(String.valueOf(exportStr.charAt(1)));
        if (y < 0 || y >= rowCount) {
            throw new IllegalArgumentException(errorMsg);
        }

        return new Position(x, y);
    }
}
