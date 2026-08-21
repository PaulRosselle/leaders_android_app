package com.leaders.app.utilities;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.actions.IGameAction;
import com.leaders.gamelogic.actions.RecruitmentAction;
import com.leaders.gamelogic.actions.RecruitmentActionMotion;
import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.gamelogic.entities.PlayableCharacter;
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

    public GameHistory getFromLbeUrl(@NonNull String lbeUrl) {
        List<IGameAction> initialActions = new ArrayList<>();
        for (TeamColor teamColor : TeamColor.values()) {
            for (PlayableCharacter playableCharacter : getTeamPlayableCharacters(lbeUrl, teamColor)) {
                initialActions.add(new RecruitmentAction(List.of(
                        new RecruitmentActionMotion(RecruitmentMotionType.Add,
                                playableCharacter.getCharacter(),
                                playableCharacter.getPosition())
                        ))
                );
            }
        }

        return PuzzleEditionUtils.getDefaultHistory(initialActions);
    }

    private List<PlayableCharacter> getTeamPlayableCharacters(@NonNull String lbeUrl, TeamColor teamColor) {
        // First we get the header separator associated with the player color
        String colorHeader;
        if (teamColor == TeamColor.Black) {
            colorHeader = LbeUtils.HEADER_BLACK;
        } else {
            colorHeader = LbeUtils.HEADER_WHITE;
        }

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

        List<PlayableCharacter> playableCharacters = new ArrayList<>();

        // Once we have isolated the tiles datas we can extract each token and add them immediately to the board
        for (String cellData : cellDatas.split(LbeUtils.CELL_DATA_SEPARATOR)) {
            String[] characterDatas = cellData.split(LbeUtils.CHARACTER_DATA_SEPARATOR);
            Position position = LbeUtils.getPositionFromExportStr(characterDatas[0].toUpperCase());
            CharacterType characterType = LbeUtils.getCharacterTypeFromExportStr(characterDatas[1].toUpperCase());

            playableCharacters.add(new PlayableCharacter(
                    Character.create(characterType, teamColor),
                    position, false, false
            ));
        }

        return playableCharacters;
    }
}
