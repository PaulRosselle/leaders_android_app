package com.leaders.puzzlelogic.utilities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.gamelogic.actions.CharacterAction;
import com.leaders.gamelogic.entities.Board;
import com.leaders.gamelogic.entities.Cell;
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

    /**
     * Finds an action that moves the specified team's leader out of the opposing sniper's range.
     *
     * @param history the game history used to determine the current game state
     * @param teamColor the color of the leader's team
     * @return an action that moves the leader out of the opposing sniper's range,
     *         or {@code null} if no such action exists
     * @throws IllegalStateException if the leader or the opposing sniper is missing
     */
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

    /**
     * Checks whether the specified team's leader is within the opposing sniper's range.
     *
     * @param board the board to check
     * @param leaderTeamColor the color of the leader's team
     * @return {@code true} if the leader is within the opposing sniper's range,
     *         {@code false} otherwise
     * @throws IllegalStateException if the leader or the opposing sniper is missing
     */
    public static boolean isLeaderInSniperRange(@NonNull Board board, @NonNull TeamColor leaderTeamColor) {
        return isInSniperRange(
                getSniperCell(board, leaderTeamColor.getOpposite()),
                getLeaderCell(board, leaderTeamColor)
        );
    }

    /**
     * Returns the leader's cell for the specified team.
     *
     * @param board the board to search
     * @param teamColor the color of the team
     * @return the cell occupied by the team's leader
     * @throws IllegalStateException if the team's leader is missing
     */
    @NonNull
    private static Cell getLeaderCell(@NonNull Board board, @NonNull TeamColor teamColor) {
        Cell leaderCell = BoardQuery.findLeaderCell(board, teamColor);
        if (leaderCell == null) {
            throw new IllegalStateException("Invalid sniper escape search: leader missing for team " + teamColor);
        }
        return leaderCell;
    }

    /**
     * Returns a cell occupied by a sniper of the specified team.
     *
     * @param board the board to search
     * @param teamColor the color of the team
     * @return a cell occupied by a sniper of the specified team
     * @throws IllegalStateException if no sniper is found for the specified team
     */
    @NonNull
    private static Cell getSniperCell(@NonNull Board board, @NonNull TeamColor teamColor) {
        return BoardQuery.findCharacterCells(
                board, teamColor, CharacterType.Sniper
        ).stream().findFirst().orElseThrow(() ->
                new IllegalStateException("Invalid sniper escape search: sniper missing for team " + teamColor.getOpposite())
        );
    }

    /**
     * Checks whether the target cell is within the sniper's attack range.
     *
     * @param sniperCell the cell occupied by the sniper
     * @param targetCell the cell to check
     * @return {@code true} if the cells are aligned and their distance is at least 3,
     *         {@code false} otherwise
     */
    private static boolean isInSniperRange(@NonNull Cell sniperCell, @NonNull Cell targetCell) {
        return sniperCell.getPosition().isAligned(targetCell.getPosition()) &&
                sniperCell.getPosition().distanceTo(targetCell.getPosition()) >= 3;
    }

    /**
     * Checks whether the specified team has at least one sniper on the board.
     *
     * @param board the board to check
     * @param teamColor the color of the team
     * @return {@code true} if the team has at least one sniper, {@code false} otherwise
     */
    private static boolean hasSniper(@NonNull Board board, @NonNull TeamColor teamColor) {
        return !BoardQuery.findCharacterCells(board, teamColor, CharacterType.Sniper).isEmpty();
    }

    /**
     * Checks whether the specified team's leader is threatened by an opposing sniper.
     *
     * @param board the board to check
     * @param teamColor the color of the leader's team
     * @return {@code true} if the opposing team has a sniper within range of the leader,
     *         {@code false} otherwise
     * @throws IllegalStateException if the leader or the opposing sniper is missing
     */
    public static boolean isLeaderThreatened(@NonNull Board board, @NonNull TeamColor teamColor) {
        return hasSniper(board, teamColor.getOpposite()) && isLeaderInSniperRange(board, teamColor);
    }
}
