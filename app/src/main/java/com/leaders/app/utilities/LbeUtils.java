package com.leaders.app.utilities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.gamelogic.entities.Board;
import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.entities.Position;
import com.leaders.gamelogic.enums.CharacterType;

import java.util.NoSuchElementException;

public class LbeUtils {
    public static final String LBE_DEFAULT_URL = "https://mthbrt.github.io/Leaders-editor/#";
    public static final String LBE_HEADER_WHITE = "white=";
    public static final String LBE_HEADER_BLACK = "black=";
    public static final String LBE_COLOR_DATA_SEPARATOR = "&";
    public static final String LBE_CHARACTER_DATA_SEPARATOR = ":";
    public static final String LBE_CELL_DATA_SEPARATOR = ",";

    private final static String POSITION_X_REF_STR = "ABCDEFG";

    public static int getCharacterExportId(@NonNull CharacterType characterType) {
        switch (characterType) {
            case Acrobat: return 3;
            case Assassin: return 5;
            case Brewmaster: return 16;
            case Bruiser: return 7;
            case ClawLauncher: return 11;
            case Cub: return 18;
            case Hermit: return 17;
            case Illusionist: return 10;
            case Jailer: return 9;
            case LeaderKing: return 1;
            case LeaderQueen: return 2;
            case Manipulator: return 12;
            case Nemesis: return 13;
            case Protector: return 14;
            case Rider: return 6;
            case RoyalGuard: return 8;
            case Vizier: return 19;
            case Wanderer: return 15;
            case Archer: return 4;
            default: throw new NoSuchElementException("No LBE ID found for: " + characterType);
        }
    }

    public static String getCharacterExportStr(@Nullable Character character) {
        if (character == null) {
            throw new IllegalArgumentException("Cannot export an empty character");
        }
        return String.valueOf(getCharacterExportId(character.getCharacterType()));
    }

    public static String getCharacterExportStr(@NonNull CharacterType characterType) {
        return String.valueOf(getCharacterExportId(characterType));
    }

    public static String getPositionExportStr(@NonNull Position position) {
        return POSITION_X_REF_STR.charAt(position.getX()) +
                String.valueOf(Board.getRowCount(position.getX()) - position.getY());
    }
}
