package com.leaders.gamelogic.queries;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.gamelogic.entities.Board;
import com.leaders.gamelogic.entities.Cell;
import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.entities.Game;
import com.leaders.gamelogic.enums.Direction;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.gamelogic.enums.WarningType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class GameQuery {
    private final static int BARRAGE_WARNING_LIMIT = 2;

    private GameQuery(){
        throw new AssertionError("Cannot instantiate utility class");
    }

    /**
     * Checks whether the specified team's leader has been captured.
     *
     * @param game the game to evaluate
     * @param teamColor the team whose leader is checked
     * @return {@code true} if the leader has been captured; {@code false} otherwise
     * @throws NullPointerException if the leader cannot be found on the board
     */
    public static boolean isLeaderCaptured(@NonNull Game game, @NonNull TeamColor teamColor) {
        // A leader is captured when its capture value reaches LEADER_CAPTURE_VALUE.
        int totalCaptureValue = 0;
        Cell leaderCell = Objects.requireNonNull(
                BoardQuery.findLeaderCell(game.getBoard(), teamColor),
                "Leader not found for team " + teamColor
        );
        for (Character character : game.getRecruitedCharacters()) {
            if (character.getTeamColor() != teamColor) {
                totalCaptureValue += CharacterAbilityQuery.getCaptureContribution(game, character, leaderCell);
                if (totalCaptureValue >= CharacterAbilityQuery.LEADER_CAPTURE_VALUE) {
                    return true;
                }
            }
        }
        // If we get here, it means that the required capture value wasn't reached
        return false;
    }

    /**
     * Checks whether the specified team's leader is surrounded.
     *
     * @param game the game to evaluate
     * @param teamColor the team whose leader is checked
     * @return {@code true} if all adjacent cells around the leader are occupied;
     *         {@code false} otherwise
     * @throws NullPointerException if the leader cannot be found on the board
     */
    public static boolean isLeaderSurrounded(@NonNull Game game, @NonNull TeamColor teamColor) {
        Cell leaderCell = Objects.requireNonNull(
                BoardQuery.findLeaderCell(game.getBoard(), teamColor),
                "Leader not found for team " + teamColor
        );
        for (Direction direction : Direction.values()) {
            Cell adjacentCell = BoardQuery.findAdjacentCell(game.getBoard(), leaderCell.getPosition(), direction);
            // If at least one cell around the leader is empty, we can exit
            // immediately since we can be sure that it is not surrounded
            if (adjacentCell != null && adjacentCell.getCharacter() == null) {
                return false;
            }
        }
        // No empty adjacent cell found = surrounded leader
        return true;
    }

    /**
     * Returns whether at least two separated groups contain an empty cell.
     * <p>
     * A barrage is valid only if the chain isolates empty areas on both sides.
     * Groups containing only characters cannot be considered isolated regions.
     *
     * @param groups the connected groups of cells detected outside the barrage chain
     * @return {@code true} if at least two groups contain an empty cell; {@code false} otherwise
     */
    private static boolean hasIsolatedGroup(@NonNull List<List<Cell>> groups) {
        // There cannot be an isolated group if there's only one group
        if (groups.size() < 2)
        {
            return false;
        }

        // We count how many groups contain at least one empty cell.
        // As soon as two such groups are found, we exit the function
        int emptyGroupCount = 0;
        for (List<Cell> group : groups) {
            for (Cell cell : group) {
                if (cell.getCharacter() == null) {
                    emptyGroupCount++;
                    if (emptyGroupCount >= 2) {
                        return true;
                    }
                    break;
                }
            }
        }

        return false;
    }

    /**
     * Recursively gathers all cells connected to the given cell using adjacent cells.
     * <p>
     * Each visited cell is removed from {@code availableCells} to prevent it from
     * being explored again during the same flood fill.
     *
     * @param board the board containing the cells
     * @param cell the starting cell of the flood fill
     * @param availableCells the cells that can still be explored
     * @param connectedCells the list receiving the cells belonging to this group
     */
    private static void gatherConnectedCells(@NonNull Board board,
                                             @NonNull Cell cell,
                                             @NonNull Set<Cell> availableCells,
                                             @NonNull List<Cell> connectedCells) {
        // The current cell belongs to this connected group.
        connectedCells.add(cell);
        // Remove it so it cannot be visited again during this flood fill.
        availableCells.remove(cell);
        for (Direction direction : Direction.values()) {
            Cell adjacentCell = BoardQuery.findAdjacentCell(board, cell.getPosition(), direction);
            if (adjacentCell != null && availableCells.contains(adjacentCell)) {
                gatherConnectedCells(board, adjacentCell, availableCells, connectedCells);
            }
        }
    }

    /**
     * Returns all cells connected to the given cell.
     *
     * @param board the board containing the cells
     * @param cell the starting cell of the group
     * @param availableCells the cells that can still be explored
     * @return the group of cells connected to the starting cell
     */
    private static List<Cell> getConnectedCells(@NonNull Board board,
                                                @NonNull Cell cell,
                                                @NonNull Set<Cell> availableCells) {
        List<Cell> connectedCells = new ArrayList<>();
        gatherConnectedCells(board, cell, availableCells, connectedCells);
        return connectedCells;
    }

    /**
     * Returns whether the given chain of characters creates a barrage.
     * <p>
     * The cells occupied by the chain are considered as a wall. The remaining cells
     * are grouped into connected regions. A barrage is detected when this wall
     * separates at least two regions containing empty cells.
     *
     * @param game the current game
     * @param chain the chain of characters considered as the barrage wall
     * @return {@code true} if the chain separates the board into valid isolated regions;
     *         {@code false} otherwise
     */
    private static boolean isChainBarrage(@NonNull Game game, @NonNull Set<Cell> chain) {
        Set<Cell> availableCells = new HashSet<>();

        for (Cell cell : game.getBoard().getCells().values()) {
            // Characters belonging to the chain are considered as a wall.
            if (!chain.contains(cell)) {
                availableCells.add(cell);
            }
        }

        // Explore each connected region outside the chain.
        // The search stops immediately when two regions containing empty cells are found.
        List<List<Cell>> connectedGroups = new ArrayList<>();
        while (!availableCells.isEmpty()) {
            List<Cell> group = getConnectedCells(game.getBoard(), availableCells.iterator().next(), availableCells);
            connectedGroups.add(group);
            if (hasIsolatedGroup(connectedGroups)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Recursively explores all possible chains of allied characters without repetition.
     * <p>
     * Every possible extension of the current chain is tested using backtracking.
     * Once a chain contains at least four characters, it is checked to determine
     * whether it creates a barrage.
     *
     * @param game the current game
     * @param characterSet the cells occupied by allied characters
     * @param chain the current chain being explored
     * @param currentCell the last cell of the current chain
     * @return {@code true} if a valid barrage chain is found; {@code false} otherwise
     */
    private static boolean findBarrageChain(@NonNull Game game,
                                            @NonNull Set<Cell> characterSet,
                                            @NonNull LinkedHashSet<Cell> chain,
                                            @NonNull Cell currentCell) {

        // A chain of four or more characters can potentially create a barrage.
        // We keep exploring after reaching four characters because a longer chain
        // might be the one actually separating the board.
        if (chain.size() >= 4 && isChainBarrage(game, chain)) {
            return true;
        }

        for (Direction direction : Direction.values()) {
            Cell adjacentCell = BoardQuery.findAdjacentCell(game.getBoard(), currentCell.getPosition(), direction);
            // The adjacent cell must contain an allied character that is not already
            // part of the current chain to avoid loops and repeated characters.
            if (adjacentCell != null && characterSet.contains(adjacentCell) && !chain.contains(adjacentCell)) {
                chain.add(adjacentCell);
                if (findBarrageChain(game, characterSet, chain, adjacentCell)) {
                    return true;
                }
                // Remove the last character to explore another possible chain.
                chain.remove(adjacentCell);
            }
        }

        return false;
    }

    /**
     * Detects whether the specified team has created a barrage.
     * <p>
     * A barrage is a chain of four or more allied characters without repetition
     * that separates the board into isolated regions containing empty cells.
     *
     * @param game the current game
     * @param teamColor the team to check
     * @return {@code true} if a barrage is detected; {@code false} otherwise
     */
    public static boolean isBarrageDetected(@NonNull Game game, @NonNull TeamColor teamColor) {
        List<Cell> characterCells = BoardQuery.findCharacterCells(game.getBoard(), teamColor, null);
        // A barrage requires at least four allied characters on the board.
        if (characterCells.size() < 4)
        {
            return false;
        }

        // The set allows fast lookup while exploring possible chains.
        Set<Cell> characterSet = new HashSet<>(characterCells);
        // Every character can be the starting point of a potential chain.
        // We cannot assume that a chain starts from an extremity.
        for (Cell startCell : characterCells) {
            LinkedHashSet<Cell> chain = new LinkedHashSet<>();
            chain.add(startCell);
            if (findBarrageChain(game, characterSet, chain, startCell)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the winning team, if the game has ended.
     *
     * @param game the game to evaluate
     * @param currentPhaseTeam the team whose turn is currently being resolved
     * @return the winning team, or {@code null} if the game has no winner yet
     */
    @Nullable
    public static TeamColor getWinnerTeam(@NonNull Game game, @NonNull TeamColor currentPhaseTeam) {
        // First, we get the team for both players
        TeamColor opponentTeam = currentPhaseTeam.getOpposite();
        TeamColor winnerTeam = null;

        // Once a player reach the barrage warning limit, they lose the game. A player can only lose
        // because of barrage warnings during his turn so we don't make the check for the opponent
        if (game.getPlayerWarningCount(currentPhaseTeam, WarningType.Barrage) >= BARRAGE_WARNING_LIMIT) {
            return opponentTeam;
        }

        // We check if the current leader is captured/surrounded first to stay consistent with
        // the "don't capture your own leader" rule
        if (isLeaderCaptured(game, currentPhaseTeam) || isLeaderSurrounded(game, currentPhaseTeam)) {
            winnerTeam = opponentTeam;
        } else if (isLeaderCaptured(game, opponentTeam) || isLeaderSurrounded(game, opponentTeam)) {
            winnerTeam = currentPhaseTeam;
        }

        return winnerTeam;
    }
}
