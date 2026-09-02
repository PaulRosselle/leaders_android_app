package com.leaders.gamelogic.queries;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.gamelogic.entities.Board;
import com.leaders.gamelogic.entities.Cell;
import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.entities.CharacterPath;
import com.leaders.gamelogic.entities.Position;
import com.leaders.gamelogic.enums.CharacterType;
import com.leaders.gamelogic.enums.Direction;
import com.leaders.gamelogic.enums.TeamColor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
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
     * Returns the cell adjacent to the given position in the specified direction.
     *
     * @param board the board containing the cells
     * @param position the reference position
     * @param direction the direction of the adjacent cell
     * @return the adjacent cell, or {@code null} if the position has no adjacent
     *         cell in the specified direction
     */
    public static Cell findAdjacentCell(@NonNull Board board,
                                        @NonNull Position position, @NonNull Direction direction) {
        Position adjacentPos = position.adjacent(direction);
        return adjacentPos != null ? board.getCell(adjacentPos) : null;
    }

    /**
     * Finds all empty cells reachable from the given position within the specified
     * maximum distance.
     *
     * @param board the board to search on
     * @param position the center position of the search
     * @param maxDistance the maximum search distance
     * @return a list of empty cells within the specified distance
     */
    public static List<Cell> findEmptyCellsAround(@NonNull Board board, @NonNull Position position, int maxDistance) {
        LinkedHashSet<Cell> emptyCells = new LinkedHashSet<>();
        LinkedHashSet<Position> visited = new LinkedHashSet<>();

        List<Position> currentLevel = new ArrayList<>();
        currentLevel.add(position);
        visited.add(position);

        // Since the distance to an immediately adjacent cell is 1, we initialize the distance to 1
        for (int distance = 1; distance <= maxDistance; distance++) {
            List<Position> nextLevel = new ArrayList<>();
            for (Position currentPosition : currentLevel) {
                for (Direction direction : Direction.values()) {
                    Position adjacentPosition = currentPosition.adjacent(direction);
                    if (adjacentPosition != null && !visited.contains(adjacentPosition)) {
                        visited.add(adjacentPosition);
                        Cell cell = board.getCell(adjacentPosition);
                        if (cell.getCharacter() == null) {
                            emptyCells.add(cell);
                            nextLevel.add(adjacentPosition);
                        }
                    }
                }
            }

            currentLevel = nextLevel;
        }

        return new ArrayList<>(emptyCells);
    }

    @NonNull
    public static List<CharacterPath> getEmptyPathsAround(@NonNull Board board,
                                                          @NonNull Position position,
                                                          int distance,
                                                          boolean avoidDuplicates) {
        if (distance <= 0) {
            return Collections.emptyList();
        }

        List<CharacterPath> paths = new ArrayList<>();
        Set<Position> destinations = new HashSet<>();

        findEmptyPathsAround(
                board, position, position, distance,
                new ArrayList<>(List.of(position)),
                destinations, paths, avoidDuplicates
        );

        return paths;
    }

    private static void findEmptyPathsAround(@NonNull Board board, @NonNull Position startPos,
                                             @NonNull Position currentPos, int remainingDistance,
                                             @NonNull List<Position> currentPath,
                                             @NonNull Set<Position> destinations,
                                             @NonNull List<CharacterPath> paths,
                                             boolean avoidDuplicates) {
        if (remainingDistance == 0) {
            return;
        }

        for (Cell cell : BoardQuery.findEmptyCellsAround(board, currentPos, 1)) {
            Position adjacentPos = cell.getPosition();

            List<Position> newPath = new ArrayList<>(currentPath);
            newPath.add(adjacentPos);

            CharacterPath path = new CharacterPath(newPath);

            // We keep only the first path per destination
            if (!startPos.equals(adjacentPos) && (!avoidDuplicates || destinations.add(adjacentPos))) {
                paths.add(path);
            }

            findEmptyPathsAround(
                    board, startPos, adjacentPos,
                    remainingDistance - 1,
                    newPath, destinations, paths,
                    avoidDuplicates
            );
        }
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
     * <p>White team starts on the last row of each column, while the other team
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
        return board.getCell(getLeaderStartingPosition(teamColor));
    }

    /**
     * Returns the starting postion of the specified team's leader.
     *
     * <p>This position is determined by the game rules and does not depend on the
     * current state of the board.</p>
     *
     * @param teamColor the team color of the leader.
     * @return the leader's starting cell.
     */
    @NonNull
    public static Position getLeaderStartingPosition(@NonNull TeamColor teamColor) {
        // Leaders start in the central column of the board, each on a different end of the Y axis
        int x = Board.COLUMN_COUNT / 2;
        return new Position(x, getTeamStartingRow(x, teamColor));
    }
}
