package com.leaders.app.utilities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.R;
import com.leaders.gamelogic.actions.IGameAction;
import com.leaders.gamelogic.actions.RecruitmentAction;
import com.leaders.gamelogic.actions.RecruitmentActionMotion;
import com.leaders.gamelogic.entities.Cell;
import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.gamelogic.entities.Position;
import com.leaders.gamelogic.enums.CharacterType;
import com.leaders.gamelogic.enums.RecruitmentMotionType;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.puzzlelogic.utilities.PuzzleEditionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class PuzzleImportUtils {
    private PuzzleImportUtils(){
        throw new AssertionError("Cannot instantiate utility class");
    }

    public static GameHistory getFromLbeUrl(@NonNull Context context, @NonNull String lbeUrl) {
        List<IGameAction> initialActions = new ArrayList<>();
        for (TeamColor teamColor : TeamColor.values()) {
            for (Cell characterCell : getTeamCharacterCells(context, lbeUrl, teamColor)) {
                initialActions.add(new RecruitmentAction(List.of(
                        new RecruitmentActionMotion(RecruitmentMotionType.Add,
                                characterCell.getCharacter(),
                                characterCell.getPosition())
                        ))
                );
            }
        }

        return PuzzleEditionUtils.getDefaultHistory(initialActions);
    }

    private static List<Cell> getTeamCharacterCells(@NonNull Context context,
                                                    @NonNull String lbeUrl,
                                                    @NonNull TeamColor teamColor) {
        // First we get the header separator associated with the player color
        String colorHeader = teamColor == TeamColor.Black ? LbeUtils.HEADER_BLACK : LbeUtils.HEADER_WHITE;

        // If the header cannot be found within the URL, we have no data to extract
        int headerIdx = lbeUrl.indexOf(colorHeader);
        if (headerIdx == -1) {
            return Collections.emptyList();
        }

        // The end of the data section can either be marked with
        // the color separator or with the end of the url
        int cellDatasStartIdx = headerIdx + colorHeader.length();
        int colorSeparatorIdx = lbeUrl.indexOf(LbeUtils.COLOR_DATA_SEPARATOR, cellDatasStartIdx);
        String cellDatas;
        if (colorSeparatorIdx != -1) {
            cellDatas = lbeUrl.substring(cellDatasStartIdx, colorSeparatorIdx);
        } else {
            cellDatas = lbeUrl.substring(cellDatasStartIdx);
        }

        List<Cell> characterCells = new ArrayList<>();

        // Once we have isolated the tiles datas we can extract each token and add them immediately to the board
        for (String cellData : cellDatas.split(LbeUtils.CELL_DATA_SEPARATOR)) {
            if (cellData.isEmpty()) {
                continue;
            }
            String[] characterDatas = cellData.split(LbeUtils.CHARACTER_DATA_SEPARATOR);
            Position position = LbeUtils.getPositionFromExportStr(context, characterDatas[0].toUpperCase());
            CharacterType characterType = LbeUtils.getCharacterTypeFromExportStr(context, characterDatas[1].toUpperCase());

            Cell characterCell = new Cell(position);
            characterCell.setCharacter(Character.create(characterType, teamColor));
            characterCells.add(characterCell);
        }

        return characterCells;
    }


    @Nullable
    public static GameHistory importPuzzleFromClipboard(@NonNull Context context) {
        String toastMessage;

        ClipData clipData = ((ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE)).getPrimaryClip();
        if (clipData != null && clipData.getItemCount() > 0) {
            // Using "coerceToText" instead of "getText" guaranties that item content can be manipulated as text
            String clipboardItemStr = clipData.getItemAt(0).coerceToText(context).toString();
            // By default, we try to decode the item string using the app export format
            try {
                GameHistory gameHistory = getFromLbeUrl(context, clipboardItemStr);
                String validityErrors = PuzzleEditionUtils.getPuzzleValidityErrors(context, gameHistory);
                if (validityErrors.isEmpty()) {
                    return gameHistory;
                }
                toastMessage = validityErrors;
            } catch (IllegalArgumentException e) {
                toastMessage = e.getMessage();
            }
        } else {
            toastMessage = context.getString(R.string.invalid_lbe_url_not_found);
        }

        // If no valid puzzle can be found, a popup dialog is shown to inform the user of the failure
        Toast.makeText(context, toastMessage, Toast.LENGTH_LONG).show();
        return null;
    }
}
