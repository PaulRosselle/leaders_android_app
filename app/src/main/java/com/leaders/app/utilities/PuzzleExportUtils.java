package com.leaders.app.utilities;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.entities.Board;
import com.leaders.gamelogic.entities.Cell;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.gamelogic.factories.GameFactory;
import com.leaders.gamelogic.queries.BoardQuery;
import com.leaders.puzzlelogic.entities.PuzzleSave;

import java.util.List;

public final class PuzzleExportUtils {

    private PuzzleExportUtils(){
        throw new AssertionError("Cannot instantiate utility class");
    }

    public static String getLbeUrl(@NonNull PuzzleSave puzzleSave) {
        return getLbeUrl(GameFactory.create(puzzleSave.getPuzzleGameHistory()).getBoard());
    }

    public static String getLbeUrl(@NonNull Board board) {
        // First we get character datas from the board
        String whiteCharacters = getCharacters(board, TeamColor.White);
        String blackCharacters = getCharacters(board, TeamColor.Black);

        // By default, the token datas string is empty
        String tokenDatas = "";

        // If we have datas for both player colors we add them with the color separator
        boolean hasWhiteCharacters = !whiteCharacters.isEmpty();
        boolean hasBlackCharacters = !blackCharacters.isEmpty();
        whiteCharacters = LbeUtils.LBE_HEADER_WHITE + whiteCharacters;
        blackCharacters = LbeUtils.LBE_HEADER_BLACK + blackCharacters;
        if (hasWhiteCharacters && hasBlackCharacters) {
            tokenDatas = whiteCharacters + LbeUtils.LBE_COLOR_DATA_SEPARATOR + blackCharacters;
        } else if (hasWhiteCharacters || hasBlackCharacters) {
            tokenDatas = hasWhiteCharacters ? whiteCharacters : blackCharacters;
        }

        return String.format(LbeUtils.LBE_DEFAULT_URL + tokenDatas);
    }

    private static String getCharacters(@NonNull Board board, @NonNull TeamColor teamColor) {
        List<Cell> characterCells = BoardQuery.findCharacterCells(board, teamColor, null);
        if (characterCells.isEmpty()) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        for (Cell characterCell : characterCells) {
            builder.append(LbeUtils.LBE_CELL_DATA_SEPARATOR);
            builder.append(LbeUtils.getPositionExportStr(characterCell.getPosition()));
            builder.append(LbeUtils.LBE_CHARACTER_DATA_SEPARATOR);
            builder.append(LbeUtils.getCharacterExportStr(characterCell.getCharacter()));
        }

        // Since we're sure to have added at least one character, we can remove the first cell data separator
        return builder.toString();
    }
}
