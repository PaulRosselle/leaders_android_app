package com.leaders.puzzlelogic.utilities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.gamelogic.actions.CharacterAction;
import com.leaders.gamelogic.entities.Board;
import com.leaders.gamelogic.entities.Cell;
import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.entities.Game;
import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.gamelogic.enums.CharacterType;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.gamelogic.factories.GameActionHandlerFactory;
import com.leaders.gamelogic.factories.GameFactory;
import com.leaders.gamelogic.handlers.GameActionHandler;
import com.leaders.gamelogic.queries.BoardQuery;

import java.util.List;

public class SniperEvasionUtils {
    private SniperEvasionUtils(){
        throw new AssertionError("Cannot instantiate utility class");
    }

    // TODO - javadoc
    @Nullable
    public static CharacterAction findEscapeAction(@NonNull GameHistory history,
                                                   @NonNull TeamColor teamColor) {
        Game game = GameFactory.create(history);

        Cell sniperCell = getSniperCell(game.getBoard(), teamColor.getOpposite());
        List<CharacterAction> leaderActions = GameSearchUtils.enumerateActions(
                game, history, getLeaderCell(game.getBoard(), teamColor).getCharacter()
        );

        for (CharacterAction action : leaderActions) {
            GameActionHandler handler = GameActionHandlerFactory.create(game, action);
            handler.doAction();

            try {
                if (!isInSniperRange(sniperCell, getLeaderCell(game.getBoard(), teamColor))) {
                    return action;
                }
            } finally {
                handler.undoAction();
            }
        }

        return null;
    }

    public static boolean isLeaderInSniperRange(@NonNull Board board, @NonNull TeamColor leaderTeamColor) {
        return isInSniperRange(
                getSniperCell(board, leaderTeamColor.getOpposite()),
                getLeaderCell(board, leaderTeamColor)
        );
    }

    @NonNull
    private static Cell getLeaderCell(@NonNull Board board, @NonNull TeamColor teamColor) {
        Cell leaderCell = BoardQuery.findLeaderCell(board, teamColor);
        if (leaderCell == null) {
            throw new IllegalStateException("Invalid sniper escape search: leader missing for team " + teamColor);
        }
        return leaderCell;
    }

    @NonNull
    private static Cell getSniperCell(@NonNull Board board, @NonNull TeamColor teamColor) {
        return BoardQuery.findCharacterCells(
                board, teamColor, CharacterType.Sniper
        ).stream().findFirst().orElseThrow(() ->
                new IllegalStateException("Invalid sniper escape search: sniper missing for team " + teamColor.getOpposite())
        );
    }

    // TODO - javadoc
    private static boolean isInSniperRange(@NonNull Cell sniperCell, @NonNull Cell targetCell) {
        return sniperCell.getPosition().isAligned(targetCell.getPosition()) &&
                sniperCell.getPosition().distanceTo(targetCell.getPosition()) >= 3;
    }
}
