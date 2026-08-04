package com.leaders.gamelogic.queries;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.gamelogic.entities.Board;
import com.leaders.gamelogic.entities.Cell;
import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.entities.Position;
import com.leaders.gamelogic.enums.CharacterType;
import com.leaders.gamelogic.enums.Direction;
import com.leaders.gamelogic.enums.TeamColor;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

public final class BoardQuery {
    private BoardQuery(){
        throw new AssertionError("Cannot instantiate utility class");
    }

    /**
     * Returns whether the specified character matches the given criteria.
     *
     * @param character the character to test.
     * @param teamColor {@code null} means any team color.
     * @param characterType {@code null} means any character type.
     * @return {@code true} if the character matches all specified criteria;
     * {@code false} otherwise.
     */
    private static boolean characterMatches(@NonNull Character character,
                                            @Nullable TeamColor teamColor,
                                            @Nullable CharacterType characterType) {

        return (teamColor == null || character.getTeamColor() == teamColor) &&
                (characterType == null || character.getCharacterType() == characterType);
    }

    /**
     * Returns all cells containing a character matching the specified criteria.
     *
     * @param board the board to search.
     * @param characterColor {@code null} means any team color.
     * @param characterType {@code null} means any character type.
     * @return the matching cells, or an empty list if no matching character is found.
     */
    @NonNull
    public static List<Cell> findCharacterCells(@NonNull Board board,
                                                @Nullable TeamColor characterColor,
                                                @Nullable CharacterType characterType) {
        List<Cell> matchingCells = new ArrayList<>();
        for (Cell cell : board.getCells().values()) {
            Character character = cell.getCharacter();
            if (character != null && characterMatches(character, characterColor, characterType)) {
                matchingCells.add(cell);
            }
        }
        return matchingCells;
    }

    /**
     * Returns the first visible character cell in the given direction whose
     * character matches the specified criteria.
     *
     * <p>The search starts from the cell immediately adjacent to {@code position}
     * and skips empty cells. The first encountered character stops the search.
     * If that character does not match the criteria, {@code null} is returned.</p>
     *
     * @param board the board to search.
     * @param position the starting position.
     * @param direction the direction of the search.
     * @param characterColor {@code null} means any team color.
     * @param characterType {@code null} means any character type.
     * @return the first visible character cell if its character matches the criteria,
     * or {@code null} if no character is visible in that direction or if the first
     * visible character does not match.
     */
    @Nullable
    public static Cell findVisibleCharacterCell(@NonNull Board board, @NonNull Position position,
                                                @NonNull Direction direction, @Nullable TeamColor characterColor,
                                                @Nullable CharacterType characterType) {
        Position currentPos = position.adjacent(direction);
        while (currentPos != null) {
            Cell cell = board.getCell(currentPos);
            Character character = cell.getCharacter();
            if (character != null) {
                return characterMatches(character, characterColor, characterType) ? cell : null;
            }
            currentPos = currentPos.adjacent(direction);
        }
        return null;
    }

    /**
     * Returns the cell occupied by the leader of the specified team.
     *
     * @param board the board to search.
     * @param teamColor the team color of the leader.
     * @return the leader's cell, or {@code null} if the leader is not present on the board.
     */
    @Nullable
    public static Cell findLeaderCell(@NonNull Board board, @NonNull TeamColor teamColor) {
        for (Cell cell : board.getCells().values()) {
            Character character = cell.getCharacter();
            if (character != null && character.getTeamColor() == teamColor &&
                    character.getCharacterType().getCharacterCard().isLeader()) {
                return cell;
            }
        }
        return null;
    }

    /**
     * Returns the cell occupied by the character with the specified identifier.
     *
     * <p>This method assumes that the character exists on the board.</p>
     *
     * @param board the board to search.
     * @param characterId the identifier of the character.
     * @return the character's cell.
     * @throws NoSuchElementException if the character is not present on the board.
     */
    @NonNull
    public static Cell getCellByCharacterId(@NonNull Board board, @NonNull UUID characterId) {
        for (Cell cell : board.getCells().values()) {
            Character character = cell.getCharacter();
            if (character != null && characterId.equals(character.getId())) {
                return cell;
            }
        }
        throw new NoSuchElementException("Character not found on the board");
    }

    /**
     * Returns the starting row index for the specified team on the given column.
     *
     * <p>Black team starts on the last row of each column, while the other team
     * starts on the first row.</p>
     *
     * @param x the column index.
     * @param teamColor the team.
     * @return the starting row index for the specified team.
     */
    private static int getTeamStartingRow(int x, @NonNull TeamColor teamColor) {
        return teamColor == TeamColor.Black ? Board.getRowCount(x) - 1 : 0;
    }

    /**
     * Returns all empty cells where the specified team can recruit a new character.
     *
     * @param board the board to inspect.
     * @param teamColor the recruiting team.
     * @return the empty recruitment cells, or an empty list if no cell is available.
     */
    @NonNull
    public static List<Cell> getRecruitmentCells(@NonNull Board board, @NonNull TeamColor teamColor) {
        // Recruitment cells are located on the outer rows of the board's hexagon.
        List<Cell> recruitmentCells = new ArrayList<>();
        for (int x = 0; x < Board.COLUMN_COUNT; x++) {
            Cell recruitmentCell = board.getCell(new Position(x, getTeamStartingRow(x, teamColor)));
            // A character can only be recruited on an empty cell
            if (recruitmentCell.getCharacter() == null) {
                recruitmentCells.add(recruitmentCell);
            }
        }
        return recruitmentCells;
    }

    /**
     * Returns the starting cell of the specified team's leader.
     *
     * <p>This cell is determined by the game rules and does not depend on the
     * current state of the board.</p>
     *
     * @param board the game board.
     * @param teamColor the team color of the leader.
     * @return the leader's starting cell.
     */
    @NonNull
    public static Cell getLeaderStartingCell(@NonNull Board board, @NonNull TeamColor teamColor) {
        // Leaders start in the central column of the board, each on a different end of the Y axis
        int x = Board.COLUMN_COUNT / 2;
        return board.getCell(new Position(x, getTeamStartingRow(x, teamColor)));
    }
}
